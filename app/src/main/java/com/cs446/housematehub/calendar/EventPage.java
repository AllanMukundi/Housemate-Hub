package com.cs446.housematehub.calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
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
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class EventPage extends Fragment {

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
    TextView user;

    Calendar start;
    Calendar end;

    boolean all_day = false;

    final SimpleDateFormat dateFormatter = new SimpleDateFormat("E, MMM dd, yyyy");
    final SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");

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
        ((HouseMainActivity) getActivity()).setToolbarTitle("Edit Event");

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
        user = view.findViewById(R.id.user);

        eventName.setText(event.eventName);
        user.setText("Created By: " + event.userCreated);

        Calendar cal = Calendar.getInstance();
        cal.setTime(event.originalStartDate);
        start = (Calendar) cal.clone();
        cal.setTime(event.endDate);
        end = (Calendar) cal.clone();

        initDatePickerListener(dayStartButton, start);
        initDatePickerListener(dayEndButton, end);
        initTimePickerListener(timeStartButton, start);
        initTimePickerListener(timeEndButton, end);

        dayStartButton.setText(dateFormatter.format(start.getTime()));
        dayEndButton.setText(dateFormatter.format(end.getTime()));
        timeStartButton.setText(timeFormatter.format(start.getTime()));
        timeEndButton.setText(timeFormatter.format(end.getTime()));

        initSpinner(EventType.getValues(), eventTypeSpinner);
        initSpinner(RepeatType.getValues(), repeatTypeSpinner);
        initSpinner(NotificationType.getValues(), notificationTypeSpinner);

        eventTypeSpinner.setSelection(EventType.getValues().indexOf(event.eventType.getString()));
        repeatTypeSpinner.setSelection(RepeatType.getValues().indexOf(event.repeatType.getString()));
        notificationTypeSpinner.setSelection(NotificationType.getValues().indexOf(event.notificationType.getString()));

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

        allDaySwitch.setChecked(event.allDay);

        Button deleteButton = view.findViewById(R.id.delete_button);
        Button updateButton = view.findViewById(R.id.update_button);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Delete the event?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteCalendarEvent();
                                getActivity().onBackPressed();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorDebtRed));
                        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorMaterialBlue));
                    }
                });
                alert.show();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((start.compareTo(end) > 0 && !all_day) || (start.compareTo(end) > 0 && all_day && start.get(Calendar.DAY_OF_MONTH) != end.get(Calendar.DAY_OF_MONTH))) {
                    Toast.makeText(getActivity(), "Event start time must be before end time", Toast.LENGTH_LONG).show();
                } else if (eventName.getText().toString().matches("")) {
                    Toast.makeText(getActivity(), "Event must have a title", Toast.LENGTH_SHORT).show();
                } else {
                    updateCalendarEvent();
                    getActivity().onBackPressed();
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

    private void updateCalendarEvent() {
        List<ParseObject> eventRes = new ArrayList<ParseObject>();
        ParseObject event_row;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Calendar");
        final String houseName = (String) ((HouseMainActivity) getActivity()).getCurrentHouse().get("houseName");

        query.whereEqualTo("houseName", houseName);
        query.whereEqualTo("eventId", event.id);

        try {
            eventRes = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (eventRes.size() == 0) {
            throw new RuntimeException("Event with the given eventId was not found");
        }

        event_row = eventRes.get(0); // should only be one matching row

        event.eventName = eventName.getText().toString();
        event.eventType = EventType.getEnum(eventTypeSpinner.getSelectedItem().toString());
        event.notificationType = NotificationType.getEnum(notificationTypeSpinner.getSelectedItem().toString());
        event.repeatType = RepeatType.getEnum(repeatTypeSpinner.getSelectedItem().toString());
        event.startDate = start.getTime();
        event.endDate = end.getTime();
        event.allDay = all_day;

        event_row.put("eventName", event.eventName);
        event_row.put("eventType", event.eventType.ordinal());
        event_row.put("notificationType", event.notificationType.ordinal());
        event_row.put("repeatType", event.repeatType.ordinal());
        event_row.put("startDate", event.startDate);
        event_row.put("endDate", event.endDate);
        event_row.put("allDay", event.allDay);
        try {
            event_row.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void deleteCalendarEvent() {
        List<ParseObject> eventRes = new ArrayList<ParseObject>();
        ParseObject event_row;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Calendar");
        final String houseName = (String) ((HouseMainActivity) getActivity()).getCurrentHouse().get("houseName");

        query.whereEqualTo("houseName", houseName);
        query.whereEqualTo("eventId", event.id);

        try {
            eventRes = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (eventRes.size() == 0) {
            throw new RuntimeException("Event with the given eventId was not found");
        }

        event_row = eventRes.get(0); // should only be one matching row

        try {
            event_row.delete();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
