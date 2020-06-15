package com.cs446.housematehub.userinit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cs446.housematehub.R;
import com.cs446.housematehub.houseinit.LoginHouseActivity;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.List;

public class SignUpActivity extends AppCompatActivity implements Validator.ValidationListener {

    @NotEmpty
    @Length(min = 4)
    private EditText username;

    @NotEmpty
    @Password
    @Length(min = 6)
    private EditText password;

    @ConfirmPassword
    private EditText confirmPassword;

    private Validator validator;

    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initView();
        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    private void initView() {
        username = findViewById(R.id.userText);
        password = findViewById(R.id.passwordText);
        confirmPassword = findViewById(R.id.passwordText2);
        signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
            }
        });
    }


    @Override
    public void onValidationSucceeded() {
        ParseUser user = new ParseUser();
        String userText = username.getText().toString();
        String passwordText = password.getText().toString();

        user.setUsername(userText);
        user.setPassword(passwordText);

        userExists(user);
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

    public void userExists(final ParseUser user) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", user.getUsername());
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    if (users.size() == 0) {
                        user.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Toast.makeText(SignUpActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignUpActivity.this, LoginHouseActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else {
                                    ParseUser.logOut();
                                    Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                                }
                            }
                        });
                    } else {
                        Toast.makeText(SignUpActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                        ParseUser.logOut();
                    }
                } else {
                    Log.e("Sign Up", e.getMessage());
                }
            }
        });
    }

    public void backButton(View view) {
        finish();
    }
}
