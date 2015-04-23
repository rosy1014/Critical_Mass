package com.example.ruoxilu.criticalmass;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * Created by tingyu on 2/26/15.
 */
public class EventActivity extends Activity {

    private String eventObjectId;

    private String messageBody;
    private String locationName;
    private Integer eventSize;
    private com.parse.ParseFile mEventIcon;

    private ParseImageView mIconPImageView;
    private TextView mTitleTextView;
    private TextView mEventSizeView;
    private Button mSendMessageButton;
    private EditText mMessageBodyField;
    private ListView mEventComments;
    private SwipeRefreshLayout mCommentScrollList;

    private CommentAdapter queryEventComment;
    private String fontPath = Settings.EVENT_NAME_FONT;


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
            mEventIcon = mass.getEventImage();

            if (locationName == null) {
                locationName = mass.getLocationName();
            }

        } catch (ParseException e) {
            Log.e(Settings.APPTAG, e.getMessage());
        }

        mIconPImageView.setParseFile(mEventIcon);
        mIconPImageView.setPlaceholder(getResources().getDrawable(R.drawable.giraffe));
        mIconPImageView.loadInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, com.parse.ParseException e) {
                Log.d(Settings.APPTAG,
                        "Fetched image");
            }
        });

        mTitleTextView.setText(locationName);

        mEventSizeView.setText(String.valueOf("Size: " + eventSize));

        if (Application.networkConnected(this)) {
            // Populating event comments
            getComments();

            setSendMessageB();
        }

    }

    private void initViewParts() {

        mIconPImageView = (ParseImageView) findViewById(R.id.activity_image);
        mTitleTextView = (TextView) findViewById(R.id.activity_name);

        // set custom font
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        mTitleTextView.setTypeface(tf);

        mEventSizeView = (TextView) findViewById(R.id.event_size);
        mEventSizeView.setTypeface(tf);

        mSendMessageButton = (Button) findViewById(R.id.send_button);
        mMessageBodyField = (EditText) findViewById(R.id.messageBodyField);
        mEventComments = (ListView) findViewById(R.id.event_comments);

        mCommentScrollList = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mCommentScrollList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshEventInfo();
            }
        });
    }

    private void refreshEventInfo() {

        // Get updated comments
        getComments();

        // Reset the ParseQueryAapter using the new location
        mCommentScrollList.setRefreshing(false);

    }

    private void getComments() {

        queryEventComment = new CommentAdapter(this, eventObjectId);
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

                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    Log.d(Settings.APPTAG, e.getMessage());
                }

                mEventComments.smoothScrollToPosition(0);
            }
        });
    }

}
