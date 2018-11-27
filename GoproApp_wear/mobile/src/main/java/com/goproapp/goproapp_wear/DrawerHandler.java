package com.goproapp.goproapp_wear;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.annotations.Nullable;

public class DrawerHandler {

    public DrawerHandler(){};

    public  Intent SwitchActivity(int id, Context context){
        Intent intent= new Intent( context,GalleryActivity.class);

        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_gallery) {
            intent= new Intent(context, GalleryActivity.class);


        } else if (id == R.id.nav_login) {
            intent= new Intent(context, LoginActivity.class);


        } else if (id == R.id.nav_settings) {


        } else if (id == R.id.nav_livestream) {
            intent= new Intent(context, LiveStreamActivity.class);

        } else if (id == R.id.nav_send) {

        }
        return intent;

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);


    }
}
