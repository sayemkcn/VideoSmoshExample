package xyz.rimon.videomash.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by SAyEM on 29/12/17.
 */

public class DateUtil {
    private DateUtil() {
    }

    public DateFormat getDateTimeFormat() {
        return new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", Locale.getDefault());
    }
}
