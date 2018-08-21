package com.adityagupta.intern2.interfaces;

import android.content.Intent;
import android.content.Context;
import android.content.ComponentName;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.util.Log;

import com.adityagupta.intern2.services.RecordService;

public class PhoneListener extends PhoneStateListener
{
    private Context context;

    public PhoneListener(Context c) {
        Log.e("CallRecorder", "PhoneListener constructor");
        context = c;
    }

    public void onCallStateChanged (int state, String incomingNumber)
    {
        Log.e("CallRecorder", "PhoneListener::onCallStateChanged state:" + state + " incomingNumber:" + incomingNumber);

        switch (state) {
        case TelephonyManager.CALL_STATE_IDLE:
            Log.e("CallRecorder", "CALL_STATE_IDLE, stoping recording");
            Boolean stopped = context.stopService(new Intent(context, RecordService.class));
            Log.e("CallRecorder", "stopService for RecordService returned " + stopped);
            break;
        case TelephonyManager.CALL_STATE_RINGING:
            Log.e("CallRecorder", "CALL_STATE_RINGING");
            break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
            Log.e("CallRecorder", "CALL_STATE_OFFHOOK starting recording");
            Intent callIntent = new Intent(context, RecordService.class);
            ComponentName name = context.startService(callIntent);
            if (null == name) {
                Log.e("CallRecorder", "startService for RecordService returned null ComponentName");
            } else {
                Log.i("CallRecorder", "startService returned " + name.flattenToString());
            }
            break;
        }
    }
}
