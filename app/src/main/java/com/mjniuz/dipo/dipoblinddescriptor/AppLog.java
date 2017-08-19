package com.mjniuz.dipo.dipoblinddescriptor;

import android.util.Log;

/**
 * Created by Alan El on 8/18/2017.
 */

public class AppLog {
    private static final String APP_TAG = "AudioRecorder";

    public static int logString(String message) {
        return Log.i(APP_TAG, message);
    }
}
