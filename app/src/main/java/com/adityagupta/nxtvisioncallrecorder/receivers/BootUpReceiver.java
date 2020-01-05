package com.adityagupta.nxtvisioncallrecorder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.adityagupta.nxtvisioncallrecorder.utils.Preferences.checkLicense;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("Boot UP", "Recieved");

        checkLicense(context);
    }
}
