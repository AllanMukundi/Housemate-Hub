package com.cs446.housematehub.houseinit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.cs446.housematehub.LoggedInBaseActivity;
import com.cs446.housematehub.R;

public class LoginHouseActivity extends LoggedInBaseActivity {

    private Button createHouse;

    private Button joinHouse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_house);
        initView();
    }

    private void initView() {
        createHouse = findViewById(R.id.createHouse);
        joinHouse = findViewById(R.id.joinHouse);

        createHouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginHouseActivity.this, CreateHouseActivity.class);
                startActivity(intent);
            }
        });

        joinHouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginHouseActivity.this, JoinHouseActivity.class);
                startActivity(intent);
            }
        });
    }
}
