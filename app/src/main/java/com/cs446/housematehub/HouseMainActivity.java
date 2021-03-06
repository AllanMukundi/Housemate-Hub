package com.cs446.housematehub;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cs446.housematehub.calendar.CalendarManager;
import com.cs446.housematehub.common.NotificationPublisher;
import com.cs446.housematehub.common.Utils;
import com.cs446.housematehub.dashboard.DashboardManager;
import com.cs446.housematehub.expenses.ExpenseManager;
import com.cs446.housematehub.grouplist.GroupListManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class HouseMainActivity extends LoggedInBaseActivity {

    private ParseUser currentUser;
    private ParseObject currentHouse;
    private TextView toolbarTitle;
    private ImageButton backButton;
    private BottomNavigationView bottomNavigation;

    public static final String TAB_DASHBOARD = "tab_dashboard";
    public static final String TAB_EXPENSE = "tab_expense";
    public static final String TAB_CALENDAR = "tab_calendar";
    public static final String TAB_LIST = "tab_list";

    private HashMap<String, Stack<Fragment>> mStacks;
    private String mCurrentTab;

    public static final String MAIN_CHANNEL_ID = "main_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_main);

        View mainLayout = findViewById(R.id.include);
        toolbarTitle = mainLayout.findViewById(R.id.base_title);

        backButton = mainLayout.findViewById(R.id.tab_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        currentUser = ParseUser.getCurrentUser();
        String houseName = currentUser.get("houseName").toString();
        currentHouse = getCurrentHouse(houseName);

        bottomNavigation = findViewById(R.id.navigationView);
        bottomNavigation.bringToFront();
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        mStacks = new HashMap<String, Stack<Fragment>>();
        mStacks.put(TAB_DASHBOARD, new Stack<Fragment>());
        mStacks.put(TAB_EXPENSE, new Stack<Fragment>());
        mStacks.put(TAB_CALENDAR, new Stack<Fragment>());
        mStacks.put(TAB_LIST, new Stack<Fragment>());

        bottomNavigation.setOnNavigationItemSelectedListener(navListener);

        createNotificationChannels(currentUser.getUsername());

        selectedTab(TAB_DASHBOARD);
    }

    public void switchToTab(String tabId) {
        View v = null;
        switch (tabId) {
            case TAB_DASHBOARD:
                v = bottomNavigation.findViewById(R.id.nav_home);
                break;
            case TAB_EXPENSE:
                v = bottomNavigation.findViewById(R.id.nav_expenses);
                break;
            case TAB_CALENDAR:
                v = bottomNavigation.findViewById(R.id.nav_calendar);
                break;
            case TAB_LIST:
                v = bottomNavigation.findViewById(R.id.nav_lists);
                break;
        }
        v.performClick();
    }

    BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_expenses:
                    selectedTab(TAB_EXPENSE);
                    setToolbarTitle("Expense Manager");
                    break;
                case R.id.nav_calendar:
                    selectedTab(TAB_CALENDAR);
                    setToolbarTitle("Calendar");
                    break;
                case R.id.nav_home:
                    selectedTab(TAB_DASHBOARD);
                    setToolbarTitle("Housemate Hub");
                    break;
                case R.id.nav_lists:
                    selectedTab(TAB_LIST);
                    setToolbarTitle("Lists");
                    break;
            }
            return true;
        }
    };

    public void selectedTab(String tabId) {
        mCurrentTab = tabId;

        if (mStacks.get(tabId).size() == 0) {
            if (tabId.equals(TAB_DASHBOARD)) {
                changeFragments(tabId, new DashboardManager(), true, false);
            } else if (tabId.equals(TAB_EXPENSE)) {
                changeFragments(tabId, new ExpenseManager(), true, false);
            } else if (tabId.equals(TAB_CALENDAR)) {
                changeFragments(tabId, new CalendarManager(), true, false);
            } else if (tabId.equals(TAB_LIST)) {
                changeFragments(tabId, new GroupListManager(), true, false);
            }
        } else {
            changeFragments(tabId, mStacks.get(tabId).lastElement(), false, false);
        }
    }

    public void changeFragments(String tag, Fragment fragment, boolean shouldAdd, boolean animate) {
        if (shouldAdd) mStacks.get(tag).push(fragment);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        if (shouldAdd && animate) {
            ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        } else if (animate) {
            ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
        }
        ft.replace(R.id.house_main_layout, fragment);
        ft.commit();
    }

    public void popFragments() {
        //Select the second last fragment in current tab's stack..
        Fragment fragment = mStacks.get(mCurrentTab).elementAt(mStacks.get(mCurrentTab).size() - 2);

        /*pop current fragment from stack.. */
        mStacks.get(mCurrentTab).pop();

        changeFragments(null, fragment, false, true);
    }

    @Override
    public void onBackPressed() {
        if (mStacks.get(mCurrentTab).size() == 1) {
            // We are already showing first fragment of current tab, so when back pressed, we will finish this activity..
            finish();
            return;
        }

        /* Goto previous fragment in navigation stack of this tab */
        popFragments();
    }

    public List<ParseObject> getHouseUsers(boolean inclusive) {
        List<ParseObject> users = new ArrayList<ParseObject>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("houseName", currentHouse.get("houseName"));
        if (!inclusive) {
            query.whereNotEqualTo("username", currentUser.getUsername());
        }
        try {
            users = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return users;
    }

    public ParseUser getCurrentUser() {
        return currentUser;
    }

    public ParseObject getCurrentHouse() {
        return currentHouse;
    }

    @Nullable
    private ParseObject getCurrentHouse(String houseName) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("House");
        query.whereEqualTo("houseName", houseName);
        try {
            List<ParseObject> response = query.find();
            ParseObject house = response.get(0);
            return house;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void enableBack() {
        backButton.setVisibility(View.VISIBLE);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) toolbarTitle.getLayoutParams();
        params.setMarginStart(Utils.getPX(this, 16));
        toolbarTitle.setLayoutParams(params);
    }

    public void disableBack() {
        backButton.setVisibility(View.GONE);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) toolbarTitle.getLayoutParams();
        params.setMarginStart(0);
        toolbarTitle.setLayoutParams(params);
    }

    public void setToolbarTitle(String text) {
        toolbarTitle.setText(text);
    }


    private void createNotificationChannels(String username) {
        // creates main channel that everyone is subscribed to
        NotificationChannel mainChannel = new NotificationChannel(MAIN_CHANNEL_ID, MAIN_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
        mainChannel.setDescription("Main Channel that every user is subscribed to.");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(mainChannel);
        ParsePush.subscribeInBackground(MAIN_CHANNEL_ID);

        // creates channel that only current user is subscribed to
        NotificationChannel currentUserChannel = new NotificationChannel(username, username, NotificationManager.IMPORTANCE_DEFAULT);
        currentUserChannel.setDescription("Channel that only current user is subscribed to.");
        manager.createNotificationChannel(currentUserChannel);
        ParsePush.subscribeInBackground(username);
    }

    public void sendNotifications(String alertText, String titleText) {
        List<ParseObject> users = getHouseUsers(false);

        //parse notification
        JSONObject data = new JSONObject();

        // Put data in the JSON object
        try {
            data.put("alert", alertText);
            data.put("title", titleText);
        } catch (JSONException e) {
            // should not happen
            throw new IllegalArgumentException("unexpected parsing error", e);
        }

        for (ParseObject user : users) {
            String username = user.get("username").toString();
            // Configure the push
            ParsePush push = new ParsePush();
            push.setChannel(username);
            push.setData(data);
            push.sendInBackground();
        }

    }

    public void scheduleNotification(String alert, String title, long futureInMillis) {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ALERT, alert);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_TITLE, title);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.RTC_WAKEUP, futureInMillis, pendingIntent);
    }

}

