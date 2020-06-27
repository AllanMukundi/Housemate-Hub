package com.cs446.housematehub;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cs446.housematehub.expenses.ExpenseManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class HouseMainActivity extends LoggedInBaseActivity {

    private static ParseUser currentUser;
    private TextView toolbarTitle;
    private Fragment expenseManagerFragment = new ExpenseManager();
    BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_expenses:
                    loadFragment(expenseManagerFragment);
                    setToolbarTitle("Expense Manager");
                    break;
                case R.id.nav_calendar:
                    setToolbarTitle("Calendar");
                    break;
                case R.id.nav_home:
                    setToolbarTitle("Housemate Hub");
                    break;
                case R.id.nav_chat:
                    setToolbarTitle("Chat");
                    break;
                case R.id.nav_lists:
                    setToolbarTitle("Lists");
                    break;
            }
            return true;
        }
    };
    private Spinner spinner;
    public String houseName;

    public List<ParseObject> getHouseUsers() {
        List<ParseObject> users = new ArrayList<ParseObject>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("houseName", houseName);
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

        spinner = findViewById(R.id.menu);
        super.initSpinner(spinner);
        currentUser = ParseUser.getCurrentUser();
        houseName = currentUser.get("houseName").toString();

        BottomNavigationView bottomNavigation = findViewById(R.id.navigationView);
        bottomNavigation.bringToFront();
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnNavigationItemSelectedListener(navListener);
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.house_main_layout, fragment);
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.commit(); // save the changes
    }

    private void setToolbarTitle(String text) {
        toolbarTitle.setText(text);
    }
}

