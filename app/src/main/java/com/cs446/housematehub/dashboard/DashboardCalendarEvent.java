package com.cs446.housematehub.dashboard;

import android.text.Html;
import android.text.Spanned;

import com.cs446.housematehub.calendar.EventType;

import java.util.Date;

public class DashboardCalendarEvent extends DashboardEvent {

    String eventName;
    EventType eventType;

    public DashboardCalendarEvent(String eventName, EventType eventType, String eventOwnerName, String dateString, Date date) {
        super(eventOwnerName, dateString, date);
        this.eventName = eventName;
        this.eventType = eventType;
        this.type = DashboardEventType.CALENDAR;
    }

    @Override
    public Spanned getEventDescription() {
        String action = this.eventType.equals(EventType.RESERVE_AMENITY) ? "reserved" : "scheduled";
        return Html.fromHtml(String.format("<b>%s</b> %s for %s", eventName, action, dateString));
    }
}
