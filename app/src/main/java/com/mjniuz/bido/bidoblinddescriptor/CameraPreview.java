package com.mjniuz.bido.bidoblinddescriptor;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Alan El on 8/16/2017.
 */

public class CameraPreview extends AsyncTask<Void, Void, String> {
    byte[] data;
    public CameraPreview(byte[] pict){
        this.data = pict;
    }

    @Override
    protected String doInBackground(Void... arg0) {
        FileOutputStream outStream = null;

        // Write to SD Card
        try {
            File secStore = Environment.getExternalStorageDirectory();
            File dir = new File (secStore + "/DCIM/DIPO");

            Log.d("DIRECTORY", String.valueOf(secStore));
            dir.mkdirs();

            String fileName ="image-temp.jpg";
            File outFile = new File(dir, fileName);

            outStream = new FileOutputStream(outFile);
            outStream.write(data);
            outStream.flush();
            outStream.close();

            Log.d("PICTURETAKEN", "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

            refreshGallery(outFile);

            return outFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return null;
    }


    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        //sendBroadcast(mediaScanIntent);
    }
}