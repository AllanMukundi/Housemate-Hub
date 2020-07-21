package com.cs446.housematehub.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.cs446.housematehub.HouseMainActivity;
import com.cs446.housematehub.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.fragment.app.DialogFragment;

public class CalendarDialog extends DialogFragment {

    EditText eventName;
    Switch allDaySwitch;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    Spinner eventTypeSpinner;
    Spinner repeatTypeSpinner;
    Spinner notificationTypeSpinner;
    Button dayStartButton;
    Button dayEndButton;
    Button timeStartButton;
    Button timeEndButton;

    Calendar start;
    Calendar end;

    boolean all_day = false;

    Calendar rightNow;
    final SimpleDateFormat dateFormatter = new SimpleDateFormat("E, MMM dd, yyyy");
    final SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");

    public CalendarDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_dialog, container, false);

        rightNow = Calendar.getInstance();

        eventName = view.findViewById(R.id.event_name_title);
        eventTypeSpinner = view.findViewById(R.id.event_type_spinner);
        allDaySwitch = view.findViewById(R.id.all_day_switch);
        dayStartButton = view.findViewById(R.id.date_start_button);
        dayEndButton = view.findViewById(R.id.date_end_button);
        timeStartButton = view.findViewById(R.id.time_start_button);
        timeEndButton = view.findViewById(R.id.time_end_button);
        eventTypeSpinner = view.findViewById(R.id.event_type_spinner);
        repeatTypeSpinner = view.findViewById(R.id.repeat_type_spinner);
        notificationTypeSpinner = view.findViewById(R.id.notification_type_spinner);

        start = (Calendar) rightNow.clone();
        start.set(Calendar.MINUTE, 0);
        dayStartButton.setText(dateFormatter.format(start.getTime()));
        timeStartButton.setText(timeFormatter.format(start.getTime()));

        rightNow.add(Calendar.HOUR_OF_DAY, 1);
        end = (Calendar) rightNow.clone();
        end.set(Calendar.MINUTE, 0);
        dayEndButton.setText(dateFormatter.format(end.getTime()));
        timeEndButton.setText(timeFormatter.format(end.getTime()));

        initDatePickerListener(dayStartButton, start);
        initDatePickerListener(dayEndButton, end);
        initTimePickerListener(timeStartButton, start);
        initTimePickerListener(timeEndButton, end);

        allDaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                all_day = isChecked;
                if (isChecked) {
                    timeStartButton.setVisibility(View.INVISIBLE);
                    timeEndButton.setVisibility(View.INVISIBLE);
                } else {
                    timeStartButton.setVisibility(View.VISIBLE);
                    timeEndButton.setVisibility(View.VISIBLE);
                }
            }
        });

        initSpinner(EventType.getValues(), eventTypeSpinner);
        initSpinner(RepeatType.getValues(), repeatTypeSpinner);
        initSpinner(NotificationType.getValues(), notificationTypeSpinner);

        Button cancelButton = view.findViewById(R.id.cancel_button);
        Button createButton = view.findViewById(R.id.create_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((start.compareTo(end) > 0 && !all_day) || (start.compareTo(end) > 0 && all_day && start.get(Calendar.DAY_OF_MONTH) != end.get(Calendar.DAY_OF_MONTH))) {
                    Toast.makeText(getActivity(), "Event start time must be before end time", Toast.LENGTH_LONG).show();
                } else if (eventName.getText().toString().matches("")) {
                    Toast.makeText(getActivity(), "Event must have a title", Toast.LENGTH_SHORT).show();
                } else {
                    createCalendarEvent();
                }
            }
        });

        return view;
    }

    private void initDatePickerListener(final Button b, final Calendar date) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                date.set(year, month, day);
                                b.setText(dateFormatter.format(date.getTime()));
                            }
                        }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
    }

    private void initTimePickerListener(final Button b, final Calendar date) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog = new TimePickerDialog(getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), hourOfDay, minute, 0);
                                b.setText(timeFormatter.format(date.getTime()));
                            }
                        }, date.get(Calendar.HOUR), date.get(Calendar.MINUTE), false);
                timePickerDialog.show();
            }
        });
    }

    private void initSpinner(List<String> s, Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, s);
        spinner.setAdapter(adapter);
    }

    public void createCalendarEvent() {
        List<ParseObject> houseRes = new ArrayList<ParseObject>();
        ParseObject house;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("House");
        final String houseName = (String) ((HouseMainActivity) getActivity()).getCurrentHouse().get("houseName");
        final ParseUser currentUser = ((HouseMainActivity) getActivity()).getCurrentUser();
        query.whereEqualTo("houseName", houseName);

        try {
            houseRes = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (houseRes.size() == 0) {
            throw new RuntimeException("House with the given houseName was not found");
        }

        house = houseRes.get(0); // should only be one matching row
        final int id = (int) house.get("nextEventId");

        final CalendarEvent calendarEvent = new CalendarEvent(
                id,
                eventName.getText().toString(),
                EventType.getEnum(eventTypeSpinner.getSelectedItem().toString()),
                NotificationType.getEnum(notificationTypeSpinner.getSelectedItem().toString()),
                RepeatType.getEnum(repeatTypeSpinner.getSelectedItem().toString()),
                start.getTime(),
                end.getTime(),
                all_day,
                houseName,
                currentUser.getUsername()
        );

        house.put("nextEventId", id + 1);
        house.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                ParseObject calendarEntity = new ParseObject("Calendar");
                calendarEntity.put("eventId", calendarEvent.id);
                calendarEntity.put("eventName", calendarEvent.eventName);
                calendarEntity.put("eventType", calendarEvent.eventType.ordinal());
                calendarEntity.put("notificationType", calendarEvent.notificationType.ordinal());
                calendarEntity.put("repeatType", calendarEvent.repeatType.ordinal());
                calendarEntity.put("startDate", calendarEvent.startDate);
                calendarEntity.put("endDate", calendarEvent.endDate);
                calendarEntity.put("allDay", calendarEvent.allDay);
                calendarEntity.put("houseName", calendarEvent.houseName);
                calendarEntity.put("userCreated", calendarEvent.userCreated);
                calendarEntity.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            dismiss();
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

    }
}
