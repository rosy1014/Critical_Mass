package com.example.ruoxilu.criticalmass;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by tingyu on 2/26/15.
 */
public class EventActivity extends Activity {

    private String eventObjectId;
    private int mEventSize;
    private String messageBody;
    private String locationName;
    private Integer eventSize;

    private TextView mTitleTextView;
    private TextView mEventSizeView;
    private Button mSendMessageButton;
    private EditText mMessageBodyField;
    private ListView mEventComments;

    private CommentAdapter queryEventComment;
    private String fontPath = "fonts/Nunito-Bold.ttf";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event);
        initViewParts();


        // Receive ObjectId from the List Activity
        Bundle extras = getIntent().getExtras();
        eventObjectId = extras.getString("objectId");

        locationName = extras.getString("location", null);

        ParseQuery<MassEvent> eventsQuery = ParseQuery.getQuery("MassEvent");
        eventsQuery.whereEqualTo("objectId", eventObjectId);
        try {
            MassEvent mass = eventsQuery.getFirst();
            eventSize = mass.getEventSize();

            if (locationName == null) {
                locationName = mass.getLocationName();
            }

        }   catch (ParseException e) {
            Log.e(Settings.APPTAG, e.getMessage());
        }

        // Set title to ObjectId
        mTitleTextView.setText(locationName);
        mEventSizeView.setText(eventSize);

        if (Application.networkConnected(this)) {
            // Populating event comments
            getComments();

            setSendMessageB();
        }

    }

    private void initViewParts() {
        // TODO: Right now we use the unique object id as event title.
        mTitleTextView = (TextView) findViewById(R.id.activity_name);

        // set custom font
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        mTitleTextView.setTypeface(tf);

        mEventSizeView = (TextView) findViewById(R.id.event_size);
        mSendMessageButton = (Button) findViewById(R.id.send_button);
        mMessageBodyField = (EditText) findViewById(R.id.messageBodyField);
        mEventComments = (ListView) findViewById(R.id.event_comments);
    }

    private void getComments() {

        final String finalId = eventObjectId;
        queryEventComment = new CommentAdapter(this, finalId);

        queryEventComment.setTextKey("UserComment");
        mEventComments.setAdapter(queryEventComment);
    }

    private void setSendMessageB() {
        // After a person decides to add comment, add a data field on EventComment and then add a
        // comment to the list view.
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageBody = mMessageBodyField.getText().toString();

                if (messageBody.isEmpty()) {

                    new SweetAlertDialog(EventActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Please enter a message")
                            .show();
                    return;

                } else {

                    Comment userComment = new Comment();
                    userComment.setEventId(eventObjectId);
                    userComment.setUserComment(messageBody);
                    userComment.setUserName(ParseUser.getCurrentUser().getUsername());
                    userComment.setUserId(ParseUser.getCurrentUser().getObjectId());

//                    ParseObject userComment = new ParseObject("EventComment");
//                    userComment.put("EventId", eventObjectId);
//                    userComment.put("UserComment", messageBody);
//                    userComment.put("UserId", ParseUser.getCurrentUser().getObjectId());
//                    userComment.put("UserName", ParseUser.getCurrentUser().getUsername());
                    userComment.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            // Add an comment to the list view.
                            queryEventComment.loadObjects();
                            queryEventComment.notifyDataSetChanged();
                            mMessageBodyField.setText(" ");
                        }
                    });
                }

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });

    }


    // If the activity is resumed


}
