package com.cs446.housematehub.dashboard;

import android.text.Spanned;

import java.util.Date;

public abstract class DashboardEvent {

    String dateString;
    Date date;
    String eventOwnerName;
    DashboardEventType type;
    public DashboardEvent(String eventOwnerName, String dateString, Date date) {
        this.eventOwnerName = eventOwnerName;
        this.dateString = dateString;
        this.date = date;
    }

    public abstract Spanned getEventDescription();

    enum DashboardEventType {
        EXPENSE,
        CALENDAR,
        GROUP_LIST,
    }

}
