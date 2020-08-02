package com.cs446.housematehub.common;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.parse.ParsePush;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationPublisher extends BroadcastReceiver {
    public static String NOTIFICATION_ALERT = "alert";
    public static String NOTIFICATION_TITLE = "title";
    public void onReceive (Context context, Intent intent) {
        //parse notification
        JSONObject data = new JSONObject();

        // Put data in the JSON object
        try {
            data.put("alert", intent.getStringExtra(NOTIFICATION_ALERT));
            data.put("title", intent.getStringExtra(NOTIFICATION_TITLE));
        } catch ( JSONException e) {
            // should not happen
            throw new IllegalArgumentException("unexpected parsing error", e);
        }

        ParsePush push = new ParsePush();
        push.setChannel("main_channel");
        push.setData(data);
        push.sendInBackground();
    }
}