package com.faltenreich.diaguard.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.faltenreich.diaguard.DiaguardApplication;
import com.faltenreich.diaguard.R;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by Faltenreich on 23.03.2016.
 */
public class DateTimeUtils {

    public static String toWeekDayShort(DateTime dateTime, boolean withDot) {
        String weekDay = dateTime.toString("E");
        if (!withDot && weekDay.length() >= 1) {
            weekDay = weekDay.substring(0, weekDay.length() - 1);
        }
        return weekDay;
    }

    public static String toWeekDayShort(DateTime dateTime) {
        return toWeekDayShort(dateTime, false);
    }

    public static String toDayAndMonth(DateTime dateTime) {
        return String.format("%s.%s.",
                dateTime.toString("dd"),
                dateTime.toString("MM"));
    }

    @SuppressLint("DefaultLocale")
    public static String parseInterval(long intervalInMillis) {
        Context context = DiaguardApplication.getContext();
        if (intervalInMillis > (DateTimeConstants.MILLIS_PER_DAY * 2)) {
            return String.format("%d %s",
                    intervalInMillis / DateTimeConstants.MILLIS_PER_DAY,
                    context.getString(R.string.days));
        } else if (intervalInMillis > (DateTimeConstants.MILLIS_PER_HOUR * 2)) {
            return String.format("%d %s",
                    intervalInMillis / DateTimeConstants.MILLIS_PER_HOUR,
                    context.getString(R.string.hours));
        } else if (intervalInMillis > (DateTimeConstants.MILLIS_PER_MINUTE * 2)) {
            return String.format("%d %s",
                    intervalInMillis / DateTimeConstants.MILLIS_PER_MINUTE,
                    context.getString(R.string.minutes));
        } else {
            return String.format("1 %s",
                    context.getString(R.string.minute));
        }
    }

    public static DateTime parseFromString(String text, String format) {
        return TextUtils.isEmpty(text) && format.isEmpty() ?
                DateTimeFormat.forPattern(format).parseDateTime(text) :
                null;
    }
}
