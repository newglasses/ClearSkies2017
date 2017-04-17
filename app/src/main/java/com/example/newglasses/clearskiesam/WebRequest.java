package com.example.newglasses.clearskiesam;

/**
 * Created by annem on 12/04/2017.
 * Adatped from: http://mobilesiri.com/json-parsing-in-android-using-android-studio/
 */

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

public class WebRequest extends IntentService {

    // for logging
    private final String LOG_TAG = WebRequest.class.getSimpleName();
    private static SharedPreferences sharedPrefs;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.e(LOG_TAG, "Service Started");
        // ArrayList<HashMap<String, String>> queryParams = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> queryParams = new HashMap<String, String>();

        // default demo data
        queryParams.put("lat", Constants.DEMO_LAT);
        queryParams.put("lng", Constants.DEMO_LNG);
        queryParams.put("aurora", Constants.DEMO_AURORA);
        queryParams.put("iss", Constants.DEMO_ISS);

        String webResponse = makeWebServiceCall(Constants.DEMO_PATHNAME, queryParams, Constants.POST);
        Log.e(LOG_TAG, "Web response: " + webResponse);

        try {
            updateSharedPrefs(webResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent i = new Intent(Constants.WEB_REQUEST_DONE);
        this.sendBroadcast(i);
    }

    public String makeWebServiceCall(String requestUrl, HashMap<String, String> params, int requestMethod) {

        URL url;
        String response = "";
        String queryParams = "";

        try {
            queryParams = getPostDataString(params);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder completeUrl = new StringBuilder();
        completeUrl.append(requestUrl);
        completeUrl.append(queryParams);

        try {
            url = new URL(completeUrl.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);

            if (requestMethod == Constants.POST) {
                conn.setRequestMethod("POST");
            } else if (requestMethod == Constants.GET) {
                conn.setRequestMethod("GET");
            }

            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(params));

            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));

                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first) {
                result.append("?");
                first = false;
            }
            else {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private void updateSharedPrefs(String response) throws JSONException {

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        JSONObject object = new JSONObject(response);

        JSONObject results = object.getJSONArray("_results").getJSONObject(0);
        String weatherTier = results.getString("weatherTier");
        String onForecast = results.getString("onForecast");

        Log.e(LOG_TAG, "Inside updateSharedPrefs \n weatherTier: " + weatherTier +
                        " \n onForecast " + onForecast);

        // parse the result & apply
        sharedPrefs.edit().putInt("weatherTier", Integer.valueOf(weatherTier)).apply();
        sharedPrefs.edit().putString("onForecast", onForecast).apply();
    }

    // Validates resource references inside Android XML files
    public WebRequest() {
        super(WeatherService.class.getName());
    }

    public WebRequest(String name) {
        super(name);
    }
}
