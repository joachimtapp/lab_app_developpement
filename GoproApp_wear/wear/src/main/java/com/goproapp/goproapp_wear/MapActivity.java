package com.goproapp.goproapp_wear;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * Lock the camera centered above the user location.
 */
public class MapActivity extends WearableActivity {

    private final String TAG = this.getClass().getSimpleName();
    private MapView mapView;
    private MapboxMap map;
    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;
    public Location lausanneLoc = new Location("");//



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.accessToken));

        // This contains the MapView in XML and needs to be called after the account manager
        setContentView(R.layout.activity_map);



        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        LocationEngineProvider locationEngineProvider = new LocationEngineProvider(this);
        locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
        //Location : Lausanne
        lausanneLoc.setLatitude( 46.5196535d);
        lausanneLoc.setLongitude(6.6322734d);


        locationEngineListener = new LocationEngineListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onConnected() {
                locationEngine.requestLocationUpdates();
            }

            @Override
            public void onLocationChanged(Location location) {
                if (map != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
                }
            }
        };
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                // Customize map with markers, polylines, etc.
                map = mapboxMap;
                @SuppressLint("MissingPermission")
                Location lastLocation = new LocationEngineProvider(MapActivity.this)
                        .obtainBestLocationEngineAvailable().getLastLocation();
//                Toast.makeText(MapActivity.this, lastLocation.toString(), Toast.LENGTH_SHORT).show();
                if (lastLocation != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
                }
                else {

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lausanneLoc), 16));
                    // distance : your location to Lausanne
                    float distanceInMeters =  lausanneLoc.distanceTo(lastLocation);
                }
            }
        });
        locationEngine.addLocationEngineListener(locationEngineListener);
        setAmbientEnabled();

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        locationEngine.activate();
    }

    @Override
    protected void onStop() {
        locationEngine.removeLocationUpdates();
        locationEngine.deactivate();
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}