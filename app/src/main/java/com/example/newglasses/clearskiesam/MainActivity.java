package com.example.newglasses.clearskiesam;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;

import static com.example.newglasses.clearskiesam.Constants.NO_INTERNET;

public class MainActivity extends AppCompatActivity {

    // for logging
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static CustomAdapter testBaseCustomAdapter;
    public static Calendar cal;

    // When accessing via PreferenceManager, DefaultPrefs you get access to the one global set
    private static SharedPreferences sharedPrefs;

    private Activity thisActivity;
    private Long alarmTime;

    private TextView dateView;
    private ListView listView;

    // Overlay
    private View topLevelLayout;


    // TODO: Implement ProgressBar
    protected static ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        topLevelLayout = findViewById(R.id.top_layout);

        dateView = (TextView) findViewById(R.id.list_date);

        // Set up friendly date
        dateView.setText(Utility.getFriendlyDayString(this, System.currentTimeMillis()));

        // Set up background work
        WakefulIntentService.scheduleAlarms(new DailyListener(), this, false);

        // Set up the ListView adapter
        testBaseCustomAdapter = new CustomAdapter(this,
                ApplicationController.getInstance().getTextFirstArray(),
                ApplicationController.getInstance().getTextSecondArray(),
                ApplicationController.getInstance().getTextThirdArray(),
                ApplicationController.getInstance().getStyleArray());

        listView = (ListView) findViewById(R.id.listView);

        listView.setAdapter(testBaseCustomAdapter);

        // Only display overlay on first use
        if (isFirstTime()) {
            topLevelLayout.setVisibility(View.INVISIBLE);
        }

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v(LOG_TAG, "Inside onRestoreInstanceState");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            Log.e(LOG_TAG, "Refresh option has been selected");
            // Until ConnectivityReceiver is used with the refresh button
            if (isNetworkAvailable()) {
                Intent clearSkiesIntent = new Intent(this, ClearSkiesService.class);
                startService(clearSkiesIntent);
                // Until ProgressBar is implemented use a toast
                Toast.makeText(MainActivity.this, "Updating...", Toast.LENGTH_LONG).show();
            } else {
                ClearSkiesService.noInternet = true;
                Intent i = new Intent(NO_INTERNET);
                sendBroadcast(i);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void onItemSelected(Uri contentUri) {
            Intent intent = new Intent(this, MainActivity.class)
                    .setData(contentUri);
            startActivity(intent);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    private boolean isFirstTime() {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean ranBefore = sharedPrefs.getBoolean("ranBefore", false);

        /* FOR TESTING PURPOSES
        ** ranBefore = false;
        */

        String locationPref = sharedPrefs.getString("locationPref", "Roaming");
        String alertTime = sharedPrefs.getString("timePicker", "20:00");

        int hourPref = sharedPrefs.getInt("selectedHour", 20);
        int minPref = sharedPrefs.getInt("selectedMinute", 00);

        String nextUpdate;

        Calendar cal=Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        cal.set(Calendar.HOUR_OF_DAY, hourPref);
        cal.set(Calendar.MINUTE, minPref);
        cal.set(Calendar.SECOND, 00);

        if(cal.getTimeInMillis() < System.currentTimeMillis()) {
            nextUpdate = "Tomorrow";
        } else {
            nextUpdate = "Today";
        }

            // Put default values in the ListView
            ApplicationController.getInstance().getTextFirstArray().add("Defaults");
            ApplicationController.getInstance().getTextFirstArray().add("NEXT UPDATE");

            ApplicationController.getInstance().getTextSecondArray().add(locationPref);
            ApplicationController.getInstance().getTextSecondArray().add(nextUpdate);

            ApplicationController.getInstance().getTextThirdArray().add(alertTime);
            ApplicationController.getInstance().getTextThirdArray().add(alertTime);

            ApplicationController.getInstance().getStyleArray().add("0");
            ApplicationController.getInstance().getStyleArray().add("1");

            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();

        if (!ranBefore) {
            sharedPrefs.edit().putBoolean("ranBefore", true).apply();
            topLevelLayout.setVisibility(View.VISIBLE);

            topLevelLayout.setOnTouchListener(new View.OnTouchListener() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    topLevelLayout.setVisibility(View.INVISIBLE);
                    return false;
                }
            });


        } else {

            Utility.clearArrayLists();

            ApplicationController.getInstance().getTextFirstArray().add("NEXT UPDATE");

            ApplicationController.getInstance().getTextSecondArray().add(nextUpdate);

            ApplicationController.getInstance().getTextThirdArray().add(alertTime);

            ApplicationController.getInstance().getStyleArray().add("0");

        }
    return ranBefore;
    }

    public boolean isNetworkAvailable() {
        // CODE TAKEN FROM HERE: http://www.vogella.com/tutorials/AndroidNetworking/article.html
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
