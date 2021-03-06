package com.goproapp.goproapp_wear;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;

// Icons made by <a href="https://www.flaticon.com/authors/chanut" title="Chanut">Chanut</a> licensed by <a href="http://creativecommons.org/licenses/by/3.0/"

public class MainActivity extends WearableActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission("android" + ""
                + ".permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") ==
                        PackageManager.PERMISSION_DENIED || checkSelfPermission("android" + "" +
                ".permission.INTERNET") == PackageManager.PERMISSION_DENIED)) {
            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android"
                    + ".permission.ACCESS_COARSE_LOCATION", "android.permission.INTERNET"}, 0);
        }
        // Enables Always-on
        setAmbientEnabled();

        Button dist = findViewById(R.id.sendDist);
        dist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // start Distance Activity
                String TAG = "MyActivity";
                Log.i(TAG, "Start activity distance set");
                Intent intentdist = new Intent(MainActivity.this, DistanceSet.class);
                startActivity(intentdist);
                //

            }
        });

        Button map = findViewById(R.id.mapBtn);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start Map Activity
                String TAG = "MyActivity";
                Log.i(TAG, "Start activity Map");
                Intent intentmap = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intentmap);

            }
        });

        Button capture = findViewById(R.id.trigger_activ);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start capture Activity
                String TAG = "MyActivity";
                Log.i(TAG, "Start activity Capture");
                Intent intentcap = new Intent(MainActivity.this, TriggerActivity.class);
                startActivity(intentcap);
            }
        });
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        updateDisplay();
    }

    private void updateDisplay() {

    }

}
