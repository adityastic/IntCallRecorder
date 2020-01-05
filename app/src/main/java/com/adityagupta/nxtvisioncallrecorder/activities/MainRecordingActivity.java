package com.adityagupta.nxtvisioncallrecorder.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.adityagupta.nxtvisioncallrecorder.MyAccessibilityService;
import com.adityagupta.nxtvisioncallrecorder.R;
import com.adityagupta.nxtvisioncallrecorder.utils.Preferences;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import static com.adityagupta.nxtvisioncallrecorder.utils.Preferences.checkLicense;

public class MainRecordingActivity extends AppCompatActivity {

    private static final int ACCESSIBILITY_ENABLED = 1;
    CardView whitelist, background, accessibility;
    TextView imei;

    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("AU", "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == ACCESSIBILITY_ENABLED) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarGradiant(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            Drawable background = activity.getResources().getDrawable(R.drawable.gradient_3);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_recording);
        setStatusBarGradiant(this);

        whitelist = findViewById(R.id.card_id_white);
        accessibility = findViewById(R.id.card_id_acc);
        background = findViewById(R.id.card_id_backg);
        imei = findViewById(R.id.device_imei);

        imei.setText(Preferences.getIMEI(this));

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.PROCESS_OUTGOING_CALLS}, 1);

        whitelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MainRecordingActivity.this).title("WhiteList App")
                        .content(
                                "Please Check Call Recorder in the coming screen.\n\nHave Fun.\nIf you have already done this click Cancel.")
                        .positiveText("Ok")
                        .theme(Theme.LIGHT)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setComponent(new ComponentName("com.iqoo.secure",
                                            "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(MainRecordingActivity.this, "Error in WhiteListCard", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .negativeText("Cancel")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MainRecordingActivity.this).title("Enable AutoStart")
                        .content(
                                "Goto \n1. App Manager\n2.Autostart manager\n3.Check Call Recorder\n\nHave Fun.\nIf you have already done this click Cancel.")
                        .positiveText("Ok")
                        .theme(Theme.LIGHT)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                enableAutoStart();
                            }
                        })
                        .negativeText("Cancel")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        accessibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MainRecordingActivity.this).title("Enable AutoStart")
                        .content(
                                "Check Accesibility")
                        .positiveText("Ok")
                        .theme(Theme.LIGHT)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (!isAccessibilitySettingsOn(MainRecordingActivity.this)) {
                                    Log.e("Access Enabled", "FALSE");
                                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                                } else
                                    Log.e("Access Enabled", "TRUE");
                            }
                        })
                        .negativeText("Cancel")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        checkLicense(this);
//
//        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//
//        AppCompatSpinner spinner = findViewById(R.id.spinnerCall);
//        ArrayList<String> strlist = new ArrayList<>();
//        strlist.add("Default");
//        strlist.add("Voice Call");
//        strlist.add("Voice Communication");
//        strlist.add("Camcorder");
//        strlist.add("MIC");
//        strlist.add("Voice Downlink");
//        strlist.add("Voice Uplink");
//        strlist.add("Voice Recognition");
//        ArrayAdapter<String> adap = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, strlist);
//        spinner.setAdapter(adap);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                prefs.edit().putInt("source",position).apply();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        if (prefs.getInt("source", -1) == -1) {
//            prefs.edit().putInt("source", 0).apply();
//        } else {
//            spinner.setSelection(prefs.getInt("source", 0),false);
//        }
//
//
//        AppCompatSpinner spinner2 = findViewById(R.id.spinnerCallAudio);
//        ArrayList<String> strlist2 = new ArrayList<>();
//        strlist2.add("Default");
//        strlist2.add("AMR_NB");
//        strlist2.add("AMR_WB");
//        strlist2.add("AAC");
//        strlist2.add("HE_AAC");
//        strlist2.add("AAC_ELD");
//        strlist2.add("VORBIS");
//        ArrayAdapter<String> adap2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, strlist2);
//        spinner2.setAdapter(adap2);
//        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                prefs.edit().putInt("audio",position).apply();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        if (prefs.getInt("audio", -1) == -1) {
//            prefs.edit().putInt("audio", 0).apply();
//        } else {
//            spinner.setSelection(prefs.getInt("audio", 0),false);
//        }
    }

    private void enableAutoStart() {
        if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
            new MaterialDialog.Builder(MainRecordingActivity.this).title("Enable AutoStart")
                    .content(
                            "Please allow QuickAlert to always run in the background,else our services can't be accessed when you are in distress")
                    .theme(Theme.LIGHT)
                    .positiveText("ALLOW")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.miui.securitycenter",
                                    "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                            startActivity(intent);
                        }
                    })
                    .show();
        } else if (Build.BRAND.equalsIgnoreCase("Letv")) {
            new MaterialDialog.Builder(MainRecordingActivity.this).title("Enable AutoStart")
                    .content(
                            "Please allow QuickAlert to always run in the background,else our services can't be accessed when you are in distress")
                    .theme(Theme.LIGHT)
                    .positiveText("ALLOW")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.letv.android.letvsafe",
                                    "com.letv.android.letvsafe.AutobootManageActivity"));
                            startActivity(intent);
                        }
                    })
                    .show();
        } else if (Build.BRAND.equalsIgnoreCase("Honor")) {
            new MaterialDialog.Builder(MainRecordingActivity.this).title("Enable AutoStart")
                    .content(
                            "Please allow QuickAlert to always run in the background,else our services can't be accessed when you are in distress")
                    .theme(Theme.LIGHT)
                    .positiveText("ALLOW")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.huawei.systemmanager",
                                    "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                            startActivity(intent);
                        }
                    })
                    .show();
        } else if (Build.MANUFACTURER.equalsIgnoreCase("oppo")) {
            new MaterialDialog.Builder(MainRecordingActivity.this).title("Enable AutoStart")
                    .content(
                            "Please allow QuickAlert to always run in the background,else our services can't be accessed when you are in distress")
                    .theme(Theme.LIGHT)
                    .positiveText("ALLOW")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            try {
                                Intent intent = new Intent();
                                intent.setClassName("com.coloros.safecenter",
                                        "com.coloros.safecenter.permission.startup.StartupAppListActivity");
                                startActivity(intent);
                            } catch (Exception e) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setClassName("com.oppo.safe",
                                            "com.oppo.safe.permission.startup.StartupAppListActivity");
                                    startActivity(intent);
                                } catch (Exception ex) {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setClassName("com.coloros.safecenter",
                                                "com.coloros.safecenter.startupapp.StartupAppListActivity");
                                        startActivity(intent);
                                    } catch (Exception exx) {

                                    }
                                }
                            }
                        }
                    })
                    .show();
        } else if (Build.MANUFACTURER.contains("vivo")) {
            new MaterialDialog.Builder(MainRecordingActivity.this).title("Enable AutoStart")
                    .content(
                            "Please allow QuickAlert to always run in the background.Our app runs in background to detect when you are in distress.")
                    .positiveText("ALLOW")
                    .theme(Theme.LIGHT)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            try {
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName("com.iqoo.secure",
                                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
                                startActivity(intent);
                            } catch (Exception e) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                                    startActivity(intent);
                                } catch (Exception ex) {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setClassName("com.iqoo.secure",
                                                "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager");
                                        startActivity(intent);
                                    } catch (Exception exx) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                    })
                    .show();
        }
    }
}
