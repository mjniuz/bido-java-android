package com.mjniuz.bido.bidoblinddescriptor;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Alan El on 8/16/2017.
 */

public class HttpRequest extends AsyncTask<String, Void, String> {
    final HttpRequest context = this;
    public String result = "none";
    public File file;
    private static final String LINE_FEED = "\r\n";

    protected void onPreExecute() {
        super.onPreExecute();
    }


    protected String doInBackground(String... passing) {
        // for post file only
        String param    = (String) passing[0];
        String dirFile  = (String) passing[1];

        File sourceFile = new File("");
        if(dirFile != ""){
            sourceFile = new File(dirFile);
        }

        // test retrofit

        try {
            String boundary = "===" + System.currentTimeMillis() + "===";
            String charset = "UTF-8";
            URL url = new URL("https://dipo.mjniuz.com/api/" + param);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("token","alan-1122");
            conn.setRequestProperty("User-Agent", "CodeJava Agent");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            OutputStream outputStream;
            PrintWriter writer;
            outputStream = conn.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                    true);

            // file hanlde
            Log.d("HASFILE", String.valueOf(sourceFile.isFile()));

            if (sourceFile.isFile()) {
                String fieldName = "file";
                String fileName = sourceFile.getName();
                writer.append("--" + boundary).append(LINE_FEED);
                writer.append(
                        "Content-Disposition: form-data; name=\"" + fieldName
                                + "\"; filename=\"" + fileName + "\"")
                        .append(LINE_FEED);
                writer.append(
                        "Content-Type: "
                                + URLConnection.guessContentTypeFromName(fileName))
                        .append(LINE_FEED);
                writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                writer.append(LINE_FEED);
                writer.flush();

                FileInputStream inputStream = new FileInputStream(sourceFile);
                byte[] buffer = new byte[4096];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                inputStream.close();

                writer.append(LINE_FEED);
                writer.flush();
            }

            writer.append(LINE_FEED).flush();
            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();

            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                result = sb.toString();

                conn.disconnect();
            } else {
                throw new IOException("Server returned non-OK status: " + status);
            }
        } catch (Exception e) {
            e.printStackTrace();

            return e.toString();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {

    }
}