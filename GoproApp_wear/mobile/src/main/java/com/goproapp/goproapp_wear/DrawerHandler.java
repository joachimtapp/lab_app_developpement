package com.goproapp.goproapp_wear;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

public class DrawerHandler {

    public DrawerHandler(){};

    public  Intent SwitchActivity(int id, Context context){
        Intent intent=new Intent();

        if (id == R.id.nav_home ) {
            intent= new Intent(context, MainActivity.class);
        } else if (id == R.id.nav_gallery) {
            intent= new Intent(context, GalleryActivity.class);
        } else if (id == R.id.nav_login) {
            intent= new Intent(context, LoginActivity.class);
        } else if (id == R.id.nav_settings) {
            intent= new Intent(context, SettingsActivity.class);
        } else if (id == R.id.nav_livestream) {
            intent= new Intent(context, LiveStreamActivity.class);
        }
        return intent;

        }
    public void setUserDrawer(NavigationView navigationView ){

        View hView =  navigationView.getHeaderView(0);
        TextView nav_email = (TextView)hView.findViewById(R.id.logged_email);
        TextView nav_name = (TextView)hView.findViewById(R.id.logged_name);
        Menu menuNav=navigationView.getMenu();
        MenuItem nav_gallery = menuNav.findItem(R.id.nav_gallery);
        if (LoginActivity.userID!=null) {
            if(LoginActivity.active_user!=null) {
                nav_email.setText(LoginActivity.active_user.email);
                nav_name.setText(LoginActivity.active_user.first_name + " " + LoginActivity.active_user.last_name);
            }
            nav_gallery.setEnabled(true);
        }
        else {
            nav_name.setText("Please log in");
            nav_gallery.setEnabled(false);
        }

    }
}
