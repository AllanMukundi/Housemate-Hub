package com.cs446.housematehub;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cs446.housematehub.account.AccountDetails;
import com.cs446.housematehub.expenses.ExpenseManager;
import com.cs446.housematehub.grouplist.GroupListManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class HouseMainActivity extends LoggedInBaseActivity {

    private ParseUser currentUser;
    private ParseObject currentHouse;
    private TextView toolbarTitle;
    private Fragment expenseManagerFragment = new ExpenseManager();
    private Fragment groupListManagerFragment = new GroupListManager();

    BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_expenses:
                    loadFragment(expenseManagerFragment, "ExpenseManager", true);
                    setToolbarTitle("Expense Manager");
                    break;
                case R.id.nav_calendar:
                    setToolbarTitle("Calendar");
                    break;
                case R.id.nav_home:
                    setToolbarTitle("Housemate Hub");
                    loadFragment(AccountDetails.newInstance(currentUser.getUsername()), "account_details_fragment", true);
                    break;
                case R.id.nav_chat:
                    setToolbarTitle("Chat");
                    break;
                case R.id.nav_lists:
                    loadFragment(groupListManagerFragment, "GroupListManager", true);
                    setToolbarTitle("Lists");
                    break;
            }
            return true;
        }
    };

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_main);

        View mainLayout = findViewById(R.id.include);
        toolbarTitle = mainLayout.findViewById(R.id.base_title);

        Spinner spinner = findViewById(R.id.menu);
        super.initSpinner(spinner);
        currentUser = ParseUser.getCurrentUser();
        String houseName = currentUser.get("houseName").toString();
        currentHouse = getCurrentHouse(houseName);

        BottomNavigationView bottomNavigation = findViewById(R.id.navigationView);
        bottomNavigation.bringToFront();
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnNavigationItemSelectedListener(navListener);
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

    public void loadFragment(Fragment fragment, String tag, boolean popBackstack) {
        FragmentManager fm = getSupportFragmentManager();
        if (popBackstack) {
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.house_main_layout, fragment);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit(); // save the changes
    }

    private void setToolbarTitle(String text) {
        toolbarTitle.setText(text);
    }
}

