package com.example.ruoxilu.criticalmass;

/**
 * Created by SEAN on 4/1/15.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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

    Button loginbutton;
    Button signup;
    String usernametxt;
    String passwordtxt;
    EditText password;
    EditText username;
    TextView mAppName;

    String fontPath = Settings.APP_NAME_FONT;

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the view from main.xml
        setContentView(R.layout.loginsignup);

        mAppName = (TextView) findViewById(R.id.app_name);
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        mAppName.setTypeface(tf);

        // Locate EditTexts in main.xml
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        initViewParts();

        // Create a mainContext variable since "this" variable cannot be used in OnClickListener.
        final Context loginSignupContext = this;

        // Login Button Click Listener
        loginbutton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {

                if (Application.networkConnected(loginSignupContext)) {
                    verifyLogin();
                }
            }
        });


        // Sign up Button Click Listener
        signup.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {

                if (Application.networkConnected(loginSignupContext)) {
                    signup();
                }

            }
        });

    }


    protected void initViewParts() {
        // Locate EditTexts in main.xml
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        // Locate Buttons in main.xml
        loginbutton = (Button) findViewById(R.id.login);
        signup = (Button) findViewById(R.id.signup);
    }


    protected void verifyLogin() {
        // Retrieve the text entered from the EditText
        usernametxt = username.getText().toString();
        passwordtxt = password.getText().toString();

        // Send data to Parse.com for verification
        ParseUser.logInInBackground(usernametxt, passwordtxt,
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
        usernametxt = username.getText().toString();
        passwordtxt = password.getText().toString();

        // Force user to fill up the form
        if (usernametxt.equals("") && passwordtxt.equals("")) {

            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("Please complete the sign up form!")
                    .show();

        } else {
            // Save new user data into Parse.com Data Storage
            ParseUser user = new ParseUser();
            user.setUsername(usernametxt);
            user.setPassword(passwordtxt);

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