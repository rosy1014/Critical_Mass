package com.example.ruoxilu.criticalmass;

import android.location.Location;

/**
 * Created by tingyu on 4/16/15.
 */
public class Settings {

    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    /*
     * Constants for location update parameters
     */
    public static final int MILLISECONDS_PER_SECOND = 1000;
    // the update interval
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    public static final int UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // the fast interval ceiling
    public static final int FAST_INTERVAL_CEILING_IN_SECONDS = 1;
    public static final int FAST_INTERVAL_CEILING_IN_MILLLISECONDS =
            FAST_INTERVAL_CEILING_IN_SECONDS * MILLISECONDS_PER_SECOND;
    public static final double UPDATE_PIVOT = 0.005; //update if move more than 5 meters
    public static final int SEARCH_DISTANCE = 5;
    public static final int ZOOM_LEVEL = 17; //city level
    /*
     * Constants for handling location results
     */
    // Conversion from feet to meters
    public static final float METERS_PER_FEET = 0.3048f;
    // Conversion from kilometers to meters
    public static final int METERS_PER_KILOMETER = 1000;
    // Initial offset for calculating the map bounds
    public static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;
    // Accuracy for calculating the map bounds
    public static final float OFFSET_CALCULATION_ACCURACY = 0.01f;

    public static final String APPTAG = "CriticalMass";

    /* Constants for population level */
    public static final int POPLEVEL1 = 1;
    public static final int POPLEVEL2 = 2;
    public static final int POPLEVEL3 = 3;
    public static final int POPLEVEL4 = 4;
    public static final int POPLEVEL5 = 5;
    public static final int POPLEVEL6 = 6;
    public static final int POPSIZE1 = 10;
    public static final int POPSIZE2 = 20;
    public static final int POPSIZE3 = 50;
    public static final int POPSIZE4 = 100;
    public static final int POPSIZE5 = 500;

    /* Maximum number of events displayed in event list*/
    public static final int MAX_EVENT_NUMBER = 10;


    private static Location DEFAULT_LOCATION = new Location(" ");

    private static void setDefaultLocation() {
        DEFAULT_LOCATION.setLongitude(37.0);
        DEFAULT_LOCATION.setLatitude(64.0);
    }

    public static Location getDefaultLocation() {
        setDefaultLocation();
        return DEFAULT_LOCATION;
    }


}
