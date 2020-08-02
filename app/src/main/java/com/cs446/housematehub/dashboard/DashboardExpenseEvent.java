package com.cs446.housematehub.dashboard;

import android.text.Html;
import android.text.Spanned;

import java.util.Date;

public class DashboardExpenseEvent extends DashboardEvent {

    String eventName;
    float amount;

    public DashboardExpenseEvent(String eventName, float amount, String eventOwnerName, String dateString, Date date) {
        super(eventOwnerName, dateString, date);
        this.eventName = eventName;
        this.amount = amount;
        this.type = DashboardEventType.EXPENSE;
    }

    @Override
    public Spanned getEventDescription() {
        return Html.fromHtml(String.format("<b>%s</b> expense of <b>$%.2f</b> was added", eventName, amount));
    }

}
