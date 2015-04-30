package com.example.ruoxilu.criticalmass;

/**
 * Created by SEAN on 4/1/15.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginSignupActivity extends Activity {

    // Declare Variables
    Button mLoginButton;
    Button mSignup;
    EditText mPasswordInput;
    EditText mUsernameInput;
    TextView mAppName;

    String mUsernameText;
    String mPasswordText;

    String mFontPath = Settings.APP_NAME_FONT;

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the view from main.xml
        setContentView(R.layout.loginsignup);

        mAppName = (TextView) findViewById(R.id.app_name);
        Typeface tf = Typeface.createFromAsset(getAssets(), mFontPath);
        mAppName.setTypeface(tf);

        // Locate EditTexts in main.xml
        mUsernameInput = (EditText) findViewById(R.id.username);
        mPasswordInput = (EditText) findViewById(R.id.password);

        initViewParts();

        // Create a mainContext variable since "this" variable cannot be used in OnClickListener.
        final Context loginSignupContext = this;

        // Login Button Click Listener
        mLoginButton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {

                if (Application.networkConnected(loginSignupContext)) {
                    verifyLogin();
                }
            }
        });


        // Sign up Button Click Listener
        mSignup.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {

                if (Application.networkConnected(loginSignupContext)) {
                    signup();
                }

            }
        });

    }


    protected void initViewParts() {
        // Locate EditTexts in main.xml
        mUsernameInput = (EditText) findViewById(R.id.username);
        mPasswordInput = (EditText) findViewById(R.id.password);

        // Locate Buttons in main.xml
        mLoginButton = (Button) findViewById(R.id.login);
        mSignup = (Button) findViewById(R.id.signup);
    }


    protected void verifyLogin() {
        // Retrieve the text entered from the EditText
        mUsernameText = mUsernameInput.getText().toString();
        mPasswordText = mPasswordInput.getText().toString();

        // Send data to Parse.com for verification
        ParseUser.logInInBackground(mUsernameText, mPasswordText,
                new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        if (e == null) {

                            // If user exist and authenticated, send user to MapsActivity.class
                            Intent intent = new Intent(
                                    LoginSignupActivity.this,
                                    MapsActivity.class);

                            startActivity(intent);
                            finish();

                        } else {

                            new SweetAlertDialog(LoginSignupActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Oops...")
                                    .setContentText("No such user exist, please sign up.")
                                    .show();

                        }
                    }
                }
        );
    }

    protected void signup() {
        // Retrieve the text entered from the EditText
        mUsernameText = mUsernameInput.getText().toString();
        mPasswordText = mPasswordInput.getText().toString();

        // Force user to fill up the form
        if (mUsernameText.equals("") && mPasswordText.equals("")) {

            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("Please complete the sign up form!")
                    .show();

        } else {
            // Save new user data into Parse.com Data Storage
            ParseUser user = new ParseUser();
            user.setUsername(mUsernameText);
            user.setPassword(mPasswordText);

            user.signUpInBackground(new SignUpCallback() {
                public void done(ParseException e) {
                    if (e == null) {

                        new SweetAlertDialog(LoginSignupActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Yay!")
                                .setContentText("Successfully signed up, please log in!")
                                .show();

                    } else {

                        new SweetAlertDialog(LoginSignupActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops...")
                                .setContentText("Something went wrong! Please try again!")
                                .show();

                    }
                }
            });
        }
    }

}