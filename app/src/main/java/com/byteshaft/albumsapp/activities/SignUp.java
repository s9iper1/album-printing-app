package com.byteshaft.albumsapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.byteshaft.albumsapp.R;
import com.byteshaft.albumsapp.utils.AppGlobals;
import com.byteshaft.albumsapp.utils.Config;
import com.byteshaft.albumsapp.utils.Constants;
import com.byteshaft.albumsapp.utils.ui.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

public class SignUp extends AppCompatActivity implements HttpRequest.OnReadyStateChangeListener,
        View.OnClickListener {

    private EditText mEmailEntry;
    private EditText mPasswordEntry;
    private EditText mPasswordRepeatEntry;
    private EditText mFullNameEntry;
    private EditText mMobileNumberEntry;
    private Button mSignUpButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mEmailEntry = (EditText) findViewById(R.id.entry_signup_email);
        mPasswordEntry = (EditText) findViewById(R.id.entry_signup_password);
        mPasswordRepeatEntry = (EditText) findViewById(R.id.entry_signup_password_repeat);
        mFullNameEntry = (EditText) findViewById(R.id.entry_signup_fullname);
        mMobileNumberEntry = (EditText) findViewById(R.id.entry_signup_mobile);
        mSignUpButton = (Button) findViewById(R.id.button_signup_execute);
        assert mSignUpButton != null;
        mSignUpButton.setOnClickListener(this);
        setCustomFont();
    }

    private void setCustomFont() {
        mEmailEntry.setTypeface(AppGlobals.typeface);
        mPasswordEntry.setTypeface(AppGlobals.typeface);
        mPasswordRepeatEntry.setTypeface(AppGlobals.typeface);
        mFullNameEntry.setTypeface(AppGlobals.typeface);
        mMobileNumberEntry.setTypeface(AppGlobals.typeface);
        mSignUpButton.setTypeface(AppGlobals.typeface);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void onReadyStateChange(HttpURLConnection connection, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                try {
                    Log.i("Res", "" + readyState + "connection "+ connection.getResponseCode());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    switch (connection.getResponseCode()) {
                        case HttpURLConnection.HTTP_CREATED:
                            Config.userRegistrationDone(true);
                            SignIn.getInstance().finish();
                            finish();
                            startActivity(
                                    new Intent(getApplicationContext(), ActivateAccount.class)
                            );
                            break;
                        case HttpURLConnection.HTTP_CONFLICT:
                            Helpers.alertDialog(SignUp.this, "User already registered",
                                    "The user is already registered");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void raiseFieldMandatory(EditText editText) {
        if (isEditTextEmpty(editText)) {
            editText.setError("Required field.");
        }
    }

    private boolean isEditTextEmpty(EditText editText) {
        String text = editText.getText().toString();
        return text.isEmpty();
    }

    private void validateSignUpData() {
        String email = mEmailEntry.getText().toString();
        String password = mPasswordEntry.getText().toString();
        String repeatPassword = mPasswordRepeatEntry.getText().toString();
        String fullName = mFullNameEntry.getText().toString();
        String mobileNumber = mMobileNumberEntry.getText().toString();

        EditText[] inputFields = {
                mEmailEntry,
                mPasswordEntry,
                mPasswordRepeatEntry,
                mFullNameEntry,
                mMobileNumberEntry
        };
        for (EditText editText: inputFields) {
            raiseFieldMandatory(editText);
        }

        if (!isEmailValid(email)) {
            mEmailEntry.setError("Invalid Email.");
            return;
        }
        if (!password.equals(repeatPassword)) {
            mPasswordRepeatEntry.setError("Passwords should be same.");
            return;
        }

        signUp(email, password, fullName, mobileNumber);
    }

    private void signUp(String email, String password, String fullName, String mobile) {

        HttpRequest request = new HttpRequest(getApplicationContext());
        request.setOnReadyStateChangeListener(this);
        request.open("POST", Constants.ENDPOINT_REGISTER);
        request.send(getSignUpData(email, password, fullName, mobile));
        Log.i("TAG", "sent request");
    }

    private String getSignUpData(
            String email,
            String password,
            String fullName,
            String mobile
    ) {
        JSONObject object = new JSONObject();
        try {
            object.put("email", email);
            object.put("password", password);
            object.put("full_name", fullName);
            object.put("mobile_number", mobile);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_signup_execute:
                validateSignUpData();
        }
    }
}
