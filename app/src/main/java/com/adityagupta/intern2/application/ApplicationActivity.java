package com.adityagupta.intern2.application;

import android.app.Application;

import com.adityagupta.intern2.R;
import com.adityagupta.intern2.utils.typeface.TypefaceUtil;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class ApplicationActivity extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/circular_std_black.otf"); // font from assets: "assets/fonts/Roboto-Regular.ttf


        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/circular_std_book.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
