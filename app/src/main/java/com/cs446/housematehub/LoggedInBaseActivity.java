package com.cs446.housematehub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cs446.housematehub.userinit.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public abstract class LoggedInBaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_base);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigation = findViewById(R.id.navigationView);
        bottomNavigation.bringToFront();
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d("SELECTED ITEM", "selected item");
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
        });
    }

    protected void initSpinner(Spinner spinner) {
        List<String> list = new ArrayList<String>();
        list.add("-"); // it is necessary to have this, ask Allan for explanation.
        list.add("Log out");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(0, false);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem == "Log out") {
                    ParseUser.logOut();
                    Intent intent = new Intent(LoggedInBaseActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
}
