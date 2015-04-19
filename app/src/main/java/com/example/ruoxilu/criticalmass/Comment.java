package com.example.ruoxilu.criticalmass;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by tingyu on 4/18/15.
 */

@ParseClassName("EventComment")
public class Comment extends ParseObject {

    public Comment() {
        super("EventComment");
    }

    public String getUserName() {
        return getString("UserName");
    }

    public void setUserName(String userName) {
        put("UserName", userName);
    }

    public String getUserComment() {
        return getString("UserComment");
    }

    public void setUserComment(String comentText) {
        put("UserComment", comentText);
    }

    public String getEventId() {
        return getString("EventId");
    }

    public void setEventId(String eventId) {
        put("EventId", eventId);
    }

    public String getUserId() {
        return getString("UserId");
    }

    public void setUserId(String userId) {
        put("UserId", userId);
    }


}
