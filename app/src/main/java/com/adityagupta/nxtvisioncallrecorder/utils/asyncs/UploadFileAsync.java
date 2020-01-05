package com.adityagupta.nxtvisioncallrecorder.utils.asyncs;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.adityagupta.nxtvisioncallrecorder.R;
import com.adityagupta.nxtvisioncallrecorder.utils.sqlite.DBHelper;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UploadFileAsync extends AsyncTask<String, Void, String> {

    Context context;

    public UploadFileAsync(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {

        try {

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024 * 1024;
            File sourceFile = new File(params[0]);

            if (sourceFile.isFile()) {

                try {
                    String uri = context.getString(R.string.callrecorder__php_link);
                    String upLoadServerUri = Uri.parse(uri)
                            .buildUpon()
                            .appendQueryParameter("call_to", params[1])
                            .appendQueryParameter("device_id", params[2])
                            .appendQueryParameter("duration", params[3])
                            .appendQueryParameter("unique_call_id", params[4])
                            .appendQueryParameter("date", params[5])
                            .appendQueryParameter("time", params[6])
                            .build().toString();

                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(
                            sourceFile);
                    URL url = new URL(upLoadServerUri);

                    // Open a HTTP connection to the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE",
                            "multipart/form-data");
                    conn.setRequestProperty("Content-Type",
                            "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("bill", params[0]);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"recording\";filename=\""
                            + params[0] + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math
                                .min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0,
                                bufferSize);

                    }

                    // send multipart form data necesssary after file
                    // data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens
                            + lineEnd);

                    // Responses from the server (code and message)
                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn
                            .getResponseMessage();

                    if (serverResponseCode == 200) {

                        // messageText.setText(msg);
                        Log.e("FileUpload Success", "YES" + serverResponseMessage);
                        // recursiveDelete(mDirectory1);

                    }

                    StringBuffer output = new StringBuffer();

                    InputStream in = null;
                    try {
                        in = conn.getInputStream();
                        byte[] bu = new byte[1024];
                        int read;
                        while ((read = in.read(bu)) > 0) {
                            output.append(new String(bu, 0, read, StandardCharsets.UTF_8));
                        }
                    } finally {
                        in.close();
                    }

                    Log.e("Reponse", output.toString());

                    JSONObject jsonObject = new JSONObject(output.toString());
                    if (jsonObject.getString("code").equals("200")) {

                        DBHelper.dbHelper.getWritableDatabase().delete("callrecordings", "id=?", new String[]{jsonObject.getString("id")});

                        if (sourceFile.exists()) {
                            if (sourceFile.delete())
                                Log.e("DELETEDFILE", "YES: " + params[0]);
                            else
                                Log.e("DELETEDFILE", "NO: " + params[0]);
                        }
                    }


                    // close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                } catch (Exception e) {

                    // dialog.dismiss();
                    e.printStackTrace();

                }
                // dialog.dismiss();

            } // End else block


        } catch (Exception ex) {
            // dialog.dismiss();

            ex.printStackTrace();
        }
        return "Executed";
    }

    @Override
    protected void onPostExecute(String result) {

    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
}