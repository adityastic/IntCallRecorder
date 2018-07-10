package com.adityagupta.intern2.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.util.Log;

import com.adityagupta.intern2.interfaces.PhoneListener;
import com.adityagupta.intern2.utils.Preferences;

public class CallBroadcastReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent) {

        Preferences.prefs = context.getSharedPreferences("call_info",Context.MODE_PRIVATE);

        if(Preferences.prefs.getBoolean("call_record",false))
        {
            String numberToCall;
            Log.d("CallRecorder", "CallBroadcastReceiver::onReceive got Intent: " + intent.toString());
            if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                numberToCall = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                Log.d("CallRecorder", "CallBroadcastReceiver intent has EXTRA_PHONE_NUMBER: " + numberToCall);
            }

            PhoneListener phoneListener = new PhoneListener(context);
            TelephonyManager telephony = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
            Log.d("PhoneStateReceiveronRec", "set PhoneStateListener");
        }else
            Log.e("Recording Disabled","YES");
    }
}
