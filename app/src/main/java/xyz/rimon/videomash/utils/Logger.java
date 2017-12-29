package xyz.rimon.videomash.utils;

import android.util.Log;

import xyz.rimon.videomash.config.Config;

/**
 * Created by SAyEM on 29/12/17.
 */

public class Logger {
    private static final String TAG = "Logger";
    private Logger() {
    }

    public static void i(String str) {
        if (Config.DEBUG)
            Log.i(TAG,str);
    }

    public static void e(String str) {
        if (Config.DEBUG)
            Log.e(TAG,str);
    }

    public static void d(String str) {
        if (Config.DEBUG)
            Log.d(TAG,str);
    }
}
