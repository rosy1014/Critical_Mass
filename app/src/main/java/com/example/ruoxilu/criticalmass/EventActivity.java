package com.example.ruoxilu.criticalmass;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.view.View;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tingyu on 2/26/15.
 */
public class EventActivity extends Activity {

    private String eventObjectId;
//    private int mEventSize;
    private ArrayList<String> comments;
    private ArrayList<String> username;

    private String messageBody;

    private TextView mTitleTextView;
    private TextView mEventSizeView;
    private Button mSendMessageButton;
    private EditText mMessageBodyField;
    private ListView mEventComments;

    private ParseQueryAdapter<ParseObject> queryEventComment;
    private String fontPath = "fonts/Nunito-Bold.ttf";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event);
        initViewParts();

        // Receive ObjectId from the List Activity
        Bundle extras = getIntent().getExtras();
        eventObjectId = extras.getString("objectId");
        // Set title to ObjectId
        mTitleTextView.setText(eventObjectId);


        if (Application.networkConnected(this)) {
            // Populating event comments
            String[] values = getComments(eventObjectId);

            // Bind data from adapter to ListView.
            ArrayAdapter<String> adapter = new EventActivityAdapter(this, comments, username);
            mEventComments.setAdapter(adapter);

            setSendMessageB();
        }

    }

    private void initViewParts() {

        // TODO: Right now we use the unique object id as event title.
        mTitleTextView = (TextView) findViewById(R.id.activity_name);
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        mTitleTextView.setTypeface(tf);

//        mEventSizeView = (TextView) findViewById(R.id.event_size);
        mSendMessageButton = (Button) findViewById(R.id.send_button);
        mMessageBodyField = (EditText) findViewById(R.id.messageBodyField);
        mEventComments = (ListView) findViewById(R.id.event_comments);
    }

//    private void getComments() {
//        ParseQueryAdapter.QueryFactory<ParseObject> factoryEventComment =
//                new ParseQueryAdapter.QueryFactory<ParseObject>() {
//                    public ParseQuery create() {
//                        ParseQuery queryEventComment = new ParseQuery("EventComment");
//                        queryEventComment.whereEqualTo("EventId", eventObjectId);
//                        return queryEventComment;
//                    }
//                };
//        queryEventComment = new ParseQueryAdapter<ParseObject>(this, factoryEventComment);
//
//        queryEventComment.setTextKey("UserComment");
//        mEventComments.setAdapter(queryEventComment);
//    }

    private String [] getComments(String eventId) {
        ParseQuery queryEventComment = new ParseQuery("EventComment");
        queryEventComment.whereEqualTo("EventId", eventId);
        comments = new ArrayList<String>();

        List<ParseObject> parseObjects;

        try {
            // Use find instead of findInBackground because of a potential thread problem.
            parseObjects = queryEventComment.find();

            for (ParseObject commentInfo : parseObjects) {
                comments.add(commentInfo.getString("UserComment"));
                username.add(commentInfo.getString("username"));
            }

        } catch (com.parse.ParseException e) {
            Log.d(Settings.APPTAG, e.getMessage());
        }

        String[] values = new String[comments.size()];
        values = comments.toArray(values);

        return values;

    }



    private void setSendMessageB() {
        // After a person decides to add comment, add a data field on EventComment and then add a
        // comment to the list view.
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageBody = mMessageBodyField.getText().toString();

                if (messageBody.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter a message", Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    ParseObject userComment = new ParseObject("EventComment");
                    userComment.put("EventId", eventObjectId);
                    userComment.put("UserComment", messageBody);
                    userComment.put("UserId", ParseUser.getCurrentUser().getObjectId());
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
            }
        });
    }
}
