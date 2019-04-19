package com.mjniuz.bido.bidoblinddescriptor;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class RecordAudio{
    private MediaRecorder recorder = null;
    /** Called when the activity is first created. */

    void startRecording(String outputFile){
        int output_formats = MediaRecorder.OutputFormat.MPEG_4;
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(outputFile);
        //recorder.setAudioSamplingRate(5);  // 44.1 khz
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Warning: " + what + ", " + extra);
        }
    };

    public boolean stopRecording(){
        if(null != recorder){
            recorder.stop();
            Log.d("AUDIOKU", "FINISH");

            recorder.release();
            recorder.reset();

            recorder = null;

            return true;
        }

        return false;
    }
}
