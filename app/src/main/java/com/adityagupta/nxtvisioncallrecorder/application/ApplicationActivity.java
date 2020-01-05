package com.adityagupta.nxtvisioncallrecorder.application;

import android.app.Application;

import com.adityagupta.nxtvisioncallrecorder.R;
import com.adityagupta.nxtvisioncallrecorder.utils.typeface.TypefaceUtil;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class ApplicationActivity extends Application {
    public static RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/circular_std_black.otf"); // font from assets: "assets/fonts/Roboto-Regular.ttf

        requestQueue = Volley.newRequestQueue(this);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/circular_std_book.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
