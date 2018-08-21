package com.adityagupta.intern2.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adityagupta.intern2.activities.MainRecordingActivity;

public class BootUpReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("Broadcast","YES");
    }
}
