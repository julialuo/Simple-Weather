package com.juliazluo.www.simpleweather;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST = 1340;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private NetworkInfo activeNetworkInfo;
    private ViewPager mViewPager;
    private static Location location;
    private static ArrayList<DayOfWeek> days;

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        days = new ArrayList<>();

        // Initiate location and connectivity manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        getLocationData();
    }

    /**
     * Retrieve user's location
     */
    private void getLocationData() {
        if (displayGpsStatus()) {

            // Create new location listener
            locationListener = new MyLocationListener();

            // Check if permissions granted
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {

                // If there is a last known location stored
                if (locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    initiateFragments();
                } else if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    // Retrieve location from network (faster)
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
                            locationListener, null);
                } else {
                    // Retrieve location from GPS (slower)
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,
                            locationListener, null);
                }
            } else {
                // Ask to grant permissions
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE}, LOCATION_REQUEST);
                getLocationData();
            }
        } else {
            //show GPS alert
            GPSAlert();
            getLocationData();
        }
    }

    /**
     * Show an alert telling user to turn on GPS
     */
    protected void GPSAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please enable your GPS to continue")
                .setCancelable(false)
                .setTitle("GPS Status")
                .setPositiveButton("GPS On",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // proceed to settings
                                Intent gpsOptionsIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(gpsOptionsIntent);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // cancel the dialog box
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Check if GPS is turned on
     *
     * @return boolean - whether GPS is turned on
     */
    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver,
                LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }

    /**
     * Initiate fragments
     */
    private void initiateFragments() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            location = loc;
            initiateFragments();
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static ListAdapter adapter;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            int position = getArguments().getInt(ARG_SECTION_NUMBER);
            final View rootView;
            switch (position) {
                case 1:
                    // Populate main fragment with weather data
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);
                    new Utils.TodayAPICall().execute(rootView, location, getActivity());
                    return rootView;
                case 2:
                    // Initialize the view and recycler list
                    rootView = inflater.inflate(R.layout.fragment_filter, container, false);
                    RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
                    adapter = new ListAdapter(days);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recyclerView.setAdapter(adapter);

                    ((ImageButton) rootView.findViewById(R.id.filter_btn)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Retrieve the user's input and get the days that match the filter
                            Utils.clearDays();
                            Double minTemp = 0.0;
                            Double maxTemp = 100.0;
                            String minInput = ((EditText) rootView.findViewById(R.id.min_temp))
                                    .getText().toString();
                            String maxInput = ((EditText) rootView.findViewById(R.id.max_temp))
                                    .getText().toString();
                            if (!minInput.equals("")) {
                                minTemp = Double.parseDouble(minInput);
                            }
                            if (!maxInput.equals("")) {
                                maxTemp = Double.parseDouble(maxInput);
                            }
                            new Utils.WeekAPICall().execute(location, minTemp, maxTemp);
                        }
                    });

                    return rootView;
            }
            return null;
        }

        /**
         * Update the recycler view of days in the week
         */
        public static void updateRecycler() {
            days.clear();
            days.addAll(Utils.days);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "0";
                case 1:
                    return "1";
            }
            return null;
        }
    }
}
