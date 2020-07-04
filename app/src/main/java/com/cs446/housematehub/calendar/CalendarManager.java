package com.cs446.housematehub.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cs446.housematehub.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;

public class CalendarManager extends Fragment {

    private View view;
    private FloatingActionButton addEventButton;

    public CalendarManager() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_calendar_manager, container, false);
        addEventButton = view.findViewById(R.id.addButton);

        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarDialog dialog = new CalendarDialog();
                dialog.show(getFragmentManager(), "CalendarDialog");
            }
        });
        return view;
    }
}
