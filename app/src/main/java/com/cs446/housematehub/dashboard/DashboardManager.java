package com.cs446.housematehub.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cs446.housematehub.HouseMainActivity;
import com.cs446.housematehub.R;
import com.cs446.housematehub.account.AccountDetails;
import com.cs446.housematehub.calendar.EventType;
import com.cs446.housematehub.common.RecyclerItemClickListener;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.cs446.housematehub.common.Utils.oneDayAgo;

public class DashboardManager extends Fragment {

    private static final int NUM_EVENTS = 10;

    private ParseObject mHouse;

    private ImageView mAccountButton;
    private RecyclerView mRecentRecyclerView;
    private RecyclerView mUpcomingRecyclerView;
    private DashboardEventAdapter mRecentEventsAdapter;
    private DashboardEventAdapter mUpcomingEventsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateData();
        initialiseViews(view);
        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
    }

    private List<DashboardEvent> getExpenseEvents() {
        List<ParseObject> expenseRes = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Expense");
        query.whereEqualTo("houseName", mHouse.get("houseName"));
        Date oneDayAgo = oneDayAgo();
        try {
            expenseRes = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<DashboardEvent> expenseEvents = new ArrayList<>();
        for (ParseObject expenseRecord : expenseRes) {
            Date commitDate = expenseRecord.getUpdatedAt();
            if (commitDate.after(oneDayAgo)) {
                DashboardEvent event = new DashboardExpenseEvent(
                        expenseRecord.getString("expenseTitle"),
                        Float.valueOf((int) expenseRecord.get("expenseAmount")) / 100,
                        expenseRecord.getString("owner"),
                        expenseRecord.getString("expenseDate"),
                        expenseRecord.getUpdatedAt()
                );
                expenseEvents.add(event);
            }
        }
        return expenseEvents;
    }

    private List<DashboardEvent> getCalendarEvents() {
        List<ParseObject> calendarRes = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Calendar");
        query.whereEqualTo("houseName", mHouse.get("houseName"));
        try {
            calendarRes = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date now = new Date();
        List<DashboardEvent> dashboardCalendarEvents = new ArrayList<>();
        for (ParseObject calendarRecord : calendarRes) {
            Date startDate = calendarRecord.getDate("startDate");
            if (startDate.after(now)) {
                DashboardEvent event = new DashboardCalendarEvent(
                        calendarRecord.getString("eventName"),
                        EventType.getEnum(calendarRecord.getInt("eventType")),
                        calendarRecord.getString("userCreated"),
                        new SimpleDateFormat("dd/MM/yyyy").format(startDate),
                        startDate
                );
                dashboardCalendarEvents.add(event);
            }
        }
        return dashboardCalendarEvents;
    }

    private List<DashboardEvent> getGroupListEvents() {
        List<ParseObject> groupListRes = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupList");
        query.whereEqualTo("houseName", mHouse.get("houseName"));

        try {
            groupListRes = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date oneDayAgo = oneDayAgo();
        List<DashboardEvent> listEvents = new ArrayList<>();
        for (ParseObject groupListRecord : groupListRes) {
            Date commitDate = groupListRecord.getCreatedAt();
            if (commitDate.after(oneDayAgo)) {
                DashboardEvent event = new DashboardListEvent(
                        groupListRecord.getString("groupListTitle"),
                        groupListRecord.getString("owner"),
                        new SimpleDateFormat("dd/MM/yyyy").format(commitDate),
                        commitDate
                );
                listEvents.add(event);
            }
        }
        return listEvents;
    }

    private void updateData() {
        mHouse = ((HouseMainActivity) getActivity()).getCurrentHouse();

        List<DashboardEvent> recentEvents = new ArrayList<>();
        List<DashboardEvent> upcomingEvents = new ArrayList<>();
        Date now = new Date();

        for (DashboardEvent event : getExpenseEvents()) {
            if (event.date.before(now)) {
                recentEvents.add(event);
            } else {
                upcomingEvents.add(event);
            }
        }
        recentEvents.addAll(getGroupListEvents());
        upcomingEvents.addAll(getCalendarEvents());

        Comparator<DashboardEvent> recentEventsComparator = new Comparator<DashboardEvent>() {
            @Override
            public int compare(DashboardEvent e1, DashboardEvent e2) {
                return e1.date.before(e2.date) ? 1 : -1;
            }
        };
        Comparator<DashboardEvent> upcomingEventsComparator = new Comparator<DashboardEvent>() {
            @Override
            public int compare(DashboardEvent e1, DashboardEvent e2) {
                return e1.date.before(e2.date) ? -1 : 1;
            }
        };
        recentEvents = recentEvents.subList(Math.max(0, recentEvents.size() - NUM_EVENTS), recentEvents.size());
        upcomingEvents = upcomingEvents.subList(0, Math.min(upcomingEvents.size(), NUM_EVENTS));
        Collections.sort(recentEvents, recentEventsComparator);
        Collections.sort(upcomingEvents, upcomingEventsComparator);
        mRecentEventsAdapter = new DashboardEventAdapter(recentEvents, getContext());
        mUpcomingEventsAdapter = new DashboardEventAdapter(upcomingEvents, getContext());
    }

    private void initialiseViews(View rootView) {
        mRecentRecyclerView = rootView.findViewById(R.id.recent_events_recyclerview);
        mUpcomingRecyclerView = rootView.findViewById(R.id.upcoming_events_recyclerview);
        mAccountButton = rootView.findViewById(R.id.account_details_button);
    }

    private void updateUI() {
        mRecentRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecentRecyclerView.getContext(),
                ((LinearLayoutManager) mRecentRecyclerView.getLayoutManager()).getOrientation());
        mRecentRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecentRecyclerView.setAdapter(mRecentEventsAdapter);

        mUpcomingRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(mUpcomingRecyclerView.getContext(),
                ((LinearLayoutManager) mUpcomingRecyclerView.getLayoutManager()).getOrientation());
        mUpcomingRecyclerView.addItemDecoration(itemDecoration);
        mUpcomingRecyclerView.setAdapter(mUpcomingEventsAdapter);
        ViewGroup.LayoutParams params = mUpcomingRecyclerView.getLayoutParams();
        params.height = 225 * mUpcomingEventsAdapter.getItemCount();
        mUpcomingRecyclerView.setLayoutParams(params);

        mRecentRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), mRecentRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        DashboardEvent event = mRecentEventsAdapter.getData().get(position);
                        String tab = null;
                        switch (event.type) {
                            case EXPENSE:
                                tab = HouseMainActivity.TAB_EXPENSE;
                                break;
                            case CALENDAR:
                                tab = HouseMainActivity.TAB_CALENDAR;
                                break;
                            case GROUP_LIST:
                                tab = HouseMainActivity.TAB_LIST;
                                break;
                        }
                        ((HouseMainActivity) getActivity()).switchToTab(tab);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );

        mUpcomingRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), mUpcomingRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        DashboardEvent event = mRecentEventsAdapter.getData().get(position);
                        String tab = null;
                        switch (event.type) {
                            case EXPENSE:
                                tab = HouseMainActivity.TAB_EXPENSE;
                                break;
                            case CALENDAR:
                                tab = HouseMainActivity.TAB_CALENDAR;
                                break;
                            case GROUP_LIST:
                                tab = HouseMainActivity.TAB_LIST;
                                break;
                        }
                        ((HouseMainActivity) getActivity()).switchToTab(tab);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );

        mAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = ParseUser.getCurrentUser().getUsername();
                ((HouseMainActivity) getActivity()).changeFragments(
                        HouseMainActivity.TAB_DASHBOARD,
                        AccountDetails.newInstance(userName),
                        true, true
                );
            }
        });
    }

}
