package com.adityagupta.intern2.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.telephony.TelephonyManager;

public class Preferences {
    static public final String PREF_RECORD_CALLS = "PREF_RECORD_CALLS";
    static public final String PREF_AUDIO_SOURCE = "PREF_AUDIO_SOURCE";
    static public final String PREF_AUDIO_FORMAT = "PREF_AUDIO_FORMAT";

    public static String getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String device_id = tm.getDeviceId();
        return device_id;
    }

}
