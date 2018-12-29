package com.adityagupta.nxtvisioncallrecorder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.adityagupta.nxtvisioncallrecorder.application.ApplicationActivity;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import static com.adityagupta.nxtvisioncallrecorder.utils.Preferences.checkLicense;

public class BootUpReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("Boot UP","Recieved");

        checkLicense(context);
    }
}
