package com.cs446.housematehub;

import android.content.Context;

public final class Utils {

    // Convert DP to PX for margin etc
    public static int getPX(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int px = (int) Math.ceil(dp * scale);
        return px;
    }
}
