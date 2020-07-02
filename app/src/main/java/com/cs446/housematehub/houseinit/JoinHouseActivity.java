package com.cs446.housematehub.houseinit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cs446.housematehub.HouseMainActivity;
import com.cs446.housematehub.LoggedInBaseActivity;
import com.cs446.housematehub.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.util.List;

public class JoinHouseActivity extends LoggedInBaseActivity {

    private Spinner spinner;

    private Button joinButton;

    // taken from https://stackoverflow.com/questions/33055860/method-to-cast-object-to-jsonobject-or-jsonarray-depending-on-the-object0k


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_house);
        spinner = findViewById(R.id.menu);
        super.initSpinner(spinner);
        initView();
    }

    private void initView() {
        joinButton = findViewById(R.id.joinButton);

        final EditText houseName = findViewById(R.id.nameText);
        final EditText houseCode = findViewById(R.id.codeText);

        final ParseUser currentUser = ParseUser.getCurrentUser();

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("House");
                query.whereEqualTo("houseName", houseName.getText().toString());
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> houses, ParseException e) {
                        if (e == null) {
                            if (houses != null) {
                                if (houses.size() == 0) {
                                    Toast.makeText(JoinHouseActivity.this, "House does not exist", Toast.LENGTH_SHORT).show();
                                } else {
                                    final ParseObject house = houses.get(0); // house names are unique
                                    String matchingHouseCode = house.get("houseCode").toString();
                                    if (houseCode.getText().toString().equals(matchingHouseCode)) {
                                        JSONArray ja = objectToJSONArray(house.get("houseMembers"));
                                        ja.put(currentUser.getUsername());
                                        house.put("houseMembers", ja);
                                        Log.i("test", houseCode.getText().toString());
                                        house.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                currentUser.put("houseName", house.get("houseName"));
                                                currentUser.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        Toast.makeText(JoinHouseActivity.this, "House joined", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(JoinHouseActivity.this, HouseMainActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        Toast.makeText(JoinHouseActivity.this, "Incorrect house code", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                e.printStackTrace();
                            }
                        } else {
                            Log.e("Join House", e.getMessage());
                        }
                    }
                });
            }
        });
    }

    public void backButton(View view) {
        finish();
    }
}
