package com.cs446.housematehub;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HouseMainActivity extends LoggedInBaseActivity {

    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_main);
        spinner = findViewById(R.id.menu);
        super.initSpinner(spinner);

        BottomNavigationView bottomNavigation = findViewById(R.id.navigationView);
        bottomNavigation.bringToFront();
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnNavigationItemSelectedListener(navListener);
    }

    BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_expenses:
                    // Switch to expenses view here
                    break;
                case R.id.nav_calendar:
                    // Switch to calendar view here
                    break;
                case R.id.nav_home:
                    // Switch to dashboard view here
                    break;
                case R.id.nav_chat:
                    // Switch to chat view here
                    break;
                case R.id.nav_lists:
                    // Switch to lists view here
                    break;
            }
            return true;
        }
    };
}
