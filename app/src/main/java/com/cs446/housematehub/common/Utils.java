package com.cs446.housematehub.common;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.Calendar;
import java.util.Date;

public final class Utils {

    // Convert DP to PX for margin etc
    public static int getPX(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int px = (int) Math.ceil(dp * scale);
        return px;
    }

    // from https://stackoverflow.com/questions/40861136/set-listview-height-programmatically
    public static void updateListViewHeight(ListView myListView) {
        ListAdapter myListAdapter = myListView.getAdapter();
        if (myListAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int adapterCount = myListAdapter.getCount();
        for (int size = 0; size < adapterCount; size++) {
            View listItem = myListAdapter.getView(size, null, myListView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = myListView.getLayoutParams();
        params.height = (totalHeight + (myListView.getDividerHeight() * (adapterCount)));
        myListView.setLayoutParams(params);
    }

    public static Date oneDayAgo() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int daysAgo = 1;
        cal.add(Calendar.DATE, -daysAgo);
        return cal.getTime();
    }
}
