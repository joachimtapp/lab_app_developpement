package com.example.lennard.test_go_pro;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private boolean toogle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void test(View view) {
        if(toogle){
            new GetMethodDemo().execute("http://10.5.5.9/gp/gpControl/command/mode?p=1");
        } else {
            new GetMethodDemo().execute("http://10.5.5.9/gp/gpControl/command/mode?p=0");
        }
        toogle = !toogle;
    }
}

