package com.adityagupta.nxtvisioncallrecorder.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.adityagupta.nxtvisioncallrecorder.application.ApplicationActivity;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class Preferences {

    public static String getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String device_id = tm.getDeviceId();
        return device_id;
    }

    public static void checkLicense(Context context) {
        Log.e("Lincense Check", "Checking");
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        JsonObjectRequest req = new JsonObjectRequest("https://raw.githubusercontent.com/adityastic/LicensingRepo/master/NxtRecorder.json", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("Record Enabled", response.toString());
                        try {
                            prefs.edit().putBoolean("allowed", response.getBoolean("Record")).apply();
                        } catch (JSONException e) {
                            prefs.edit().putBoolean("allowed", false).apply();
                            e.printStackTrace();
                        }
                        Log.e("Saved Value", prefs.getBoolean("allowed", false) + "");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        ApplicationActivity.requestQueue.getCache().clear();
        ApplicationActivity.requestQueue.add(req);
    }
}
