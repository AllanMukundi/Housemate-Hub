package com.cs446.housematehub.common;

import android.view.View;
import android.view.ViewGroup;

public class ViewUtils {

    /**
     * Recursively set visibility of all views and child views
     * @param view root view
     * @param show show or hide all views
     */
    public static void setViewsVisibility(View view, boolean show) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); ++i) {
                View child = viewGroup.getChildAt(i);
                child.setEnabled(show);
                child.setVisibility(show ? View.VISIBLE : View.GONE);
                setViewsVisibility(child, show);
            }
        }
    }

}
