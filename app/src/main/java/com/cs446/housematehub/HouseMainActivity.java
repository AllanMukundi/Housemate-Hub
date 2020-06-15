package com.cs446.housematehub;

import android.os.Bundle;
import android.widget.Spinner;

public class HouseMainActivity extends LoggedInBaseActivity {

    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_main);
        spinner = findViewById(R.id.menu);
        super.initSpinner(spinner);
    }
}
