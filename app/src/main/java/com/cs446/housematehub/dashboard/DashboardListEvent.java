package com.cs446.housematehub.dashboard;

import android.text.Html;
import android.text.Spanned;

import java.util.Date;

public class DashboardListEvent extends DashboardEvent {

    String groupListName;

    public DashboardListEvent(String groupListName, String eventOwnerName, String dateString, Date date) {
        super(eventOwnerName, dateString, date);
        this.groupListName = groupListName;
        this.type = DashboardEventType.GROUP_LIST;
    }

    @Override
    public Spanned getEventDescription() {
        return Html.fromHtml("The group list <b>" + groupListName + "</b> was created");
    }

}
