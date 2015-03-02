package com.example.ruoxilu.criticalmass;

import com.parse.Parse;
import com.parse.ParseObject;


public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(MassUser.class);
        Parse.initialize(this, "ADIzf9tA1P4KQFL1AyyAKoCjLKhgaCmaZTmp96CL", "PcefekoiDoE3uR2yUd932HRbPPqrEGJyaE61aPVF");
    }
}
