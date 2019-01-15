package com.goproapp.goproapp_wear;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ImageButton;



public class TriggerActivity extends WearableActivity {

    // variable that decide wether the gopro should be shooting or not
    // variable to send via an intent to the tablet
    public boolean triggerCapt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);

        ImageButton trigButton = findViewById(R.id.trigger_button);
        trigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // capture - > intent to capture
                triggerCapt = true;
                // send the intent to the tablet here

                //stop the capture here
                triggerCapt =false;
            }
        });

    }
}
