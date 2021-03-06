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
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static String gopro_ssid;
    ArrayList<Integer> myImageList = new ArrayList<>();
    int z=0;
    int nImageDelaySeconds=5;
    private Boolean firstTime;
    private NavigationView navigationView;
    private  DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission("android" + ""
                + ".permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") ==
                        PackageManager.PERMISSION_DENIED || checkSelfPermission("android" + "" +
                ".permission.INTERNET") == PackageManager.PERMISSION_DENIED)) {
            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android"
                    + ".permission.ACCESS_COARSE_LOCATION", "android.permission.INTERNET"}, 0);
        }


        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                firstTime = sharedPref.getBoolean("mainFirst", true);
                if(firstTime)
                    guidedTour();
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        navigationView =  findViewById(R.id.nav_view);


        navigationView.setNavigationItemSelectedListener(this);
        //read saved gopro ssid
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        gopro_ssid = sharedPref.getString("goProSSID", "none");

        //launch the diaporama
        setMainImageandWelcome();

    }

    private void guidedTour() {
                new ShowcaseView.Builder(MainActivity.this)
                .setTarget(new ViewTarget(navigationView.getHeaderView(0).findViewById(R.id.logged_name)))
                .setContentTitle("User")
                .setContentText("You can see the connected user Here")
                .setStyle(R.style.CustomShowcaseTheme)
                .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        new ShowcaseView.Builder(MainActivity.this)
                                .setTarget(new ViewTarget(navigationView.getMenu().findItem(R.id.goProConnect).getActionView()))
                                .setContentTitle("Quick WiFi switch")
                                .setContentText("Switch that allows you to alternate between the GoPro WiFi " +
                                        "and your usual one quickly.\n This switch can be used after selecting your GoPro " +
                                        "and logging in.")
                                .setStyle(R.style.CustomShowcaseTheme)
                                .build();
                    }
                })
                .build()
                .show();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("mainFirst", false);
        editor.apply();
    }

    private void setMainImageandWelcome() {

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
        }

        //slideshow
        final ImageView slideSh = findViewById(R.id.image_mainim);
        myImageList.add(R.drawable.sport_1);
        myImageList.add(R.drawable.sport_2);
        myImageList.add(R.drawable.sport_3);
        slideSh.setImageResource(myImageList.get(0));

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator()); //and this
        fadeOut.setStartOffset((nImageDelaySeconds-1)*1000);
        fadeOut.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);



        Thread timer = new Thread() {
            public void run() {
                try {
                    for (z = 0; z < myImageList.size() + 4; z++) {
                        if (z < myImageList.size()) {

                            sleep(nImageDelaySeconds*1000);

                            runOnUiThread(new Runnable() {
                                public void run() {

                                    slideSh.setImageResource(myImageList.get(z));
                                    slideSh.startAnimation(animation);
                                }
                            });
                        } else {
                            z = 0;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.start();
    }

    // button callbak -> fetch a wifi connection
    public void onMainConnect(View view) {
        // start the wifi settings of the device
        startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            // Wifimanager get the wifi networks selected
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();

            Button buttonCon = findViewById(R.id.button_maincon);

            if (!wifiManager.isWifiEnabled()) {
                // problem if wifimanager not available
                Toast.makeText(MainActivity.this,
                        "connection problem", Toast.LENGTH_SHORT).show();
            } else {
                TextView maingoprostatu = findViewById(R.id.text_main_gopro_statu);

                if (ssid.contains("GP")) {
                    Toast.makeText(MainActivity.this,
                            "Connect to the GoPro" + ssid, Toast.LENGTH_SHORT).show();
                    buttonCon.setText("Connected");

                    // assign the ssid to the var gopro_ssid
                    SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("goProSSID", ssid);
                    editor.apply();
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
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        DrawerHandler dh = new DrawerHandler(this, navigationView);

        dh.setUserDrawer();
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

    //handle the drawer menu selection
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id != R.id.nav_home) {//exclude himself
            DrawerHandler dh = new DrawerHandler(this, navigationView);

            Intent intent;
            intent = dh.SwitchActivity(id, MainActivity.this);
            MainActivity.this.startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
