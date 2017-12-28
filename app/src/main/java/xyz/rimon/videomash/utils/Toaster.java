package xyz.rimon.videomash.utils;

import android.content.Context;
import android.widget.Toast;

import xyz.rimon.videomash.config.Config;

/**
 * Created by SAyEM on 29/12/17.
 */

public class Toaster {
    private Toaster(){}

    public static void toast(Context context, String message) {
        if (Config.DEBUG)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
