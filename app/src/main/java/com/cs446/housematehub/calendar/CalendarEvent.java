package com.cs446.housematehub.calendar;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class CalendarEvent implements Comparable<CalendarEvent>, Serializable {
    public int id;
    public String eventName;
    public EventType eventType;
    public NotificationType notificationType;
    public RepeatType repeatType;
    public Date originalStartDate;
    public Date startDate;
    public Date endDate;
    public boolean allDay;
    public String houseName;
    public String userCreated;
    // Used for displaying an event
    public boolean showHeader = false;
    public boolean showDate = false;

    public CalendarEvent(int id, String eventName, EventType eventType, NotificationType notificationType, RepeatType repeatType, Date startDate, Date endDate, boolean allDay, String houseName, String userCreated) {
        this.id = id;
        this.eventName = eventName;
        this.eventType = eventType;
        this.notificationType = notificationType;
        this.repeatType = repeatType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.allDay = allDay;
        this.houseName = houseName;
        this.userCreated = userCreated;
    }

    @Override
    public int compareTo(CalendarEvent e) {
        Calendar cThis = Calendar.getInstance();
        Calendar cE = Calendar.getInstance();
        cThis.setTime(this.startDate);
        cE.setTime(e.startDate);

        if (cThis.get(Calendar.DAY_OF_MONTH) == cE.get(Calendar.DAY_OF_MONTH) && cThis.get(Calendar.YEAR) == cE.get(Calendar.YEAR)) {
            if (this.allDay && e.allDay) {
                return 0;
            } else if(this.allDay) {
                return -1;
            } else if(e.allDay) {
                return 1;
            }
        }
        return this.startDate.compareTo(e.startDate);
    }
}
