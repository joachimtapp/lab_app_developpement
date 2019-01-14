package com.goproapp.goproapp_wear;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static String gopro_ssid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission("android" + ""
                + ".permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") ==
                        PackageManager.PERMISSION_DENIED || checkSelfPermission("android" + "" +
                ".permission.INTERNET") == PackageManager.PERMISSION_DENIED)) {
            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android"
                    + ".permission.ACCESS_COARSE_LOCATION", "android.permission.INTERNET"}, 0);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);


        navigationView.setNavigationItemSelectedListener(this);
        //read saved gopro ssid
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = "none";
        gopro_ssid = sharedPref.getString("goProSSID", defaultValue);

        //launch the diaporama
        setMainImageandWelcome();
    }

    private void setMainImageandWelcome() {
        ImageView mainIm = findViewById(R.id.image_mainim);
        mainIm.setImageResource(R.drawable.sport_1);
        TextView welcomeTex = findViewById(R.id.text_main_gopro_statu);
        TextView ssidView = findViewById(R.id.SSIDView);
        LinearLayout ssidLayout = findViewById(R.id.ssidLayout);
        ImageView goprostat = findViewById(R.id.main_im_cam_statu);

        if (!gopro_ssid.equals("none")) {
            ssidView.setText(gopro_ssid.replace("\"", ""));
            ssidLayout.setAlpha(1.0f);
            goprostat.setImageResource(R.drawable.ic_linked_camera_black_24dp);
        } else {
            ssidLayout.setAlpha(0.0f);
            goprostat.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp);
        }
        if (LoginActivity.userID != null) {
            if (LoginActivity.active_user != null) {
                welcomeTex.setText(" Welcome " + LoginActivity.active_user.first_name);
            }

        } else {
            welcomeTex.setText("Please log in");
//            nav_gallery.setEnabled(false);
        }
    }

    // button callbak -> fetch a wifi connection
    public void onMainConnect(View view) {
        // start the wifi settings of the device
        selectWifiNetwork();

    }

    private void selectWifiNetwork() {
        // fetch wifi
        startActivityForResult(new Intent(
                Settings.ACTION_WIFI_SETTINGS), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            // Wifimanager get the wifi networks selected
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            // info of the wifi network selected
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            // name of the wifi selected
            String ssid = wifiInfo.getSSID();

            Button buttonCon = findViewById(R.id.button_maincon);

            if (!wifiManager.isWifiEnabled()) {
                // problem if wifimanager not available
                Toast.makeText(MainActivity.this,
                        "connection problem", Toast.LENGTH_SHORT).show();
            } else {
                //String name = wifiInfo.getSSID();
                //Toast.makeText(MainActivity.this,
                // "Connect to the GoPro"+ssid, Toast.LENGTH_SHORT).show();
                TextView maingoprostatu = findViewById(R.id.text_main_gopro_statu);
                // check if the network correspont to a Gopro -> the 2 first letter of the network ssid are : "GP..."
                String NetworkIdCheck = ssid.substring(1, 3);

                if (NetworkIdCheck.equals("GP")) {
                    Toast.makeText(MainActivity.this,
                            "Connect to the GoPro" + ssid, Toast.LENGTH_SHORT).show();
                    buttonCon.setText("Connected");

                    // assign the ssid to the var gopro_ssid
                    SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("goProSSID", ssid);
                    editor.commit();
                    gopro_ssid = ssid;
                    setMainImageandWelcome();

                } else {
                    Toast.makeText(MainActivity.this,
                            ssid + "Network is not a GoPro", Toast.LENGTH_SHORT).show();
                    maingoprostatu.setText("Please connect to a GoPro");
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DrawerHandler dh = new DrawerHandler();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        dh.setUserDrawer(navigationView);
        SwitchCompat drawerSwitch = (SwitchCompat) navigationView.getMenu().findItem(R.id.goProConnect).getActionView();
        dh.addSwitchListener(drawerSwitch,this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //handle the drawer menu selection
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id != R.id.nav_home) {//exclude himself
            DrawerHandler dh = new DrawerHandler();

            Intent intent;
            intent = dh.SwitchActivity(id, MainActivity.this);
            MainActivity.this.startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
