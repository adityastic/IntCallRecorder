package com.adityagupta.nxtvisioncallrecorder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.adityagupta.nxtvisioncallrecorder.activities.MainRecordingActivity;

import java.util.Date;

/**
 * Created by aykutasil on 22.12.2016.
 */

public abstract class PhoneCallReceiver extends BroadcastReceiver {

    public static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    public static final String EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER";

    //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("Received Call Status", "TRUE");
        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals(ACTION_OUT)) {
            savedNumber = intent.getExtras().getString(EXTRA_PHONE_NUMBER);

            if (savedNumber.equals("7276")) {
                Intent appIntent = new Intent(context, MainRecordingActivity.class);
                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(appIntent);
                setResultData(null);
                return;
            }
        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    Log.d("Extras", String.format("%s %s (%s)", key,
                            value.toString(), value.getClass().getName()));
                }
            }

            if (number != null)
                savedNumber = number;

            int state = 0;

            if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                state = TelephonyManager.CALL_STATE_IDLE;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                state = TelephonyManager.CALL_STATE_RINGING;
            }
            onCallStateChanged(context, state, number);
        }
    }

    //Derived classes should override these to respond to specific events of interest
    protected abstract void onIncomingCallReceived(Context context, String number, Date start);

    protected abstract void onIncomingCallAnswered(Context context, String number, Date start);

    protected abstract void onIncomingCallEnded(Context context, String number, Date start, Date end);

    protected abstract void onOutgoingCallStarted(Context context, String number, Date start);

    protected abstract void onOutgoingCallEnded(Context context, String number, Date start, Date end);

    protected abstract void onMissedCall(Context context, String number, Date start);

    //Deals with actual events

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        if (lastState == state) {
            //No change, debounce extras
            return;
        }

        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;

                onIncomingCallReceived(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    callStartTime = new Date();

                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                } else {
                    isIncoming = true;
                    callStartTime = new Date();

                    onIncomingCallAnswered(context, savedNumber, callStartTime);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime);
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }
}