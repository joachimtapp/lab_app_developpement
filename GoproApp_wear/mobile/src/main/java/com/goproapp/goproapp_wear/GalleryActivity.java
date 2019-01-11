package com.goproapp.goproapp_wear;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity
        implements GalleryFragment.OnFragmentInteractionListener,
        GalleryMapFragment.OnFragmentInteractionListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private static CustomViewPager mViewPager;
    private DrawerLayout mDrawerLayout;
    public static List<ImgData> imgData = new ArrayList<ImgData>();
    private MyFirebaseRecordingListener mFirebaseRecordingListener;
    private DatabaseReference databaseRef;

    private String imgDataFile = "imgDataFile";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gallery);

//        readLocalData();

        LoginActivity.userID = "foo";

        //handle drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        // Handle navigation view item clicks here.
                        int id = item.getItemId();
                        if (id != R.id.nav_gallery) {//exclude himself
                            DrawerHandler dh = new DrawerHandler();

                            Intent intent;
                            intent = dh.SwitchActivity(id, GalleryActivity.this);
                            GalleryActivity.this.startActivity(intent);
                        }
                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        drawer.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //set drawer button
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (CustomViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        //Set user info on the drawer
        DrawerHandler dh = new DrawerHandler();
        dh.setUserDrawer(navigationView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home button
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override //require do manually restart activity to handle the map during orientation change
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Intent intent;
        intent = new Intent(GalleryActivity.this, GalleryActivity.class);
        GalleryActivity.this.startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseRecordingListener = new MyFirebaseRecordingListener();
        databaseRef.child("users").child(LoginActivity.userID).child("Data").addValueEventListener
                (mFirebaseRecordingListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        databaseRef.child("users").child(LoginActivity.userID).child("Data").removeEventListener
                (mFirebaseRecordingListener);
    }

    //return the fragment for each section
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return GalleryFragment.newInstance();
            else if (position == 1)
                return GalleryMapFragment.newInstance();
            else return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    //get user stored data
    private class MyFirebaseRecordingListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (imgData.size() == 0)
                for (final DataSnapshot rec : dataSnapshot.getChildren()) {

                    final ImgData newImgData = new ImgData();
                    String db_date = rec.child("text").getValue().toString();
                    String db_HR = rec.child("heart_rate").getValue().toString();
                    String db_position = rec.child("position").getValue().toString();
                    String db_imUrl = rec.child("picture").getValue().toString();
                    ImgData newData = new ImgData();
                    newData.date = db_date;
                    newData.bpm = db_HR;
                    String[] latLng = db_position.split(",");
                    double latitude = Double.parseDouble(latLng[0]);
                    double longitude = Double.parseDouble(latLng[1]);
                    newData.latLng = new LatLng(latitude, longitude);
                    newData.imgUrl = db_imUrl;
                    imgData.add(newData);
                }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.v("err", databaseError.toString());
        }
    }

    private void readLocalData() {
        try {
            FileInputStream fis = this.openFileInput(imgDataFile);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            Log.v("debug", "read file: " + sb.toString());
            try {
                JSONArray array = new JSONArray(sb.toString());

                Log.v("debug", "len: " + array.length());
                for (int i = 0; i < array.length(); i++) {

                    ImgData newImgData = new ImgData();
                    JSONObject image = array.getJSONObject(i);
                    Log.v("debug", "img: " + image.toString());
                    newImgData.bpm = image.getString("bpm");
                    newImgData.date = image.getString("date");
                    newImgData.imgString = image.getString("imgString");
                    newImgData.name = image.getString("name");

                    LatLng latLng = new LatLng();
                    latLng.setLatitude(Float.parseFloat(image.getJSONObject("latLng").getString("latitude")));
                    latLng.setLongitude(Float.parseFloat(image.getJSONObject("latLng").getString("longitude")));
                    newImgData.latLng = latLng;
                    Log.v("debug", "bpm. " + newImgData.bpm);
                    Log.v("debug", newImgData.date);
                    Log.v("debug", newImgData.name);
                    Log.v("debug", "lat: " + latLng.toString());
                    imgData.add(newImgData);
                }
            } catch (Throwable tx) {
                Log.v("debug", "parsing error" + sb.toString());
            }

        } catch (FileNotFoundException e) {
            Log.e("debug", "file not found");
        } catch (UnsupportedEncodingException e) {

        } catch (IOException e) {
        }


    }

    class NameList {
        List<ImgData> list;
        //getter and setter
    }

    @Override
    protected void onStop() {
        super.onStop();
//        FileOutputStream outputStream;
//        Gson gson = new Gson();
//        String json = gson.toJson(imgData);
//        Log.d("debug","write "+json);
//        try {
//            outputStream = openFileOutput(imgDataFile, this.MODE_PRIVATE);
//            outputStream.write(json.getBytes());
//            outputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}


