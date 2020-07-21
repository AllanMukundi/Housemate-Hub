package com.cs446.housematehub.calendar;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.cs446.housematehub.HouseMainActivity;
import com.cs446.housematehub.R;
import com.cs446.housematehub.account.AccountDetails;
import com.cs446.housematehub.expenses.ExpenseLog;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;

public class EventPage extends Fragment {

    private CalendarEvent event;
    private View view;

    public EventPage(CalendarEvent event) {
        this.event = event;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_event_page, container, false);

        ((HouseMainActivity) getActivity()).enableBack();



        return view;
    }
}
