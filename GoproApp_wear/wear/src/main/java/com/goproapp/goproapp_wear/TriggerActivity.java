package com.goproapp.goproapp_wear;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ImageButton;



public class TriggerActivity extends WearableActivity {

    // variable that decide wether the gopro should be shooting or not
    // variable to send via an intent to the tablet

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);
        setAmbientEnabled();

        Intent intent = new Intent(this, WearService.class);
        intent.setAction(WearService.ACTION_SEND.STARTACTIVITY.name());
        intent.putExtra(WearService.ACTIVITY_TO_START, BuildConfig.W_goproparam);
        startService(intent);

        ImageButton trigButton = findViewById(R.id.trigger_button);
        trigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // capture - > intent to capture
                triggerCapture();

            }
        });

    }

    private void triggerCapture() {
        Intent intent = new Intent(this, WearService.class);
        intent.setAction(WearService.ACTION_SEND.SHUTTER.name());
        intent.putExtra(WearService.SHUTTER_TYPE, BuildConfig.W_shutter_arbitrary);
        startService(intent);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
    }
}
