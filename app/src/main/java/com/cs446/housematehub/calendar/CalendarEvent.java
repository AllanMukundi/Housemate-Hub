package com.cs446.housematehub.calendar;

import java.util.Date;

public class CalendarEvent {
    public int id;
    public String eventName;
    public EventType eventType;
    public NotificationType notificationType;
    public RepeatType repeatType;
    public Date startDate;
    public Date endDate;
    public boolean allDay;
    public String houseName;
    public String userCreated;

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

}
