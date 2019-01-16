package com.goproapp.goproapp_wear;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;

public class DistanceSet extends WearableActivity {

    private LocationManager locationManager;
    private final static int DISTANCE_UPDATES = 1;
    private final static int TIME_UPDATES = 5;
    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;
    private final String TAG = this.getClass().getSimpleName();
    public static int triggerDistance;
    public static Location goproLocation;
    public Location lastLocation;
    public static float distanceInMeters;
    // variable that decide wether the gopro should be shooting or not
    // variable to send via an intent to the tablet
    public boolean triggerCapt;

    //
    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_distance_set);
        setAmbientEnabled();

        // setDist button : set distance trigger and location of the Gopro
        ImageButton setDist = findViewById(R.id.setdist_button);

        // EditText trigger distance
        // trigger distance
        EditText dist_trig = (EditText) findViewById(R.id.disttrig_edit);


        dist_trig.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    /* Write your logic here that will be executed when user taps next button */
                    triggerDistance = Integer.parseInt(dist_trig.getText().toString());
                    Toast.makeText(DistanceSet.this, "Trigger distance set to : " + triggerDistance, Toast.LENGTH_SHORT).show();
                    InputMethodManager imm = (InputMethodManager)getSystemService(DistanceSet.this.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    dist_trig.setSelected(false);
                    handled = true;
                }
                return handled;
            }
        });

        locationEngineListener = new LocationEngineListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onConnected() {
                locationEngine.requestLocationUpdates();
            }

            @Override
            public void onLocationChanged(Location location) {
                lastLocation = location;
                double lastLat = location.getLatitude();
                Log.v(TAG, "lat :" + lastLat);

                if (goproLocation != null) {
                    // distance to the Gorpro (distanceInMeters)
                    distanceInMeters = goproLocation.distanceTo(location);
                    // current dist : contain the current distance to the Gopro
                    TextView currentDist = findViewById(R.id.current_dist);
                    currentDist.setText(Float.toString(distanceInMeters));
                    // check on the distance to gopro to decide if the gopro shoots or not
                    if (distanceInMeters <= triggerDistance) {
                        // call the method that launch the capture process
                        triggerCapture();
                    } else if (distanceInMeters > triggerDistance) {
                        // call the method that stops the capture process
                        triggerCapture();
                    }
                }
            }
        };

        LocationEngineProvider locationEngineProvider = new LocationEngineProvider(this);
        locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.setFastestInterval(1000);
        locationEngine.addLocationEngineListener(locationEngineListener);
        locationEngine.activate();


        setDist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create an object location with the location
                triggerCapt = false;
                if (lastLocation != null && goproLocation == null) {
                    goproLocation = lastLocation;
                    Toast.makeText(DistanceSet.this,
                            "Gopro Location set to : Lat " + goproLocation.getLatitude() + " Lon :" + goproLocation.getLongitude(), Toast.LENGTH_SHORT).show();

                } else if (lastLocation != null && goproLocation != null) {
                    goproLocation = lastLocation;
                    Toast.makeText(DistanceSet.this,
                            "Gopro Location changed to : Lat " + goproLocation.getLatitude() + " Lon :" + goproLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                } else if (lastLocation==null) {
                    Toast.makeText(DistanceSet.this,
                            "Gopro Location not found ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // method stop capture
    private void triggerCapture() {
        Intent intent = new Intent(this, WearService.class);
        intent.setAction(WearService.ACTION_SEND.SHUTTER.name());
        startService(intent);
    }
}
