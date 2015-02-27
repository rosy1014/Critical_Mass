package com.example.ruoxilu.criticalmass;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by RuoxiLu on 2/22/15.
 */
@ParseClassName("MassUser")
public class MassUser extends ParseObject{

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public static ParseQuery<MassUser> getQuery(){
        return ParseQuery.getQuery(MassUser.class);
    }
}
