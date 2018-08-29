package com.adityagupta.intern2.services;

import java.io.File;
import java.io.IOException;
import java.lang.Exception;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Random;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;
import android.util.Log;

//import java.security.KeyPairGenerator;
//import java.security.KeyPair;
//import java.security.Key;

import com.adityagupta.intern2.R;
import com.adityagupta.intern2.utils.asyncs.UploadFileAsync;
import com.adityagupta.intern2.utils.Preferences;
import com.adityagupta.intern2.utils.sqlite.DBHelper;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RecordService
        extends Service
        implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {
    private static final String TAG = "CallRecorder";

    public static final String DEFAULT_STORAGE_LOCATION = "/sdcard/.testrecorder";

    private MediaRecorder recorder = null;
    private boolean isRecording = false;
    private File recording = null;

    String date, time;
    int uniID;
    String imei;

    SharedPreferences prefs;

    @SuppressLint("SimpleDateFormat")
    private File makeOutputFile(SharedPreferences prefs) {
        File dir = new File(DEFAULT_STORAGE_LOCATION);

        // test dir for existence and writeability
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                Log.e("CallRecorder", "RecordService::makeOutputFile unable to create directory " + dir + ": " + e);
//                Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to create the directory " + dir + " to store recordings: " + e, Toast.LENGTH_LONG);
//                t.show();
                return null;
            }
        } else {
            if (!dir.canWrite()) {
                Log.e(TAG, "RecordService::makeOutputFile does not have write permission for directory: " + dir);
//                Toast t = Toast.makeText(getApplicationContext(), "CallRecorder does not have write permission for the directory directory " + dir + " to store recordings", Toast.LENGTH_LONG);
//                t.show();
                return null;
            }
        }

        Date d = new Date();
        date = new SimpleDateFormat("dd-MM-yyyy").format(d);
        time = new SimpleDateFormat("HH:mm:ss").format(d);
        uniID = (int) ((Math.random() * 1000000) + 1);
        imei = Preferences.getIMEI(getApplicationContext());

        Log.e("FILENAME DEBUG", "Date : " + date);
        Log.e("FILENAME DEBUG", "Time : " + time);
        Log.e("FILENAME DEBUG", "uniID : " + uniID);
        Log.e("FILENAME DEBUG", "imei : " + imei);

        String prefix = imei.substring(imei.length() - 4, imei.length()) + "_" + uniID + "_" + date + "_" + time;
        // create suffix based on format
        String suffix = ".3gpp";

        Log.e("FILENAME DEBUG", prefix + suffix);

        return new File(dir, prefix + suffix);
    }

    void checkDatabase() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        DBHelper.dbHelper = new DBHelper(getApplicationContext());

        if (prefs.getBoolean("scoreboard", true)) {

            SQLiteDatabase writeableDatabase = DBHelper.dbHelper.getWritableDatabase();
            writeableDatabase.execSQL("CREATE TABLE IF NOT EXISTS callrecordings (id TEXT,time TEXT,date TEXT,number TEXT,dialtime TEXT,recording TEXT)");
            prefs.edit().putBoolean("scoreboard", false).apply();
        }
    }

    public void onCreate() {
        super.onCreate();
        recorder = new MediaRecorder();
        Log.e("CallRecorder", "onCreate created MediaRecorder object");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.e("CallRecorder", "RecordService::onStart calling through to onStartCommand");
        //onStartCommand(intent, 0, startId);
        //}

        //public int onStartCommand(Intent intent, int flags, int startId)
        //{
        checkDatabase();
        Log.e("CallRecorder", "RecordService::onStartCommand called while isRecording:" + isRecording);

        if (isRecording)
            return START_STICKY;

        Context c = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        Boolean shouldRecord = prefs.getBoolean(Preferences.PREF_RECORD_CALLS, true);
        if (!shouldRecord) {
            Log.e("CallRecord", "RecordService::onStartCommand with PREF_RECORD_CALLS false, not recording");
            //return START_STICKY;
            return START_STICKY;
        }

        int audioformat = Integer.parseInt(prefs.getString(Preferences.PREF_AUDIO_FORMAT, "1"));


        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);

        recording = makeOutputFile(prefs);
        if (recording == null) {
            recorder = null;
//            return START_STICKY;
        }

        Log.e("CallRecorder", "RecordService will config MediaRecorder with audiosource: " + " audioformat: " + audioformat);
        try {
            // These calls will throw exceptions unless you set the
            // android.permission.RECORD_AUDIO permission for your app
            recorder.reset();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                System.out.println("Present in MIC");
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            } else {
                recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            }
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            Log.e("CallRecorder", "set output " + audioformat);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            Log.e("CallRecorder", "set encoder default");
            recorder.setOutputFile(recording.getAbsolutePath());
            Log.e("CallRecorder", "set file: " + recording);
            //recorder.setMaxDuration(msDuration); //1000); // 1 seconds
            //recorder.setMaxFileSize(bytesMax); //1024*1024); // 1KB

            recorder.setOnInfoListener(this);
            recorder.setOnErrorListener(this);

            try {
                recorder.prepare();
            } catch (IOException e) {
                Log.e("CallRecorder", "RecordService::onStart() IOException attempting recorder.prepare()\n");
//                Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to start recording: " + e, Toast.LENGTH_LONG);
//                t.show();
                recorder = null;
                return START_STICKY;
            }
            Log.e("CallRecorder", "recorder.prepare() returned");

            recorder.start();
            isRecording = true;
            Log.e("CallRecorder", "recorder.start() returned");
//            updateNotification(true);
        } catch (Exception e) {
//            Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to start recording: " + e, Toast.LENGTH_LONG);
//            t.show();

            Log.e("CallRecorder", "RecordService::onStart caught unexpected exception", e);
            recorder = null;
        }

        return START_NOT_STICKY;
    }

    @SuppressLint("SdCardPath")
    public void onDestroy() {
        super.onDestroy();

        if (null != recorder) {
            Log.e("CallRecorder", "RecordService::onDestroy calling recorder.release()");
            isRecording = false;
            recorder.release();

            DialingInfo info = retriveCallSummary();

            ContentValues contentValues = new ContentValues();
            contentValues.put("id", String.valueOf(uniID));
            contentValues.put("time", time);
            contentValues.put("date", date);
            contentValues.put("number", info.getDialedNumber());
            contentValues.put("dialtime", info.getDialedTime());
            contentValues.put("recording", "/sdcard/.testrecorder/" + recording.getName());

            SQLiteDatabase writeableDatabase = DBHelper.dbHelper.getWritableDatabase();
            writeableDatabase.insert("callrecordings", null, contentValues);

            SQLiteDatabase readableDatabse = DBHelper.dbHelper.getReadableDatabase();

            Cursor cursor = readableDatabse.rawQuery("SELECT * FROM callrecordings", null);
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                do {
                    Log.e("Call Details", "Start");
                    Log.e("Call Details", "File Name: " + cursor.getString(cursor.getColumnIndex("recording")));
                    Log.e("Call Details", "Stop");
                    new UploadFileAsync(getBaseContext(), writeableDatabase).execute(cursor.getString(cursor.getColumnIndex("recording")),
                            cursor.getString(cursor.getColumnIndex("number")),
                            imei,
                            cursor.getString(cursor.getColumnIndex("dialtime")),
                            cursor.getString(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("date")),
                            cursor.getString(cursor.getColumnIndex("time")));
                } while (cursor.moveToNext());
            }

        }

//        updateNotification(false);
    }

    public DialingInfo retriveCallSummary() {
        DialingInfo info = null;
        @SuppressLint("MissingPermission") Cursor managedCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, android.provider.CallLog.Calls.DATE + " DESC limit 1;");
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int duration1 = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        if (managedCursor.moveToLast() == true) {
            info = new DialingInfo(managedCursor.getString(number), managedCursor.getString(duration1));
        }
        managedCursor.close();
        return info;
    }

    // methods to handle binding the service

    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean onUnbind(Intent intent) {
        return false;
    }

    public void onRebind(Intent intent) {
    }
//
//
//    private void updateNotification(Boolean status) {
//        Context c = getApplicationContext();
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
//
//        String ns = Context.NOTIFICATION_SERVICE;
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
//
//        Context context = getApplicationContext();
//        CharSequence contentText = "Recording call from channel...";
//        if (status) {
//            CharSequence tickerText = "Recording call from channel " + prefs.getString(Preferences.PREF_AUDIO_SOURCE, "1");
//            long when = System.currentTimeMillis();
//
//            Notification notification = new Notification(R.mipmap.ic_launcher, tickerText, when);
//
//            CharSequence contentTitle = "CallRecorder Status";
//            Intent notificationIntent = new Intent(this, RecordService.class);
//            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//
////            Toast.makeText(context, contentText, Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(context, contentText + "<<<  False", Toast.LENGTH_SHORT).show();
//        }
//    }

    // MediaRecorder.OnInfoListener
    public void onInfo(MediaRecorder mr, int what, int extra) {
        Log.e("CallRecorder", "RecordService got MediaRecorder onInfo callback with what: " + what + " extra: " + extra);
        isRecording = false;
    }

    // MediaRecorder.OnErrorListener
    public void onError(MediaRecorder mr, int what, int extra) {
        Log.e("CallRecorder", "RecordService got MediaRecorder onError callback with what: " + what + " extra: " + extra);
        isRecording = false;
        mr.release();
    }

    class DialingInfo {
        String dialedNumber, dialedTime;

        public DialingInfo(String dialedNumber, String dialedTime) {
            this.dialedNumber = dialedNumber;
            this.dialedTime = dialedTime;
        }

        public String getDialedNumber() {
            return dialedNumber;
        }

        public String getDialedTime() {
            return dialedTime;
        }
    }
}
