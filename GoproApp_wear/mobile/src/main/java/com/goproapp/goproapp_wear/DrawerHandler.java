package com.goproapp.goproapp_wear;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class DrawerHandler {

    public DrawerHandler() {
    }

    public Intent SwitchActivity(int id, Context context) {
        Intent intent = new Intent();

        switch (id) {
            case R.id.nav_home:
                intent = new Intent(context, MainActivity.class);
                break;
            case R.id.nav_gallery:
                intent = new Intent(context, GalleryActivity.class);
                break;
            case R.id.nav_login:
                intent = new Intent(context, LoginActivity.class);
                break;
            case R.id.nav_livestream:
                intent = new Intent(context, LiveStreamActivity.class);
                break;
        }
        return intent;

    }

    public void addSwitchListener(SwitchCompat drawerSwitch, Context context) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String ssid = info.getSSID();
        if (MainActivity.gopro_ssid != null)
            if (ssid.equals(MainActivity.gopro_ssid)) drawerSwitch.setChecked(true);
        drawerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    try {

                        if (MainActivity.gopro_ssid == null) {
                            Toast.makeText(context, "Please select your goPro in Home", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                                .getSystemService(Context.WIFI_SERVICE);
                        List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
                        for (WifiConfiguration wifiConfiguration : wifiConfigurations) {
                            if (wifiConfiguration.SSID.equals(MainActivity.gopro_ssid)) {
                                wifiManager.enableNetwork(wifiConfiguration.networkId, true);
                                Log.e("Myinfo", "connectToWifi: will enable " + wifiConfiguration.SSID);
                                wifiManager.reconnect();


                            }
                        }
                    } catch (NullPointerException | IllegalStateException e) {
                        Log.e("Myinfo", "connectToWifi: Missing network configuration.");
                    }
                } else {
                    if (LoginActivity.InternetSSID == null) {
                        Toast.makeText(context, "Please Log in to use Cloud storage", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                                .getSystemService(Context.WIFI_SERVICE);

                        List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();

                        for (WifiConfiguration wifiConfiguration : wifiConfigurations) {
                            if (wifiConfiguration.SSID.equals("\"" + LoginActivity.InternetSSID + "\"")) {
                                wifiManager.enableNetwork(wifiConfiguration.networkId, true);
                                Log.e("Myinfo", "connectToWifi: will enable " + wifiConfiguration.SSID);
                                wifiManager.reconnect();


                            }
                        }
                    } catch (NullPointerException | IllegalStateException e) {
                        Log.e("Myinfo", "connectToWifi: Missing network configuration.");
                    }
                }
            }
        });
    }

    public void setUserDrawer(NavigationView navigationView) {

        View hView = navigationView.getHeaderView(0);
        TextView nav_email = (TextView) hView.findViewById(R.id.logged_email);
        TextView nav_name = (TextView) hView.findViewById(R.id.logged_name);
        Menu menuNav = navigationView.getMenu();
        MenuItem nav_gallery = menuNav.findItem(R.id.nav_gallery);
        if (LoginActivity.userID != null) {
            if (LoginActivity.active_user != null) {
                nav_email.setText(LoginActivity.active_user.email);
                nav_name.setText(LoginActivity.active_user.first_name + " " + LoginActivity.active_user.last_name);
            }
            nav_gallery.setEnabled(true);
        } else {
            nav_name.setText("Please log in");
//            nav_gallery.setEnabled(false);
        }

    }
}
