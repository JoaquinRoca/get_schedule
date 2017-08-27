package com.joaquinroca.android.getschedule;

/*
  Created by joaquinroca on 8/11/17.
  inspired by https://github.com/david-laundav/Android-CalendarService
 */

import android.support.annotation.NonNull;

import java.util.Date;


/**
    * This class describes a calendar event.
    *
    * @author  Joaquin Roca
    * @see Comparable
    *
*/

 class CalendarEvent implements Comparable<CalendarEvent> {


    private String title;
    private String description;
    private String location;
    private Date begin;
    private Date end;
//    private boolean allDay;
    private boolean placeholder;
    private boolean hidden;


    CalendarEvent(String title, Date begin, Date end, boolean placeholder) {
        setTitle(title);
        setDescription(null);
        setLocation((null));
        setBegin(begin);
        setEnd(end);
//        setAllDay(false);
        setPlaceholder(placeholder);
    }

    CalendarEvent(String title, Date begin, Date end, boolean placeholder, boolean hidden) {
        setTitle(title);
        setDescription(null);
        setLocation((null));
        setBegin(begin);
        setEnd(end);
//        setAllDay(false);
        setPlaceholder(placeholder);
        setHidden(hidden);
    }

    private String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    private String getLocation() {
        return location;
    }

    private void setLocation(String location) {
        this.location = location;
    }

    private String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    Date getBegin() {
        return begin;
    }

    private void setBegin(Date begin) {
        this.begin = begin;
    }

    private Date getEnd() {
        return end;
    }

    private void setEnd(Date end) {
        this.end = end;
    }

//    public boolean isAllDay() {
//        return allDay;
//    }

//    private void setAllDay(boolean allDay) {
//        this.allDay = allDay;
//    }

    boolean isPlaceholder() {
        return placeholder;
    }

    private void setPlaceholder(boolean placeholder) {
        this.placeholder = placeholder;
    }

    boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }


    @Override
    public String toString() {
        String eventLocation = getLocation();
        String locationPart;
        String eventTitle = getTitle();
        String titlePart;
        String eventDescription = getDescription();
        String descriptionPart = null;
        Date eventTimeString = getBegin();
        String timePart;
        Date eventEndTimeString = getEnd();
        String endTimePart;
        boolean placeholder = isPlaceholder();

        if (placeholder) {
            return eventTitle;
        } else if (eventLocation != null && !eventLocation.isEmpty()) {
            locationPart = " Location: " + eventLocation;
        } else {
            locationPart = " No location found";
        }

        if (eventTitle != null && !eventTitle.isEmpty()) {
            titlePart = eventTitle;
        } else {
            titlePart = "Unnamed event";
        }

        if (eventDescription != null && !eventDescription.isEmpty()) {
            descriptionPart = " Here's a short description: " + eventDescription;
        }

        if (eventTimeString != null) {
            timePart = " beginning at " + eventTimeString;
        } else {
            timePart = " beginning at an unknown time.";
        }

        if (eventEndTimeString != null) {
            endTimePart = " until " + eventEndTimeString + ".";
        } else {
            endTimePart = " until an unknown time.";
        }

        if (descriptionPart != null) {
            return titlePart + timePart + endTimePart + locationPart + "." + descriptionPart;
        } else {
            return titlePart + timePart + endTimePart + locationPart + ".";
        }

    }

    @Override
    public int compareTo(@NonNull CalendarEvent other) {
        // -1 = less, 0 = equal, 1 = greater
        return getBegin().compareTo(other.begin);
    }

}