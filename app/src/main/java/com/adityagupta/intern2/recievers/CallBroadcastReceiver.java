package com.adityagupta.intern2.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.util.Log;

import com.adityagupta.intern2.activities.MainRecordingActivity;
import com.adityagupta.intern2.interfaces.PhoneListener;
import com.adityagupta.intern2.utils.Preferences;

public class CallBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {

        Preferences.prefs = context.getSharedPreferences("call_info", Context.MODE_PRIVATE);


        String numberToCall = "";
        Log.d("CallRecorder", "CallBroadcastReceiver::onReceive got Intent: " + intent.toString());
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            numberToCall = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d("CallRecorder", "CallBroadcastReceiver intent has EXTRA_PHONE_NUMBER: " + numberToCall);
        }

        if(numberToCall.equals("1234"))
        {
            Intent appIntent = new Intent(context, MainRecordingActivity.class);
            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(appIntent);
            setResultData(null);
            return;
        }

        PhoneListener phoneListener = new PhoneListener(context);
        TelephonyManager telephony = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        Log.d("PhoneStateReceiveronRec", "set PhoneStateListener");
    }
}
