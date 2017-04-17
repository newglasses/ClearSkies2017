package com.example.newglasses.clearskiesam;

/**
 * Created by annem on 17/04/2017.
 */

public class Constants {

    public final static int GET = 1;
    public final static int POST = 2;

    public final static String DEMO_PATHNAME = "http://192.168.1.70:8080/api/demo";

    public static final String WEB_REQUEST_DONE = "com.example.newglasses.amclearskies.WEB_REQUEST_DONE";

    // Defining Broadcasts:
    // Used to notify the ClearSkiesService of the outcome of the background work
    // Depending on the outcome, the ClearSkiesService prepares the UI data accordingly
    
    protected static final String SETTINGS_UPDATED =
            "com.example.newglasses.amclearskies.SETTINGS_UPDATED";

    protected static final String NO_INTERNET =
            "com.example.newglasses.amclearskies.NO_INTERNET";

    protected static final String NOTHING_TO_DECLARE =
            "com.example.newglasses.amclearskies.NOTHING_TO_DECLARE";

    protected static final String AURORA =
            "com.example.newglasses.amclearskies.AURORA";

    protected static final String AURORA_ISS =
            "com.example.newglasses.amclearskies.AURORA_ISS";

    protected static final String ISS =
            "com.example.newglasses.amclearskies.ISS";

    protected static final String OUT_OF_RANGE =
            "com.example.newglasses.amclearskies.OUT_OF_RANGE";

    protected static final String MAKE_NOTIFICATION =
            "com.example.newglasses.amclearskies.MAKE_NOTIFICATION";





    protected static final String NEXT_UPDATE = "NEXT UPDATE";
    
}
