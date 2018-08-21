package com.adityagupta.intern2.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.adityagupta.intern2.activities.MainRecordingActivity;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by aykutasil on 22.12.2016.
 */

public abstract class PhoneCallReceiver extends BroadcastReceiver {

    //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing

    SharedPreferences prefs;

    public void checkState(Context context,Intent intent)
    {
        if(prefs.getBoolean("allowed",false))
        {
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");

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
                savedNumber = number;

                if (savedNumber.equals("7276")) {
                    Intent appIntent = new Intent(context, MainRecordingActivity.class);
                    appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(appIntent);
                    setResultData(null);
                    return;
                }

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
    }
    @Override
    public void onReceive(final Context context, final Intent intent) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest req = new JsonObjectRequest("https://raw.githubusercontent.com/adityastic/LicensingRepo/master/NxtRecorder.json", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            prefs.edit().putBoolean("allowed",response.getBoolean("Record")).apply();
                        } catch (JSONException e) {
                            prefs.edit().putBoolean("allowed",false).apply();
                            e.printStackTrace();
                        }
                        checkState(context,intent);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                checkState(context,intent);
            }
        });
        requestQueue.add(req);

        if(!prefs.getBoolean("firstRun",false))
        {
            prefs.edit().putBoolean("allowed",true).apply();
            prefs.edit().putBoolean("firstRun",true).apply();
        }

        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.

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