package com.goproapp.goproapp_wear;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class DistanceSet extends WearableActivity implements LocationListener {
    private LocationManager locationManager;
    private final static int DISTANCE_UPDATES = 1;
    private final static int TIME_UPDATES = 5;
    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;
    private final String TAG = this.getClass().getSimpleName();
    public int triggerDistance;
    public Location goproLocation;
    public Location lastLocation;
    public float distanceInMeters;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_distance_set);

        // Location manager
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                        this);
                Log.w(TAG, "request location updates");
            } catch (Exception e) {
                Log.w(TAG, "Could not request location updates");
            }
        }


        // recover the trigger distance set by the user
        EditText dist_trig = findViewById(R.id.disttrig_edit);

        // setDist button : set distance trigger and location of the Gopro
        Button setDist = findViewById(R.id.setdist_button);


        setDist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create an object location with the location
                if (lastLocation != null && goproLocation==null) {
                    goproLocation = lastLocation;
                    Toast.makeText(DistanceSet.this,
                            "Gopro Location set to : Lat "+goproLocation.getLatitude()+" Lon :"+goproLocation.getLongitude(), Toast.LENGTH_SHORT).show();

                }
                else if(lastLocation != null && goproLocation!=null)
                {
                    goproLocation = lastLocation;
                    Toast.makeText(DistanceSet.this,
                            "Gopro Location changed to : Lat "+goproLocation.getLatitude()+" Lon :"+goproLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                }
                // trigger distance
                if (dist_trig != null && goproLocation!=null){

                    triggerDistance = Integer.valueOf(dist_trig.getText().toString());
                    Toast.makeText(DistanceSet.this,
                            "Trigger distance set to : "+triggerDistance +" m", Toast.LENGTH_SHORT).show();
                    // Latlng object for a marker of the gopro in the map activity
                    //
                    LatLng goproMarker = new LatLng(goproLocation.getLatitude(),goproLocation.getLongitude());

                    // intent -> marker with the Gopro location to the map activity

            }

            }
        });


    }


    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;
        double lastLat = location.getLatitude();
        Log.v(TAG, "lat :"+lastLat);
        // distance to the Gorpro (distanceInMeters)
        distanceInMeters =  goproLocation.distanceTo(location);
        // current dist : contain the current distance to the Gopro
        TextView currentDist=findViewById(R.id.current_dist);

        currentDist.setText(distanceInMeters+"m");

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
