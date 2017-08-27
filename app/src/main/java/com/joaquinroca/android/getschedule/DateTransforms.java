package com.joaquinroca.android.getschedule;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
 * Created by joaquinroca on 8/22/17.
 */

class DateTransforms {

    static long dateAsMillisecondLong(String milliseconds) {
        return Long.parseLong(milliseconds);
    }

    static long dateAsMillisecondLong(Date nonMilliDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nonMilliDate);
        return calendar.getTimeInMillis();
    }

    static Date milliSecondToDate(String milliseconds) {
        Calendar calendar = Calendar.getInstance();
        long milliSeconds = Long.parseLong(milliseconds);
        calendar.setTimeInMillis(milliSeconds);
        return calendar.getTime();
    }

    static Date milliSecondToDate(Long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return calendar.getTime();
    }


    static String dateAsString(Date thisDate) {
        DateFormat formatter = new SimpleDateFormat("dd MMM yyyy 'at' hh:mm aaa z");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(thisDate);
        return formatter.format(calendar.getTime());
    }

    static String dateAsString(String milliseconds) {
        Date thisDate = milliSecondToDate(milliseconds);
        return dateAsString(thisDate);
    }

    static String dateAsString(Long milliseconds) {
        String stringMilliseconds = Long.toString(milliseconds);
        Date thisDate = milliSecondToDate(stringMilliseconds);
        return dateAsString(thisDate);
    }

}
