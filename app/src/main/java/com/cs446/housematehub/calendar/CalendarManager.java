package com.cs446.housematehub.calendar;

import android.os.Bundle;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import com.cs446.housematehub.HouseMainActivity;
import com.cs446.housematehub.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.cs446.housematehub.calendar.EventType.getEnum;

public class CalendarManager extends Fragment {

    private View view;
    private FloatingActionButton addEventButton;
    private HouseMainActivity mainActivity;
    private String houseName;

    private List<CalendarEvent> calendarEvents = new ArrayList<>();
    private Calendar rightNow;

    public CalendarManager() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            calendarEvents = (List<CalendarEvent>) savedInstanceState.getSerializable("calendarEvents");
            addEventCards(getLayoutInflater());
        }
        rightNow = Calendar.getInstance();
        mainActivity = ((HouseMainActivity) getActivity());
        houseName = (String) mainActivity.getCurrentHouse().get("houseName");
        getCalendarEvents();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("calendarEvents", (Serializable) calendarEvents);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
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

        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCalendarEvents();
                addEventCards(inflater);

                pullToRefresh.setRefreshing(false);
            }
        });

        addEventCards(inflater);
        mainActivity.disableBack();
        mainActivity.setToolbarTitle("Calendar");
        return view;
    }

    public void getCalendarEvents() {
        calendarEvents = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Calendar").orderByAscending("startDate");
        query.whereEqualTo("houseName", houseName);
        List<CalendarEvent> removeList = new ArrayList<>();
        try {
            List<ParseObject> eventList = query.find();
            for(ParseObject e : eventList) {
                CalendarEvent event = new CalendarEvent(
                        e.getInt("id"),
                        e.getString("eventName"),
                        EventType.getEnum(e.getInt("eventType")),
                        NotificationType.getEnum(e.getInt("notificationType")),
                        RepeatType.getEnum(e.getInt("repeatType")),
                        e.getDate("startDate"),
                        e.getDate("endDate"),
                        e.getBoolean("allDay"),
                        e.getString("houseName"),
                        e.getString("userCreated")
                );
                calendarEvents.add(event);
                Calendar c = Calendar.getInstance();
                c.setTime(e.getDate("startDate"));
                if (c.before(rightNow) && RepeatType.getEnum(e.getInt("repeatType")) == RepeatType.NO_REPEAT) removeList.add(event);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (CalendarEvent event : removeList) calendarEvents.remove(event);
        for (CalendarEvent event : calendarEvents) {
            if(event.repeatType != RepeatType.NO_REPEAT) event = adjustRepeatEvent(event);
        }
        Collections.sort(calendarEvents);
        int year = -1;
        int month = -1;
        int day = -1;
        for (CalendarEvent event : calendarEvents) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(event.startDate);
            if (year != cal.get(Calendar.YEAR)) {
                event.showHeader = true;
                event.showDate = true;
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);
            }
            if (month != cal.get(Calendar.MONTH)) {
                event.showHeader = true;
                event.showDate = true;
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);
            }
            if (day != cal.get(Calendar.DAY_OF_MONTH)) {
                event.showDate = true;
                day = cal.get(Calendar.DAY_OF_MONTH);
            }
        }
    }

    private void addEventCards(LayoutInflater inflater) {
        LinearLayout scrollView = view.findViewById(R.id.event_list);
        scrollView.removeAllViews();
        for(final CalendarEvent event : calendarEvents) {
            View eventCard = inflater.inflate(R.layout.fragment_calendar_event_item, null);
            TextView monthText = eventCard.findViewById(R.id.monthText);
            TextView dateNum = eventCard.findViewById(R.id.dateNum);
            TextView dateWeek = eventCard.findViewById(R.id.dateWeek);
            TextView titleAndUser = eventCard.findViewById(R.id.titleAndUser);
            TextView timeRange = eventCard.findViewById(R.id.timeRange);

            dateNum.setText(new SimpleDateFormat("d").format(event.startDate));
            dateWeek.setText(new SimpleDateFormat("EEE").format(event.startDate));
            titleAndUser.setText(event.eventName + " - " + event.userCreated);

            String startTime = new SimpleDateFormat("h:mm a").format(event.startDate);
            String endTime;
            Calendar cStart = Calendar.getInstance();
            Calendar cEnd = Calendar.getInstance();
            cStart.setTime(event.startDate);
            cEnd.setTime(event.endDate);

            if(cStart.get(Calendar.YEAR) == rightNow.get(Calendar.YEAR)) {
                monthText.setText(new SimpleDateFormat("MMMM").format(event.startDate));
            } else {
                monthText.setText(new SimpleDateFormat("MMMM YYYY").format(event.startDate));
            }

            if(cEnd.DAY_OF_MONTH == cStart.DAY_OF_MONTH && cEnd.YEAR == cStart.YEAR) {
                endTime = new SimpleDateFormat("h:mm a").format(event.endDate);
            } else {
                endTime = new SimpleDateFormat("MMM d h:mm a").format(event.endDate);
            }
            timeRange.setText(startTime + " - " + endTime);

            if(!event.showHeader) {
                LinearLayout monthHeader = eventCard.findViewById(R.id.monthHeader);
                monthHeader.setVisibility(View.GONE);
                monthHeader.setTag("notSticky");
            }

            if(!event.showDate) {
                LinearLayout dateBox = eventCard.findViewById(R.id.dateBox);
                dateBox.setVisibility(View.INVISIBLE);
            }

            eventCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment ePage = new EventPage(event);
                    ((HouseMainActivity) v.getContext()).changeFragments(HouseMainActivity.TAB_CALENDAR, ePage,true);
                }
            });

            scrollView.addView(eventCard);
        }
    }

    private CalendarEvent adjustRepeatEvent(CalendarEvent event) {
        Calendar start = Calendar.getInstance();
        start.setTime(event.startDate);

        if(start.compareTo(rightNow) > 0) return event;
        double intervalBetween;
        switch (event.repeatType) {
            case DAILY:
                intervalBetween = Math.ceil(ChronoUnit.MINUTES.between(start.toInstant(), rightNow.toInstant()));
                intervalBetween = (((int) intervalBetween + 1439) / 1440 * 1440) / 1440;
                start.add(Calendar.DAY_OF_YEAR, (int) intervalBetween);
                break;
            case WEEKLY:
                intervalBetween = Math.ceil(ChronoUnit.MINUTES.between(start.toInstant(), rightNow.toInstant()));
                intervalBetween = (((int) intervalBetween + 1439) / 1440 * 1440) / 1440;
                intervalBetween = ((int) intervalBetween + 6) / 7 * 7;
                start.add(Calendar.DAY_OF_YEAR, (int) intervalBetween);
                break;
            case BI_WEEKLY:
                intervalBetween = Math.ceil(ChronoUnit.MINUTES.between(start.toInstant(), rightNow.toInstant()));
                intervalBetween = (((int) intervalBetween + 1439) / 1440 * 1440) / 1440;
                intervalBetween = ((int) intervalBetween + 13) / 14 * 14;
                start.add(Calendar.DAY_OF_YEAR, (int) intervalBetween);
                break;
            case MONTHLY:
                intervalBetween = Math.ceil(ChronoUnit.MONTHS.between(YearMonth.from(start.toInstant()), YearMonth.from(rightNow.toInstant())));
                start.add(Calendar.MONTH, (int) intervalBetween);
                break;
            default:
                return event;
        }

        event.startDate = start.getTime();
        return event;
    }
}
