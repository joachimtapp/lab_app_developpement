package com.goproapp.goproapp_wear;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.text.Editable;
import android.text.TextWatcher;
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
    // handle to not shoot
    public boolean isInsideTriggerCapt;

    //


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_distance_set);

        // Location manager

        // setDist button : set distance trigger and location of the Gopro
        Button setDist = findViewById(R.id.setdist_button);

        // EditText trigger distance
        // trigger distance
        EditText dist_trig = (EditText) findViewById(R.id.disttrig_edit);

        dist_trig.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                // set the Distance trigger location
                if(dist_trig.getText().toString().length()>0) {
                    triggerDistance = Integer.parseInt(dist_trig.getText().toString());
                    Toast.makeText(DistanceSet.this,
                            "Trigger distance set to : " + triggerDistance, Toast.LENGTH_SHORT).show();

                }


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

                        if( isInsideTriggerCapt==true) {
                            triggerCapture();
                            triggerCaptureOn();
                        }


                    } else if (distanceInMeters > triggerDistance) {
                        //
                        isInsideTriggerCapt = true;
                        // call the method that stops the capture process

                        triggerCapture();
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

                if (lastLocation != null && goproLocation == null) {
                    goproLocation = lastLocation;
                    Toast.makeText(DistanceSet.this,
                            "Gopro Location set to : Lat " + goproLocation.getLatitude() + " Lon :" + goproLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    isInsideTriggerCapt=false;

                } else if (lastLocation != null && goproLocation != null) {
                    goproLocation = lastLocation;
                    Toast.makeText(DistanceSet.this,
                            "Gopro Location changed to : Lat " + goproLocation.getLatitude() + " Lon :" + goproLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    isInsideTriggerCapt=false;
                } else if (lastLocation==null) {
                    Toast.makeText(DistanceSet.this,
                            "Gopro Location not found ", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void triggerCaptureOff() {
    }

    private void triggerCaptureOn() {
    }

    // method stop capture
    private void triggerCapture() {
        Intent intent = new Intent(this, WearService.class);
        intent.setAction(WearService.ACTION_SEND.SHUTTER.name());
        startService(intent);
    }
}
