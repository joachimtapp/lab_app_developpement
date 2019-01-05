package com.goproapp.goproapp_wear;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class LiveStreamActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ImageButton mMenuDeployer;
    private static boolean down_EV = true;
    private ArrayList<View> menus = new ArrayList<>();

    private Spinner spinner_res_video;
    private Spinner spinner_FPS_video;
    private Spinner spinner_FOV_video;

    private Spinner spinner_res_photo;
    private Spinner spinner_FOV_photo;

    private GoProCombinations goProCombinations;
    private ArrayList<String> FPS_spinner_video;
    private ArrayList<String> FOV_spinner_video;
    private Switch switchWB_video;
    private Switch switchISO_video;
    private SeekBar seekBarWB_video;
    private SeekBar seekBarISO_video;

    private Switch switchWB_photo;
    private Switch switchISO_photo;
    private Switch switchShutter_photo;
    private SeekBar seekBarWB_photo;
    private SeekBar seekBarISO_photo;
    private SeekBar seekBarShutter_photo;

    private static final int MENU_PHOTO = 0;
    private static final int MENU_VIDEO = 1;
    private static final int MENU_BURST = 2;
    private static final int MENU_POSITION = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_stream);

        goProCombinations = new GoProCombinations(getResources().getStringArray(R.array.val_FPS), getResources().getStringArray(R.array.val_FOV));

        // Setup drawer and drawer button on the left
        setupDrawer();

        // Setup side menu on the right
        setupSideMenu();

        // Setup EV adjustment bar on the bottom
        setupEV();

        // Setup mode menu at the center top
        setupModeMenu();
    }

    private void setupModeMenu(){
        ImageButton buttonPhoto = findViewById(R.id.modePhoto);
        ImageButton buttonVideo = findViewById(R.id.modeVideo);
        ImageButton buttonBurst = findViewById(R.id.modeBurst);
        ImageButton buttonPosition = findViewById(R.id.modePosition);


        buttonPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMenu(MENU_PHOTO);
            }
        });

        buttonVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMenu(MENU_VIDEO);
            }
        });

        buttonBurst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMenu(MENU_BURST);
            }
        });

        buttonPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMenu(MENU_POSITION);
            }
        });

    }

    private void changeMenu(int view){
        // Set all invisible
        for (int i = 0; i < menus.size(); i++){
            menus.get(i).setVisibility(View.INVISIBLE);
        }

        // Set visible the corresponding view
        menus.get(view).setVisibility(View.VISIBLE);

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,46.5F*view, r.getDisplayMetrics());

        View boxView = findViewById(R.id.selectedMode);
        ObjectAnimator animation = ObjectAnimator.ofFloat(boxView, "translationX", px);
        animation.setDuration(500);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.start();
    }

    private void setupEV(){
        SeekBar EV_bar = findViewById(R.id.EV_bar);
        TextView EV_text = findViewById(R.id.EV_val);

        // Place SeekBar in the middle
        EV_bar.setProgress(4);

        // Set text of TextView
        EV_text.setText("EV : " + (EV_bar.getProgress() - 4.)/2);

        // Set callback for SeekBar
        EV_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                EV_text.setText("EV : " + (progress - 4.)/2);
                //TODO : Send new parameter to GoPro
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Animate bar when user clicks on the text
        EV_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = findViewById(R.id.EV_view);
                ObjectAnimator animator;
                if(down_EV){
                    animator = ObjectAnimator.ofFloat(view, "translationY", 60);
                } else {
                    animator = ObjectAnimator.ofFloat(view, "translationY", 0);
                }
                down_EV = !down_EV;
                animator.setDuration(700);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.start();
            }
        });
    }

    private void setupSideMenu(){

        View menuPhoto = findViewById(R.id.menu_photo);
        View menuVideo = findViewById(R.id.menu_video);
        View menuBurst = findViewById(R.id.menu_burst);
        View menuPosition = findViewById(R.id.menu_position);

        menus.add(MENU_PHOTO, menuPhoto);
        menus.add(MENU_VIDEO, menuVideo);
        menus.add(MENU_BURST, menuBurst);
        menus.add(MENU_POSITION, menuPosition);

        menuPhoto.setVisibility(View.VISIBLE);
        menuVideo.setVisibility(View.INVISIBLE);
        menuBurst.setVisibility(View.INVISIBLE);
        menuPosition.setVisibility(View.INVISIBLE);


        //Rotate button on the side of menu each time it is pressed and slide in and out the menu
        mMenuDeployer = findViewById(R.id.menuDeployer);
        mMenuDeployer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = findViewById(R.id.side_menu);
                ObjectAnimator animation_menu;
                ObjectAnimator animation_button;
                float deg;
                float px;
                float dip = 200f;
                Resources r = getResources();

                if(mMenuDeployer.getRotation() == 180F) {
                    deg = 0F;
                    px = 0;
                } else {
                    deg = 180F;
                    px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dip,r.getDisplayMetrics());
                }

                animation_button = ObjectAnimator.ofFloat(mMenuDeployer, "rotation", deg);
                animation_menu = ObjectAnimator.ofFloat(view, "translationX", px);

                animation_menu.setDuration(700);
                animation_button.setDuration(700);

                animation_button.setInterpolator(new AccelerateDecelerateInterpolator());
                animation_menu.setInterpolator(new AccelerateDecelerateInterpolator());

                animation_menu.start();
                animation_button.start();
            }
        });

        videoMenuSetup();

        photoSetup();
    }


    private void setupDrawer() {
        //Add Transparent drawer to have access to floating menu
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        // Handle navigation view item clicks here.
                        int id = item.getItemId();
                        if (id != R.id.nav_livestream) {//exclude himself
                            DrawerHandler dh = new DrawerHandler();

                            Intent intent;
                            intent = dh.SwitchActivity(id, LiveStreamActivity.this);
                            LiveStreamActivity.this.startActivity(intent);
                        }
                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        drawer.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_live);
        //set drawer button
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        getSupportActionBar().setTitle(null);
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DrawerHandler dh = new DrawerHandler();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        dh.setUserDrawer(navigationView);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
        }


        return super.onOptionsItemSelected(item);
    }

    private void photoSetup(){
        spinner_res_photo = findViewById(R.id.spinner_res_photo);
        spinner_FOV_photo = findViewById(R.id.spinner_FOV_photo);

        ArrayAdapter<CharSequence> adapter_res = ArrayAdapter.createFromResource(getApplicationContext(), R.array.val_res_photo, R.layout.spinner_item);
        ArrayAdapter<CharSequence> adapter_FOV = ArrayAdapter.createFromResource(getApplicationContext(), R.array.val_FOV, R.layout.spinner_item);

        adapter_res.setDropDownViewResource(R.layout.spinner_dropdown);
        adapter_FOV.setDropDownViewResource(R.layout.spinner_dropdown);

        spinner_res_photo.setAdapter(adapter_res);
        spinner_FOV_photo.setAdapter(adapter_FOV);

        switchWB_photo = findViewById(R.id.switchWB_photo);
        switchISO_photo = findViewById(R.id.switchISO_photo);
        switchShutter_photo = findViewById(R.id.switchShutter_photo);

        seekBarWB_photo = findViewById(R.id.seekBarWB_photo);
        seekBarISO_photo = findViewById(R.id.seekBarISO_photo);
        seekBarShutter_photo = findViewById(R.id.seekBarShutter_photo);

        seekBarWB_photo.setEnabled(false);
        seekBarISO_photo.setEnabled(false);
        seekBarShutter_photo.setEnabled(false);

        setupCallbackPhoto();
    }

    private void setupCallbackPhoto(){

        spinner_res_photo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //TODO : send data to GopPro
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_FOV_photo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //TODO send data to GoPro
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        switchWB_photo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    seekBarWB_photo.setEnabled(false);
                } else {
                    seekBarWB_photo.setEnabled(true);
                }
            }
        });

        switchISO_photo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    seekBarISO_photo.setEnabled(false);
                } else {
                    seekBarISO_photo.setEnabled(true);
                }
            }
        });

        switchShutter_photo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    seekBarShutter_photo.setEnabled(false);
                } else {
                    seekBarShutter_photo.setEnabled(true);
                }
            }
        });
    }

    private void videoMenuSetup(){

        // Add items to spinner of menu
        spinner_res_video = findViewById(R.id.spinner_res_video);
        spinner_FOV_video = findViewById(R.id.spinner_FOV_video);
        spinner_FPS_video = findViewById(R.id.spinner_FPS_video);

        ArrayAdapter<CharSequence> adapter_res = ArrayAdapter.createFromResource(getApplicationContext(), R.array.val_res_video, R.layout.spinner_item);

        FOV_spinner_video = goProCombinations.getFov("1080p");
        FPS_spinner_video = goProCombinations.getFPS("1080p");

        ArrayAdapter<String> adapter_FOV = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, FOV_spinner_video);
        ArrayAdapter<String> adapter_FPS = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, FPS_spinner_video);

        adapter_res.setDropDownViewResource(R.layout.spinner_dropdown);
        adapter_FOV.setDropDownViewResource(R.layout.spinner_dropdown);
        adapter_FPS.setDropDownViewResource(R.layout.spinner_dropdown);

        spinner_res_video.setAdapter(adapter_res);
        spinner_FOV_video.setAdapter(adapter_FOV);
        spinner_FPS_video.setAdapter(adapter_FPS);

        // Set initial state
        spinner_res_video.setSelection(4);
        spinner_FPS_video.setSelection(5);
        spinner_FOV_video.setSelection(2);

        // Setup WB
        switchWB_video = findViewById(R.id.switchWB_video);
        seekBarWB_video = findViewById(R.id.seekBarWB_video);
        seekBarWB_video.setEnabled(false);

        switchISO_video = findViewById(R.id.switchISO_video);
        seekBarISO_video = findViewById(R.id.seekBarISO_video);
        seekBarISO_video.setEnabled(false);

        setupCallbackVideo();

    }


    private void setupCallbackVideo(){
        spinner_res_video.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(LiveStreamActivity.this, "New resolution selected : " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                //TODO : send new parameter to GoPro
                FOV_spinner_video = goProCombinations.getFov(spinner_res_video.getSelectedItem().toString());
                FPS_spinner_video = goProCombinations.getFPS(spinner_res_video.getSelectedItem().toString());

                ArrayAdapter<String> adapter_FOV = new ArrayAdapter<>(parent.getContext(), R.layout.spinner_item, FOV_spinner_video);
                ArrayAdapter<String> adapter_FPS = new ArrayAdapter<>(parent.getContext(),  R.layout.spinner_item, FPS_spinner_video);

                adapter_FOV.setDropDownViewResource(R.layout.spinner_dropdown);
                adapter_FPS.setDropDownViewResource(R.layout.spinner_dropdown);

                spinner_FOV_video.setAdapter(adapter_FOV);
                spinner_FPS_video.setAdapter(adapter_FPS);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_FOV_video.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(LiveStreamActivity.this, "New FOV selected : " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                //TODO : send new parameters to GoPro
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_FPS_video.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(LiveStreamActivity.this, "New FPS rate selected : " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                //TODO : Send data to GoPro.
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        switchWB_video.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    seekBarWB_video.setEnabled(false);
                } else {
                    seekBarWB_video.setEnabled(true);
                }
            }
        });

        switchISO_video.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    seekBarISO_video.setEnabled(false);
                } else {
                    seekBarISO_video.setEnabled(true);
                }
            }
        });

    }
}