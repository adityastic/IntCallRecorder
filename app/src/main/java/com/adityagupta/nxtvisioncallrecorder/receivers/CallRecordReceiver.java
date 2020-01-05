package com.adityagupta.nxtvisioncallrecorder.receivers;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.adityagupta.nxtvisioncallrecorder.utils.Preferences;
import com.adityagupta.nxtvisioncallrecorder.utils.asyncs.UploadFileAsync;
import com.adityagupta.nxtvisioncallrecorder.utils.sqlite.DBHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by aditya on 19.12.2018.
 */
public class CallRecordReceiver extends PhoneCallReceiver {

    private static final String TAG = CallRecordReceiver.class.getSimpleName();
    static long starttime;
    static long endtime;
    static String date, time;
    static int uniID;
    static String imei;
    private static MediaRecorder recorder;
    private static File audiofile;
    private static boolean isRecordStarted = false;

    @Override
    protected void onIncomingCallReceived(Context context, String number, Date start) {
    }

    @Override
    protected void onIncomingCallAnswered(Context context, String number, Date start) {
        startRecord(context, "incoming", number);
    }

    @Override
    protected void onIncomingCallEnded(Context context, String number, Date start, Date end) {
        stopRecord(context, number);
    }

    @Override
    protected void onOutgoingCallStarted(Context context, String number, Date start) {
        startRecord(context, "outgoing", number);
    }

    @Override
    protected void onOutgoingCallEnded(Context context, String number, Date start, Date end) {
        stopRecord(context, number);
    }

    @Override
    protected void onMissedCall(Context context, String number, Date start) {

    }

    // Derived classes could override these to respond to specific events of interest
    protected void onRecordingStarted(Context context, File audioFile) {
    }

    protected void onRecordingFinished(Context context, File audioFile) {
    }

    private void startRecord(Context context, String seed, String phoneNumber) {
        try {
            if (isRecordStarted) {
                try {
                    recorder.stop();  // stop the recording
                } catch (RuntimeException e) {
                    // RuntimeException is thrown when stop() is called immediately after start().
                    // In this case the output file is not properly constructed ans should be deleted.
                    Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                    //noinspection ResultOfMethodCallIgnored
                    audiofile.delete();
                }
                releaseMediaRecorder();
                isRecordStarted = false;
            } else {
                startWifi(context);
                checkDatabase(context);

                if (prepareAudioRecorder(context, seed, phoneNumber)) {
                    recorder.start();
                    isRecordStarted = true;
                    onRecordingStarted(context, audiofile);

                    starttime = Calendar.getInstance().getTime().getTime();

                    Log.i(TAG, "record start");
                } else {
                    releaseMediaRecorder();
                }
                //new MediaPrepareTask().execute(null, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
        }
    }

    private void checkDatabase(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        DBHelper.dbHelper = new DBHelper(context);

        if (prefs.getBoolean("scoreboard", true)) {

            SQLiteDatabase writeableDatabase = DBHelper.dbHelper.getWritableDatabase();
            writeableDatabase.execSQL("CREATE TABLE IF NOT EXISTS callrecordings (id TEXT,time TEXT,date TEXT,number TEXT,dialtime TEXT,recording TEXT)");
            prefs.edit().putBoolean("scoreboard", false).apply();

        }
    }

    private void startWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
    }

    private void stopRecord(Context context, String number) {
        try {
            if (recorder != null && isRecordStarted) {
                releaseMediaRecorder();
                isRecordStarted = false;
                onRecordingFinished(context, audiofile);

                endtime = Calendar.getInstance().getTime().getTime();
                long timeDiff = (endtime - starttime) / 1000;

                storeCallInDatabase(context, timeDiff, number);


                Log.i(TAG, "record stop");
            }
        } catch (Exception e) {
            releaseMediaRecorder();
            e.printStackTrace();
        }
    }

    private void storeCallInDatabase(Context context, long timeDiff, String number) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("id", String.valueOf(uniID));
        contentValues.put("time", time);
        contentValues.put("date", date);
        contentValues.put("number", number);
        contentValues.put("dialtime", (int) timeDiff);
        contentValues.put("recording", "/sdcard/.nxtvision/" + audiofile.getName());

        SQLiteDatabase writeableDatabase = DBHelper.dbHelper.getWritableDatabase();
        writeableDatabase.insert("callrecordings", null, contentValues);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("allowed", false)) {
            SQLiteDatabase readableDatabse = DBHelper.dbHelper.getReadableDatabase();

            Cursor cursor = readableDatabse.rawQuery("SELECT * FROM callrecordings", null);
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                do {
                    Log.e("Call Details", "Start");
                    Log.e("Call Details", "File Name: " + cursor.getString(cursor.getColumnIndex("recording")));
                    Log.e("Call Details", "Stop");
                    new UploadFileAsync(context).execute(cursor.getString(cursor.getColumnIndex("recording")),
                            cursor.getString(cursor.getColumnIndex("number")),
                            imei,
                            cursor.getString(cursor.getColumnIndex("dialtime")),
                            cursor.getString(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("date")),
                            cursor.getString(cursor.getColumnIndex("time")));
                } while (cursor.moveToNext());
            }
        } else {
            Log.e("License Expired", "TRUE");
        }
    }
//
//    public DialingInfo retriveCallSummary(Context context) {
//        DialingInfo info = null;
//        @SuppressLint("MissingPermission") Cursor managedCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
//                null, null, null, android.provider.CallLog.Calls.DATE + " DESC limit 1;");
//        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
//        int duration1 = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
//        if (managedCursor.moveToLast() == true) {
//            Log.e("Number and Duration", managedCursor.getString(number) + ", " + managedCursor.getString(duration1));
//            info = new DialingInfo(managedCursor.getString(number), managedCursor.getString(duration1));
//        }
//        managedCursor.close();
//        return info;
//    }

    private boolean prepareAudioRecorder(Context context, String seed, String phoneNumber) {
        try {
            String dir_path = ".nxtvision";
            String dir_name = Environment.getExternalStorageDirectory().getPath();
            int output_format = MediaRecorder.OutputFormat.MPEG_4;
            int audio_source = MediaRecorder.AudioSource.VOICE_CALL;
            int audio_encoder = MediaRecorder.AudioEncoder.HE_AAC;

            File sampleDir = new File(dir_name + "/" + dir_path);

            Date d = new Date();
            date = new SimpleDateFormat("dd-MM-yyyy").format(d);
            time = new SimpleDateFormat("HH:mm:ss").format(d);
            uniID = (int) ((Math.random() * 1000000) + 1);
            imei = Preferences.getIMEI(context);

            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }


            String prefix = imei.substring(imei.length() - 4) + "_" + uniID + "_" + date + "_" + time;

            String suffix = "";
            switch (output_format) {
                case MediaRecorder.OutputFormat.AMR_NB: {
                    suffix = ".amr";
                    break;
                }
                case MediaRecorder.OutputFormat.AMR_WB: {
                    suffix = ".amr";
                    break;
                }
                case MediaRecorder.OutputFormat.MPEG_4: {
                    suffix = ".mp4";
                    break;
                }
                case MediaRecorder.OutputFormat.THREE_GPP: {
                    suffix = ".3gp";
                    break;
                }
                default: {
                    suffix = ".amr";
                    break;
                }
            }
            Log.e("File Information", sampleDir + "/" + prefix + suffix);
            audiofile = new File(sampleDir + "/" + prefix + suffix);

            recorder = new MediaRecorder();
            recorder.setAudioSource(audio_source);
            recorder.setOutputFormat(output_format);
            recorder.setAudioEncoder(audio_encoder);
            recorder.setOutputFile(audiofile.getAbsolutePath());
            recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mediaRecorder, int i, int i1) {

                }
            });

            try {
                recorder.prepare();
            } catch (IllegalStateException e) {
                Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
                releaseMediaRecorder();
                return false;
            } catch (IOException e) {
                Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
                releaseMediaRecorder();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void releaseMediaRecorder() {
        if (recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }
}
