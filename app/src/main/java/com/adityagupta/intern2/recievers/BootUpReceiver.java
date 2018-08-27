package com.adityagupta.intern2.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.adityagupta.intern2.activities.MainRecordingActivity;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class BootUpReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        requestQueue.add(req);
    }
}
