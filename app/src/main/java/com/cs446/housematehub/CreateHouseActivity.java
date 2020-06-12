package com.cs446.housematehub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;

import java.util.List;

public class CreateHouseActivity extends LoggedInBaseActivity implements Validator.ValidationListener {

    @NotEmpty
    @Length(min = 4)
    private EditText houseName;

    @NotEmpty
    @Password
    @Length(min = 6)
    private EditText houseCode;

    private Validator validator;

    private Button createHouseButton;

    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_house);
        spinner = findViewById(R.id.menu);
        super.initSpinner(spinner);
        initView();
        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    private void initView() {
        houseName = findViewById(R.id.nameText);
        houseCode = findViewById(R.id.codeText);

        createHouseButton = findViewById(R.id.createButton);

        createHouseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
            }
        });
    }


    @Override
    public void onValidationSucceeded() {
        ParseObject house = new ParseObject("House");

        String houseNameText = houseName.getText().toString();
        String houseCodeText = houseCode.getText().toString();

        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Current user must be assigned in CreateHouseActivity");
        }

        JSONArray ja = new JSONArray();
        ja.put(currentUser.getUsername());

        house.put("houseName", houseNameText);
        house.put("houseCode", houseCodeText);
        house.put("houseMembers", ja);

        houseExists(house);
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            // Display error messages
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }


    public void houseExists(final ParseObject house) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("House");
        query.whereEqualTo("houseName", house.get("houseName"));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> houses, ParseException e) {
                if (e == null) {
                    if (houses != null) {
                        if (houses.size() == 0) {
                            house.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    ParseUser currentUser = ParseUser.getCurrentUser();
                                    currentUser.put("houseName", house.get("houseName"));
                                    currentUser.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            Toast.makeText(CreateHouseActivity.this, "House created", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(CreateHouseActivity.this, HouseMainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }
                                    });
                                }
                            });
                        } else {
                            Toast.makeText(CreateHouseActivity.this, "House name already exists", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("Create House", e.getMessage());
                }
            }
        });
    }

    public void backButton(View view) {
        finish();
    }
}
