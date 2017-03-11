package com.juliazluo.www.simpleweather;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thbs.skycons.library.CloudFogView;
import com.thbs.skycons.library.CloudHvRainView;
import com.thbs.skycons.library.CloudMoonView;
import com.thbs.skycons.library.CloudRainView;
import com.thbs.skycons.library.CloudSnowView;
import com.thbs.skycons.library.CloudSunView;
import com.thbs.skycons.library.CloudThunderView;
import com.thbs.skycons.library.CloudView;
import com.thbs.skycons.library.MoonView;
import com.thbs.skycons.library.SunView;
import com.thbs.skycons.library.WindView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by julia on 2017-03-09.
 */

public class Utils {

    private static final String CLASS_NAME = "Utils";
    private static final String API_KEY = "6d585fa0b44c7bddbd4e540600a9b926";
    private static final String DARK_SKY_URL = "https://api.darksky.net/forecast/";
    private static final String rainText = "It will start raining around ";
    private static final String noRainText = "It will not rain in the next hour";

    protected static ArrayList<DayOfWeek> days = new ArrayList<>();

    /**
     * Convert input stream to string
     *
     * @param is
     * @return String representing input stream
     */
    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * Converts int value from calendar to day of week
     *
     * @param day
     * @return String representation of day of week
     */
    public static String intToDay(int day) {
        switch (day) {
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            default:
                return "Saturday";
        }
    }

    /**
     * Clears the days of week arraylist
     */
    public static void clearDays() {
        days.clear();
    }

    public static class TodayAPICall extends AsyncTask<Object, Void, JSONObject> {
        View view;
        Context context;

        @Override
        protected JSONObject doInBackground(Object... data) {
            // Get view, location, and context
            view = (View) data[0];
            Location location = (Location) data[1];
            context = (Context) data[2];
            String urlStr = DARK_SKY_URL + API_KEY + "/" + location.getLatitude() + "," +
                    location.getLongitude();
            try {
                // Make DarkSky API call
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String response = convertStreamToString(in);
                JSONObject json = new JSONObject(response);
                return json;
            } catch (Exception e) {
                Log.e(CLASS_NAME, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                // Get and display temperature
                double temperature = jsonObject.getJSONObject("currently").getDouble("temperature");
                ((TextView) view.findViewById(R.id.temperature)).setText((int) temperature + "");

                // Get and display if it will rain
                boolean willRain = false;
                JSONArray jsonArray = jsonObject.getJSONObject("minutely").getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (jsonArray.getJSONObject(i).getInt("precipProbability") > 0) {
                        SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm");
                        Date date = new Date(jsonArray.getJSONObject(0).getInt("time"));
                        String time = localDateFormat.format(date);
                        ((TextView) view.findViewById(R.id.rain)).setText(rainText + time);
                        willRain = true;
                        break;
                    }
                }
                if (!willRain) {
                    ((TextView) view.findViewById(R.id.rain)).setText(noRainText);
                }

                // Get and display summary
                String description = jsonObject.getJSONObject("currently").getString("summary");
                ((TextView) view.findViewById(R.id.description)).setText(description);

                // Get icon string and display icon
                String iconStr = jsonObject.getJSONObject("currently").getString("icon");
                setSkycon(iconStr);

            } catch (Exception e) {
                Log.e(CLASS_NAME, e.getMessage());
            }
            super.onPostExecute(jsonObject);
        }

        /**
         * Sets the skycon based on the icon string from API
         *
         * @param iconStr
         */
        private void setSkycon(String iconStr) {
            RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.skycon_container);
            LinearLayout layout = new LinearLayout(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layout.setLayoutParams(params);

            // Choose a SkyCon view based on the icon string given by API
            switch (iconStr) {
                case "clear-day":
                    SunView sunView = new SunView(context);
                    sunView.setLayoutParams(params);
                    sunView.setBgColor(Color.argb(200, 0, 0, 0));
                    sunView.setStrokeColor(Color.WHITE);
                    layout.addView(sunView);
                    break;
                case "clear-night":
                    MoonView moonView = new MoonView(context);
                    moonView.setLayoutParams(params);
                    moonView.setBgColor(Color.argb(200, 0, 0, 0));
                    moonView.setStrokeColor(Color.WHITE);
                    layout.addView(moonView);
                    break;
                case "partly-cloudy-day":
                    CloudSunView csView = new CloudSunView(context);
                    csView.setLayoutParams(params);
                    csView.setBgColor(Color.argb(200, 0, 0, 0));
                    csView.setStrokeColor(Color.WHITE);
                    layout.addView(csView);
                    break;
                case "partly-cloudy-night":
                    CloudMoonView cmView = new CloudMoonView(context);
                    cmView.setLayoutParams(params);
                    cmView.setBgColor(Color.argb(200, 0, 0, 0));
                    cmView.setStrokeColor(Color.WHITE);
                    layout.addView(cmView);
                    break;
                case "cloudy":
                    CloudView cloudView = new CloudView(context);
                    cloudView.setLayoutParams(params);
                    cloudView.setBgColor(Color.argb(200, 0, 0, 0));
                    cloudView.setStrokeColor(Color.WHITE);
                    layout.addView(cloudView);
                    break;
                case "rain":
                    CloudRainView crView = new CloudRainView(context);
                    crView.setLayoutParams(params);
                    crView.setBgColor(Color.argb(200, 0, 0, 0));
                    crView.setStrokeColor(Color.WHITE);
                    layout.addView(crView);
                    break;
                case "sleet":
                    CloudHvRainView hrView = new CloudHvRainView(context);
                    hrView.setLayoutParams(params);
                    hrView.setBgColor(Color.argb(200, 0, 0, 0));
                    hrView.setStrokeColor(Color.WHITE);
                    layout.addView(hrView);
                    break;
                case "snow":
                    CloudSnowView snView = new CloudSnowView(context);
                    snView.setLayoutParams(params);
                    snView.setBgColor(Color.argb(200, 0, 0, 0));
                    snView.setStrokeColor(Color.WHITE);
                    layout.addView(snView);
                    break;
                case "wind":
                    WindView windView = new WindView(context);
                    windView.setLayoutParams(params);
                    windView.setBgColor(Color.argb(200, 0, 0, 0));
                    windView.setStrokeColor(Color.WHITE);
                    layout.addView(windView);
                    break;
                case "fog":
                    CloudFogView cfView = new CloudFogView(context);
                    cfView.setLayoutParams(params);
                    cfView.setBgColor(Color.argb(200, 0, 0, 0));
                    cfView.setStrokeColor(Color.WHITE);
                    layout.addView(cfView);
                    break;
                default:
                    CloudThunderView ctView = new CloudThunderView(context);
                    ctView.setLayoutParams(params);
                    ctView.setBgColor(Color.argb(200, 0, 0, 0));
                    ctView.setStrokeColor(Color.WHITE);
                    layout.addView(ctView);
            }

            relativeLayout.addView(layout);
        }
    }

    public static class WeekAPICall extends AsyncTask<Object, Void, JSONObject> {

        double minTemp, maxTemp;

        @Override
        protected JSONObject doInBackground(Object... data) {
            // Get location and max and min temperatures
            Location location = (Location) data[0];
            minTemp = (double) data[1];
            maxTemp = (double) data[2];

            String urlStr = DARK_SKY_URL + API_KEY + "/" + location.getLatitude() + "," +
                    location.getLongitude();
            try {
                // Make API call
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String response = convertStreamToString(in);
                JSONObject json = new JSONObject(response);
                return json;
            } catch (Exception e) {
                Log.e(CLASS_NAME, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                JSONArray jsonArray = jsonObject.getJSONObject("daily").getJSONArray("data");
                Calendar calendar = Calendar.getInstance();

                for (int i = 0; i < jsonArray.length(); i++) {
                    double minTemp = jsonArray.getJSONObject(i).getDouble("temperatureMin");
                    double maxTemp = jsonArray.getJSONObject(i).getDouble("temperatureMax");

                    // Check if the day's temperature matches criteria
                    if (minTemp > this.minTemp && maxTemp < this.maxTemp) {
                        // Get the date
                        calendar.setTimeInMillis(jsonArray.getJSONObject(i).getLong("time") * 1000);
                        int day = calendar.get(Calendar.DAY_OF_WEEK);

                        // Add to the arraylist of days of the week and update the list
                        DayOfWeek dayOfWeek = new DayOfWeek(intToDay(day) + "", (int) minTemp + "",
                                (int) maxTemp + "");
                        days.add(dayOfWeek);
                        MainActivity.PlaceholderFragment.updateRecycler();
                    }
                }

            } catch (Exception e) {
                Log.e(CLASS_NAME, e.getMessage());
            }
            super.onPostExecute(jsonObject);
        }
    }
}
