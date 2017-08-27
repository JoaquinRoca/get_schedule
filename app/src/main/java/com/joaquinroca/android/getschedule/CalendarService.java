package com.joaquinroca.android.getschedule;

/*
  Created by joaquinroca on 8/11/17.
*/



import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.RequiresApi;

import static android.provider.CalendarContract.*;
import static com.joaquinroca.android.getschedule.DateTransforms.milliSecondToDate;

/*
 * Created by David Laundav and contributed by Christian Orthmann
 *
 * Copyright 2013 Daivd Laundav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * References:
 * http://stackoverflow.com/questions/5883938/getting-events-from-calendar
 *
 * Please do not delete the references as they gave inspiration for the implementation
 */



abstract class CalendarService extends Context {

    // This is a projection for querying the calendar-provider for android
    // https://developer.android.com/guide/topics/providers/calendar-provider.html
    final private static String[] EVENT_PROJECTION = new String[] {
            Events._ID,                             // 0
            Events.CALENDAR_ID,                     // 1
            Events.ORGANIZER,                       // 2
            Events.TITLE,                           // 3
            Events.EVENT_LOCATION,                  // 4
            Events.DESCRIPTION,                     // 5
            Events.DTSTART,                         // 6
            Events.DTEND,                           // 7
            Events.ALL_DAY,                         // 8
    };


    // These are static references to the column numbers we will use in the cursor.
    // final private static int PROJECTION_ID_INDEX = 0;
    // final private static int PROJECTION_CALENDAR_ID_INDEX = 1;
    // final private static int PROJECTION_ORGANIZER_INDEX = 2;
    final private static int PROJECTION_TITLE_INDEX = 3;
    // final private static int PROJECTION_EVENT_LOCATION_INDEX = 4;
    // final private static int PROJECTION_DESCRIPTION_INDEX = 5;
    final private static int PROJECTION_DTSTART_INDEX = 6;
    final private static int PROJECTION_DTEND_INDEX = 7;
    final private static int PROJECTION_ALL_DAY = 8;


    final private static int HALF_HOUR_IN_MILLIS = 1800000;


    // Return a cursor for the half hour block given as start and end times
    // https://developer.android.com/reference/android/database/Cursor.html
    private static Cursor getEventCursor(
            ContentResolver contentResolver,
            String startTimeAsString,
            String endTimeAsString,
            Activity thisActivity
    ) {

        // we defined this in GetPermissions in order to check permissions for calendar
        GetPermissions.checkCalendarPermission(thisActivity);
        String[] selectionArgs = {startTimeAsString, endTimeAsString};
        String selection =
                EVENT_PROJECTION[PROJECTION_DTSTART_INDEX] + " >= ? AND " +
                        EVENT_PROJECTION[PROJECTION_DTSTART_INDEX] + " < ?";

        // https://developer.android.com/reference/android/content/ContentResolver.html#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
        // Query the given URI, returning a Cursor over the result set.
        // This returns a Cursor
        return contentResolver.query(
                Events.CONTENT_URI,
                // projection
                EVENT_PROJECTION,
                // selection
                selection,
                // selectionArgs
                selectionArgs,
                // sortOrder
                EVENT_PROJECTION[PROJECTION_DTSTART_INDEX] + " ASC"
        );


    }


    private static Boolean timeIsFree(
            ContentResolver contentResolver,
            String startTimeAsString,
            String endTimeAsString,
            Activity thisActivity
    ) {
        GetPermissions.checkCalendarPermission(thisActivity);
        String[] selectionArgs = {endTimeAsString, startTimeAsString, "false"};
        String selection =
                EVENT_PROJECTION[PROJECTION_DTSTART_INDEX] + " < ? AND " +
                        EVENT_PROJECTION[PROJECTION_DTEND_INDEX] + " > ? AND " +
                        EVENT_PROJECTION[PROJECTION_ALL_DAY] + " IS ?";

        // https://developer.android.com/reference/android/content/ContentResolver.html#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
        // Query the given URI, returning a Cursor over the result set.
        // This returns a Cursor
        Cursor evCursor = contentResolver.query(
                Events.CONTENT_URI,
                // projection
                EVENT_PROJECTION,
                // selection
                selection,
                // selectionArgs
                selectionArgs,
                // sortOrder
                EVENT_PROJECTION[PROJECTION_DTSTART_INDEX] + " ASC"
        );
        if (evCursor!= null && evCursor.getCount() > 0) {
            evCursor.close();
            return false;
        } else {
            return true;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static List<CalendarEvent> getCalendarEvents(Context context, Activity thisActivity) {

        List<CalendarEvent> calendarEventList = new ArrayList<>();

        // Find the current time to the nearest half hour (timeRightNow)
        Date now = new Date();
        Calendar c = new GregorianCalendar();
        c.setTime(now);
        int currentMinute = c.get(Calendar.MINUTE);
        if (currentMinute >= 0 && currentMinute < 30) {
            c.set(Calendar.MINUTE, 0);
        } else if (currentMinute >= 30 && currentMinute < 60) {
            c.set(Calendar.MINUTE, 30);
        }
        c.set(Calendar.SECOND, 0);
        long timeRightNow = c.getTimeInMillis();

        // Get events for each of the next XX time blocks
        int numberOfTimeBlocks = 48;
        int halfHoursPerBlock = 1;
        int lengthOfBlocks = halfHoursPerBlock * HALF_HOUR_IN_MILLIS;
        for (int i = 0; i < numberOfTimeBlocks; i++) {

            // 1800000 is 30 minutes in milliseconds
            // this will give a start time at the top of the half hour
            // and an end time at the bottom of the half hour
            long startTimeInMilliseconds = timeRightNow + (i * lengthOfBlocks);
            long endTimeInMilliseconds = startTimeInMilliseconds + lengthOfBlocks;
            Date begin = milliSecondToDate(startTimeInMilliseconds);
            Date end = milliSecondToDate(endTimeInMilliseconds);

            // REFACTOR for some reason the calendar wants a date one minute before the start and end
            String startTimeAsString = String.valueOf(startTimeInMilliseconds - 60000);
            String endTimeAsString = String.valueOf(endTimeInMilliseconds - 60000);

            String freeBusyStartTimeAsString = String.valueOf(startTimeInMilliseconds);
            String freeBusyEndTimeAsString = String.valueOf(endTimeInMilliseconds);

            // https://developer.android.com/reference/android/content/Context.html#getContentResolver()
            // Return a ContentResolver instance for your application's package.
            // https://developer.android.com/reference/android/content/ContentResolver.html
            // This class provides applications access to the content model.
            ContentResolver contentResolver = context.getContentResolver();
            Cursor eventCursor = getEventCursor(contentResolver, startTimeAsString, endTimeAsString, thisActivity);


            if (eventCursor != null && eventCursor.getCount() > 0) {

                while (eventCursor.moveToNext()) {
                    // create an event and add it to the list
                    String eventTitle = eventCursor.getString(PROJECTION_TITLE_INDEX);

                    long startDateInMilliseconds = eventCursor.getLong(PROJECTION_DTSTART_INDEX);
                    begin = milliSecondToDate(startDateInMilliseconds);

                    long endDateInMilliseconds = eventCursor.getLong(PROJECTION_DTEND_INDEX);
                    end = milliSecondToDate(endDateInMilliseconds);

                    CalendarEvent newEvent = new CalendarEvent(eventTitle, begin, end, false);
                    calendarEventList.add(newEvent);
                }
                eventCursor.close();
            } else {
                boolean timeIsFree = timeIsFree(contentResolver, freeBusyStartTimeAsString, freeBusyEndTimeAsString, thisActivity);
                if (timeIsFree) {
                    for (int eventNumber = 1; eventNumber < 3; eventNumber++) {
                        boolean hidden;

                        hidden = eventNumber != 1;

                        String eventTitle = "Placeholder event: " + String.valueOf(eventNumber);
                        CalendarEvent newEvent = new CalendarEvent(eventTitle, begin, end, true, hidden);
                        calendarEventList.add(newEvent);
                    }

                }

            }

        }
        return calendarEventList;

    }

}