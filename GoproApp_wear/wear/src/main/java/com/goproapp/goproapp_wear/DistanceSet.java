package com.goproapp.goproapp_wear;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_distance_set);

        // Location manager

        // setDist button : set distance trigger and location of the Gopro
        Button setDist = findViewById(R.id.setdist_button);


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
                // trigger distance
                EditText dist_trig = (EditText) findViewById(R.id.disttrig_edit);
                if (dist_trig.getText().length() > 0) {
                    triggerDistance = Integer.valueOf(dist_trig.getText().toString());
                    Toast.makeText(DistanceSet.this,
                            "Trigger distance set to : "+triggerDistance, Toast.LENGTH_SHORT).show();
                }
                if (goproLocation != null) {
                    // distance to the Gorpro (distanceInMeters)
                    distanceInMeters = goproLocation.distanceTo(location);
                    // current dist : contain the current distance to the Gopro
                    TextView currentDist = findViewById(R.id.current_dist);
                    currentDist.setText(Float.toString(distanceInMeters));
                    // check on the distance to gopro to decide if the gopro shoots or not
                    if (distanceInMeters <= triggerDistance) {
                        // call the method that launch the capture process
                        triggerCaptureOn();
                    } else if (distanceInMeters > triggerDistance) {
                        // call the method that stops the capture process
                        triggerCaptureOff();
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
    private void triggerCaptureOff() {

    }
    // method start capture
    private void triggerCaptureOn() {

    }


}
