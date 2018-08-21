package com.adityagupta.intern2.recievers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.util.Log;

import com.adityagupta.intern2.services.RecordService;
import com.adityagupta.intern2.utils.Preferences;
import com.adityagupta.intern2.utils.asyncs.UploadFileAsync;
import com.adityagupta.intern2.utils.sqlite.DBHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CallRecordReceiver extends PhoneCallReceiver {

    @Override
    protected void onIncomingCallReceived(Context context, String number, Date start) {

    }

    @Override
    protected void onIncomingCallAnswered(Context context, String number, Date start) {
        context.startService(new Intent(context, RecordService.class));
    }

    @Override
    protected void onIncomingCallEnded(Context context, String number, Date start, Date end) {
        context.stopService(new Intent(context, RecordService.class));
    }

    @Override
    protected void onOutgoingCallStarted(Context context, String number, Date start) {
        context.startService(new Intent(context, RecordService.class));
    }

    @Override
    protected void onOutgoingCallEnded(Context context, String number, Date start, Date end) {
        context.stopService(new Intent(context, RecordService.class));
    }

    @Override
    protected void onMissedCall(Context context, String number, Date start) {

    }
}
