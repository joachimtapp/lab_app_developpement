package com.goproapp.goproapp_wear;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class GoProParametersActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ImageButton mMenuDeployer;
    private static boolean down_EV = true;
    private ArrayList<View> menus = new ArrayList<>();

    private SeekBar EV_bar;

    private GoProCombinations goProCombinations;
    private ArrayList<String> FPS_spinner_video;
    private ArrayList<String> FOV_spinner_video;
    private Switch switchWB_video;
    private Switch switchISO_video;
    private Switch switchProTune_video;
    private SeekBar seekBarWB_video;
    private SeekBar seekBarISO_video;
    private LinearLayout proTune_video;
    private Spinner spinner_res_video;
    private Spinner spinner_FPS_video;
    private Spinner spinner_FOV_video;

    private Switch switchWB_photo;
    private Switch switchISO_photo;
    private Switch switchShutter_photo;
    private Switch switchProTune_photo;
    private SeekBar seekBarWB_photo;
    private SeekBar seekBarISO_min_photo;
    private SeekBar seekBarISO_max_photo;
    private SeekBar seekBarShutter_photo;
    private LinearLayout proTune_photo;
    private Spinner spinner_FOV_photo;


    private Spinner spinner_FOV_burst;
    private Spinner spinner_rate_burst;
    private Switch switchProTune_burst;
    private Switch switchWB_burst;
    private Switch switchISO_burst;
    private SeekBar seekBarWB_burst;
    private SeekBar seekBarISO_min_burst;
    private SeekBar seekBarISO_max_burst;
    private LinearLayout proTune_burst;

    private TextView distText;

    private ImageButton shutterButton;
    private Boolean recording = false;

    GoProInterface goProInterface;
    private String MODE;
    private Boolean firstTime=true;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {

            utils.sendAsyncMagicPacket magicPacket = new utils.sendAsyncMagicPacket();
            magicPacket.execute();

            timerHandler.postDelayed(this, 420000);
        }
    };

    private View whiteView;

    private static final int MENU_PHOTO = 0;
    private static final int MENU_VIDEO = 1;
    private static final int MENU_BURST = 2;
    private static final int MENU_POSITION = 3;

    public static final String TRIG_DIST_INT = "TRIG_DIST";
    public static final String TRIG_DIST_VAL = "TRIG_DIST_VAL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gopro_parameters);

        timerHandler.postDelayed(timerRunnable, 0);

        goProCombinations = new GoProCombinations(getResources().getStringArray(R.array.val_FPS), getResources().getStringArray(R.array.val_FOV));
        goProInterface = new GoProInterface();

        shutterButton = findViewById(R.id.shutterButton);
        Animation animation_out = new AlphaAnimation(1.0F, 0.0F);
        animation_out.setDuration(200);
        animation_out.setInterpolator(new AccelerateDecelerateInterpolator());
        animation_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                whiteView.setAlpha(0.0F);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        whiteView = findViewById(R.id.whiteBack);


        shutterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteView.setAlpha(1.0F);
                whiteView.startAnimation(animation_out);



                if(MODE.equals(GoProInterface.MODE_VIDEO)){
                    if(recording){
                        goProInterface.shutterStop();
                        shutterButton.setImageDrawable(getDrawable(R.drawable.shutter_small));
                    } else {
                        goProInterface.shutter();
                        shutterButton.setImageDrawable(getDrawable(R.drawable.shutter_stop_small));
                    }
                    recording = !recording;
                } else {
                    goProInterface.shutter();
                }
            }
        });

        shutterButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                whiteView.setAlpha(1.0F);
                whiteView.startAnimation(animation_out);

                String previous_mode = MODE;

                goProInterface.setMode(GoProInterface.MODE_PHOTO);
                MODE = GoProInterface.MODE_PHOTO;
                goProInterface.shutter();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.e("Shutter", "Start of attempt to change background");
                        new SetBackgroundImage().execute("http://10.5.5.9:8080/gp/gpMediaList");
                    }
                }, 1500);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.e("Shutter", "Deleting last image");
                        goProInterface.deleteLastMedia();
                    }
                }, 4000);

                MODE = previous_mode;
                goProInterface.setMode(previous_mode);

                return true;
            }
        });

        distText = findViewById(R.id.distTrig);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Integer distVal = intent.getIntExtra(TRIG_DIST_VAL, 0);
                distText.setText(distVal.toString());
            }
        }, new IntentFilter(TRIG_DIST_INT));

        // Setup camera
        setupCamera();

        // Setup mode menu at the center top
        setupModeMenu();

        // Setup drawer and drawer button on the left
        setupDrawer();

        // Setup side menu on the right
        setupSideMenu();

        // Setup EV adjustment bar on the bottom
        setupEV();

        new SetBackgroundImage().execute("http://10.5.5.9:8080/gp/gpMediaList");
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        firstTime = sharedPref.getBoolean("parametersFirst", true);
        if(firstTime)
            GuidedTour();
    }

    private void GuidedTour() {
        new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(findViewById(R.id.modeSelectLayout)))
                .setContentTitle("Mode selection")
                .setContentText("The different capture modes can be set here")
                .setStyle(R.style.CustomShowcaseTheme)
                .setShowcaseEventListener(new SimpleShowcaseEventListener() {

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        new ShowcaseView.Builder(GoProParametersActivity.this)
                                .setTarget(new ViewTarget(findViewById(R.id.menuDeployer)))
                                .setContentTitle("Parameters")
                                .setContentText("Every parameter can be tune here")
                                .setStyle(R.style.CustomShowcaseTheme)
                                .setShowcaseEventListener(new SimpleShowcaseEventListener() {

                                    @Override
                                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                                        new ShowcaseView.Builder(GoProParametersActivity.this)
                                                .setTarget(new ViewTarget(findViewById(R.id.shutterButton)))
                                                .setContentTitle("Shutter")
                                                .setContentText("A simple press will take a picture/ start a recording.\n" +
                                                        "A preview can be obtain by maintaining the shutter button.  ")
                                                .setStyle(R.style.CustomShowcaseTheme)
                                                .build();
                                    }
                                })
                                .build();
                    }
                })
                .build()
                .show();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("parametersFirst", false);
        editor.commit();
    }

    @Override
    public void onPause(){
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void setupCamera(){

        goProInterface.setMode(GoProInterface.MODE_PHOTO);

    }

    private void setupModeMenu(){
        ImageButton buttonPhoto = findViewById(R.id.modePhoto);
        ImageButton buttonVideo = findViewById(R.id.modeVideo);
        ImageButton buttonBurst = findViewById(R.id.modeBurst);
        ImageButton buttonPosition = findViewById(R.id.modePosition);

        MODE = GoProInterface.MODE_PHOTO;



        buttonPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MODE = GoProInterface.MODE_PHOTO;
                goProInterface.setMode(MODE);
                changeMenu(MENU_PHOTO);
                goProInterface.setFOVPhoto(spinner_FOV_photo.getSelectedItem().toString());
                goProInterface.setProTune(switchProTune_photo.isChecked(), MODE);
                if(!switchProTune_photo.isChecked()){
                    if(switchWB_photo.isChecked()){
                        goProInterface.setWBAuto(GoProInterface.MODE_PHOTO);
                    } else {
                        goProInterface.setWB(seekBarWB_photo.getProgress(), MODE);
                    }

                    if(switchISO_photo.isChecked()){
                        goProInterface.setISOMin(0, MODE);
                        goProInterface.setISOMax(seekBarISO_max_photo.getMax(), MODE);
                    } else {
                        goProInterface.setISOMin(seekBarISO_min_photo.getProgress(), MODE);
                        goProInterface.setISOMax(seekBarISO_max_photo.getProgress(), MODE);
                    }

                    if(switchShutter_photo.isChecked()){
                        goProInterface.setShutterAuto();
                    } else {
                        goProInterface.setShutter(seekBarShutter_photo.getProgress());
                    }
                }
            }
        });

        buttonVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MODE = GoProInterface.MODE_VIDEO;
                goProInterface.setMode(MODE);
                changeMenu(MENU_VIDEO);
                goProInterface.setResVideo(spinner_res_video.getSelectedItem().toString());
                goProInterface.setFPSVideo(spinner_FPS_video.getSelectedItem().toString());
                goProInterface.setFOVVideo(spinner_FOV_video.getSelectedItem().toString());
                goProInterface.setProTune(switchProTune_video.isChecked(), MODE);
                if(switchProTune_video.isChecked()){
                    if(switchWB_video.isChecked()){
                        goProInterface.setWBAuto(MODE);
                    } else {
                        goProInterface.setWB(seekBarWB_video.getProgress(), MODE);
                    }

                    if(switchISO_video.isChecked()){
                        goProInterface.setISO(seekBarISO_video.getMax());
                        goProInterface.setISOMode(GoProInterface.ISO_MODE_MAX);
                    } else {
                        goProInterface.setISO(seekBarISO_video.getProgress());
                        goProInterface.setISOMode(GoProInterface.ISO_MODE_LOCK);
                    }
                }
            }
        });

        buttonBurst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MODE = GoProInterface.MODE_BURST;
                goProInterface.setMode(MODE);
                changeMenu(MENU_BURST);
                goProInterface.setFOVBurst(spinner_FOV_burst.getSelectedItem().toString());
                goProInterface.setBurstRate(spinner_rate_burst.getSelectedItem().toString());
                goProInterface.setProTune(switchProTune_burst.isChecked(), MODE);
                if(switchProTune_burst.isChecked()){
                    if(switchWB_burst.isChecked()){
                        goProInterface.setWBAuto(MODE);
                    } else {
                        goProInterface.setWB(seekBarWB_burst.getProgress(), MODE);
                    }

                    if(switchISO_burst.isChecked()){
                        goProInterface.setISOMin(0, MODE);
                        goProInterface.setISOMax(seekBarISO_max_burst.getMax(), MODE);
                    } else {
                        goProInterface.setISOMin(seekBarISO_min_burst.getProgress(), MODE);
                        goProInterface.setISOMax(seekBarISO_max_burst.getProgress(), MODE);
                    }
                }
            }
        });

        buttonPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goProInterface.setMode(GoProInterface.MODE_VIDEO);
                MODE = GoProInterface.MODE_VIDEO;
                changeMenu(MENU_POSITION);
                Intent intent = new Intent(GoProParametersActivity.this, WearService.class);
                intent.setAction(WearService.ACTION_SEND.STARTACTIVITY.name());
                intent.putExtra(WearService.ACTIVITY_TO_START, BuildConfig.W_mainactivity);
                startService(intent);
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
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,50F*view, r.getDisplayMetrics());

        View boxView = findViewById(R.id.selectedMode);
        ObjectAnimator animation = ObjectAnimator.ofFloat(boxView, "translationX", px);
        animation.setDuration(500);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.start();
    }

    private void setupEV(){
        EV_bar = findViewById(R.id.EV_bar);
        TextView EV_text = findViewById(R.id.EV_val);

        // Place SeekBar in the middle
        EV_bar.setProgress(4);
        goProInterface.setEV(EV_bar.getProgress(), MODE);

        // Set text of TextView
        EV_text.setText("EV : " + (EV_bar.getProgress() - 4.)/2);

        // Set callback for SeekBar
        EV_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                EV_text.setText("EV : " + (progress - 4.)/2);
                //TODO : Send new parameter to GoPro
                goProInterface.setEV(progress, MODE);
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
                float dip = 230f;
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

        photoMenuSetup();

        burstMenuSetup();
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
                            intent = dh.SwitchActivity(id, GoProParametersActivity.this);
                            GoProParametersActivity.this.startActivity(intent);
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

    private void burstMenuSetup(){
        spinner_FOV_burst = findViewById(R.id.spinner_FOV_burst);
        spinner_rate_burst = findViewById(R.id.spinner_rate_burst);
        switchProTune_burst = findViewById(R.id.switchProTune_burst);
        switchWB_burst = findViewById(R.id.switchWB_burst);
        seekBarWB_burst = findViewById(R.id.seekBarWB_burst);
        switchISO_burst = findViewById(R.id.switchISO_burst);
        seekBarISO_max_burst = findViewById(R.id.seekBarISO_max_burst);
        seekBarISO_min_burst = findViewById(R.id.seekBarISO_min_burst);
        proTune_burst = findViewById(R.id.protune_burst);

        seekBarISO_min_burst.setEnabled(false);
        seekBarISO_max_burst.setEnabled(false);
        seekBarWB_burst.setEnabled(false);

        ArrayAdapter<CharSequence> adapter_FOV = ArrayAdapter.createFromResource(getApplicationContext(), R.array.valFOV_burst, R.layout.spinner_item);
        ArrayAdapter<CharSequence> adapter_rate = ArrayAdapter.createFromResource(getApplicationContext(), R.array.val_burst_rate, R.layout.spinner_item);

        adapter_FOV.setDropDownViewResource(R.layout.spinner_dropdown);
        adapter_rate.setDropDownViewResource(R.layout.spinner_dropdown);

        spinner_FOV_burst.setAdapter(adapter_FOV);
        spinner_rate_burst.setAdapter(adapter_rate);

        switchProTune_burst.setChecked(false);
        proTune_burst.setVisibility(View.GONE);

        setupCallbackBurst();

    }

    private void setupCallbackBurst(){

        spinner_rate_burst.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                goProInterface.setBurstRate(spinner_rate_burst.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_FOV_burst.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                goProInterface.setFOVBurst(spinner_FOV_burst.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        switchProTune_burst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    proTune_burst.setVisibility(View.VISIBLE);

                    // ProTune callbacks

                    if(switchWB_burst.isChecked()){
                        goProInterface.setWBAuto(MODE);
                    } else {
                        goProInterface.setWB(seekBarWB_burst.getProgress(), MODE);
                    }

                    if(switchISO_burst.isChecked()){
                        goProInterface.setISOMin(0, MODE);
                        goProInterface.setISOMax(4, MODE);
                    } else {
                        goProInterface.setISOMin(seekBarISO_min_burst.getProgress(), MODE);
                        goProInterface.setISOMax(seekBarISO_max_burst.getProgress(), MODE);
                    }

                } else {
                    proTune_burst.setVisibility(View.GONE);
                }
                goProInterface.setProTune(isChecked, MODE);
                goProInterface.setEV(EV_bar.getProgress(), MODE);
            }
        });

        switchWB_burst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    seekBarWB_burst.setEnabled(false);
                    goProInterface.setWBAuto(MODE);
                } else {
                    seekBarWB_burst.setEnabled(true);
                    goProInterface.setWB(seekBarWB_burst.getProgress(), MODE);
                }
            }
        });

        switchISO_burst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    seekBarISO_min_burst.setEnabled(false);
                    seekBarISO_max_burst.setEnabled(false);
                    goProInterface.setISOMin(0, MODE);
                    goProInterface.setISOMax(4, MODE);
                } else {
                    seekBarISO_min_burst.setEnabled(true);
                    seekBarISO_max_burst.setEnabled(true);
                    goProInterface.setISOMin(seekBarISO_min_burst.getProgress(), MODE);
                    goProInterface.setISOMax(seekBarISO_max_burst.getProgress(), MODE);
                }
            }
        });

        seekBarWB_burst.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                goProInterface.setWB(progress, MODE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarISO_min_burst.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress > seekBarISO_max_burst.getProgress()){
                    seekBarISO_max_burst.setProgress(progress);
                }
                goProInterface.setISOMin(progress, MODE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarISO_max_burst.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress < seekBarISO_min_burst.getProgress()){
                    seekBarISO_min_burst.setProgress(progress);
                }
                goProInterface.setISOMax(progress, MODE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    private void photoMenuSetup(){
        spinner_FOV_photo = findViewById(R.id.spinner_FOV_photo);
        switchWB_photo = findViewById(R.id.switchWB_photo);
        switchISO_photo = findViewById(R.id.switchISO_photo);
        switchShutter_photo = findViewById(R.id.switchShutter_photo);
        seekBarWB_photo = findViewById(R.id.seekBarWB_photo);
        seekBarISO_min_photo = findViewById(R.id.seekBarISO_min_photo);
        seekBarISO_max_photo = findViewById(R.id.seekBarISO_max_photo);
        seekBarShutter_photo = findViewById(R.id.seekBarShutter_photo);
        proTune_photo = findViewById(R.id.protune_photo);

        ArrayAdapter<CharSequence> adapter_FOV = ArrayAdapter.createFromResource(getApplicationContext(), R.array.val_FOV_photo, R.layout.spinner_item);

        adapter_FOV.setDropDownViewResource(R.layout.spinner_dropdown);

        spinner_FOV_photo.setAdapter(adapter_FOV);

        seekBarWB_photo.setEnabled(false);
        seekBarISO_min_photo.setEnabled(false);
        seekBarISO_max_photo.setEnabled(false);
        seekBarShutter_photo.setEnabled(false);

        switchProTune_photo = findViewById(R.id.switchProTune_photo);

        switchProTune_photo.setChecked(false);
        proTune_photo.setVisibility(View.GONE);
        goProInterface.setProTune(false, MODE);

        setupCallbackPhoto();
    }

    private void setupCallbackPhoto(){

        switchProTune_photo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    proTune_photo.setVisibility(View.VISIBLE);

                    // Call all ProTune callbacks
                    if(switchWB_photo.isChecked()){
                        goProInterface.setWBAuto(MODE);
                    } else {
                        goProInterface.setWB(seekBarWB_photo.getProgress(), MODE);
                    }

                    if(switchISO_photo.isChecked()){
                        goProInterface.setISOMin(0, MODE);
                        goProInterface.setISOMax(4, MODE);
                    } else {
                        goProInterface.setISOMin(seekBarISO_min_photo.getProgress(), MODE);
                        goProInterface.setISOMax(seekBarISO_max_photo.getProgress(), MODE);
                    }

                    if(switchShutter_photo.isChecked()){
                        goProInterface.setShutterAuto();
                    } else {
                        goProInterface.setShutter(seekBarShutter_photo.getProgress());
                    }

                } else {
                    proTune_photo.setVisibility(View.GONE);
                }
                goProInterface.setProTune(isChecked, MODE);
                goProInterface.setEV(EV_bar.getProgress(), MODE);
            }
        });

        spinner_FOV_photo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //TODO send data to GoPro
                goProInterface.setFOVPhoto(spinner_FOV_photo.getSelectedItem().toString());
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
                    goProInterface.setWBAuto(MODE);
                } else {
                    seekBarWB_photo.setEnabled(true);
                    goProInterface.setWB(seekBarWB_photo.getProgress(), MODE);
                }
            }
        });

        seekBarWB_photo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                goProInterface.setWB(progress, MODE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        switchISO_photo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    seekBarISO_min_photo.setEnabled(false);
                    seekBarISO_max_photo.setEnabled(false);
                    goProInterface.setISOMin(0, MODE);
                    goProInterface.setISOMax(4, MODE);
                } else {
                    seekBarISO_min_photo.setEnabled(true);
                    seekBarISO_max_photo.setEnabled(true);
                    goProInterface.setISOMin(seekBarISO_min_photo.getProgress(), MODE);
                    goProInterface.setISOMax(seekBarISO_max_photo.getProgress(), MODE);
                }
            }
        });

        switchShutter_photo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    seekBarShutter_photo.setEnabled(false);
                    goProInterface.setShutterAuto();
                } else {
                    seekBarShutter_photo.setEnabled(true);
                    goProInterface.setShutter(seekBarShutter_photo.getProgress());
                }
            }
        });

        seekBarShutter_photo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                goProInterface.setShutter(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarISO_max_photo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress < seekBarISO_min_photo.getProgress()){
                    seekBarISO_min_photo.setProgress(progress);
                }
                goProInterface.setISOMax(progress, MODE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarISO_min_photo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress > seekBarISO_max_photo.getProgress()){
                    seekBarISO_max_photo.setProgress(progress);
                }
                goProInterface.setISOMin(progress, MODE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void videoMenuSetup(){

        // Add items to spinner of menu
        spinner_res_video = findViewById(R.id.spinner_res_video);
        spinner_FOV_video = findViewById(R.id.spinner_FOV_video);
        spinner_FPS_video = findViewById(R.id.spinner_FPS_video);
        switchWB_video = findViewById(R.id.switchWB_video);
        seekBarWB_video = findViewById(R.id.seekBarWB_video);
        switchISO_video = findViewById(R.id.switchISO_video);
        seekBarISO_video = findViewById(R.id.seekBarISO_video);
        switchProTune_video = findViewById(R.id.switchProTune_video);
        proTune_video = findViewById(R.id.protune_video);

        ArrayAdapter<CharSequence> adapter_res = ArrayAdapter.createFromResource(getApplicationContext(), R.array.val_res_video, R.layout.spinner_item);
        adapter_res.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner_res_video.setAdapter(adapter_res);
        spinner_res_video.setSelection(3);


        FPS_spinner_video = goProCombinations.getFPS(spinner_res_video.getSelectedItem().toString());
        ArrayAdapter<String> adapter_FPS = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, FPS_spinner_video);
        adapter_FPS.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner_FPS_video.setAdapter(adapter_FPS);
        spinner_FPS_video.setSelection(3);


        FOV_spinner_video = goProCombinations.getFov(spinner_res_video.getSelectedItem().toString(), spinner_FPS_video.getSelectedItem().toString());
        ArrayAdapter<String> adapter_FOV = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, FOV_spinner_video);
        adapter_FOV.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner_FOV_video.setAdapter(adapter_FOV);
        spinner_FOV_video.setSelection(3);

        spinner_res_video.setSelection(3);
        spinner_FPS_video.setSelection(3);
        spinner_FOV_video.setSelection(3);

        // Setup WB
        seekBarWB_video.setEnabled(false);
        seekBarISO_video.setEnabled(false);

        switchProTune_video.setChecked(false);
        proTune_video.setVisibility(View.GONE);
        goProInterface.setProTune(switchProTune_video.isChecked(), MODE);

        setupCallbackVideo();

    }

    private void setupCallbackVideo(){

        switchProTune_video.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    proTune_video.setVisibility(View.VISIBLE);

                    // Callbacks of ProTune

                    if(switchWB_video.isChecked()){
                        goProInterface.setWBAuto(MODE);
                    } else {
                        goProInterface.setWB(seekBarWB_video.getProgress(), MODE);
                    }

                    if(switchISO_video.isChecked()){
                        goProInterface.setISO(2);
                    } else {
                        goProInterface.setISO(seekBarISO_video.getProgress());
                    }
                } else {
                    proTune_video.setVisibility(View.GONE);
                }
                goProInterface.setProTune(isChecked, MODE);
                goProInterface.setEV(EV_bar.getProgress(), MODE);
            }
        });

        spinner_res_video.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(GoProParametersActivity.this, "New resolution selected : " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                //TODO : send new parameter to GoPro
                FPS_spinner_video = goProCombinations.getFPS(spinner_res_video.getSelectedItem().toString());
                FOV_spinner_video = goProCombinations.getFov(spinner_res_video.getSelectedItem().toString(), spinner_FPS_video.getSelectedItem().toString());


                ArrayAdapter<String> adapter_FOV = new ArrayAdapter<>(parent.getContext(), R.layout.spinner_item, FOV_spinner_video);
                ArrayAdapter<String> adapter_FPS = new ArrayAdapter<>(parent.getContext(),  R.layout.spinner_item, FPS_spinner_video);

                adapter_FOV.setDropDownViewResource(R.layout.spinner_dropdown);
                adapter_FPS.setDropDownViewResource(R.layout.spinner_dropdown);

                spinner_FOV_video.setAdapter(adapter_FOV);
                spinner_FPS_video.setAdapter(adapter_FPS);

                goProInterface.setResVideo(spinner_res_video.getSelectedItem().toString());
                goProInterface.setFPSVideo(spinner_FPS_video.getSelectedItem().toString());
                goProInterface.setFOVVideo(spinner_FOV_video.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_FOV_video.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(GoProParametersActivity.this, "New FOV selected : " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                //TODO : send new parameters to GoPro
                goProInterface.setFOVVideo(spinner_FOV_video.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_FPS_video.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(GoProParametersActivity.this, "New FPS rate selected : " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                //TODO : Send data to GoPro.
                FOV_spinner_video = goProCombinations.getFov(spinner_res_video.getSelectedItem().toString(), spinner_FPS_video.getSelectedItem().toString());

                ArrayAdapter<String> adapter_FOV = new ArrayAdapter<>(parent.getContext(), R.layout.spinner_item, FOV_spinner_video);

                adapter_FOV.setDropDownViewResource(R.layout.spinner_dropdown);

                spinner_FOV_video.setAdapter(adapter_FOV);

                goProInterface.setFPSVideo(spinner_FPS_video.getSelectedItem().toString());
                goProInterface.setFOVVideo(spinner_FOV_video.getSelectedItem().toString());
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
                    goProInterface.setWBAuto(MODE);
                    goProInterface.setISOMode(GoProInterface.ISO_MODE_MAX);
                } else {
                    seekBarWB_video.setEnabled(true);
                    goProInterface.setWB(seekBarWB_video.getProgress(), MODE);
                    goProInterface.setISOMode(GoProInterface.ISO_MODE_LOCK);
                }
            }
        });

        seekBarWB_video.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                goProInterface.setWB(progress, MODE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        switchISO_video.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    seekBarISO_video.setEnabled(false);
                    goProInterface.setISO(2);
                    goProInterface.setISOMode(GoProInterface.ISO_MODE_MAX);
                } else {
                    seekBarISO_video.setEnabled(true);
                    goProInterface.setISO(seekBarISO_video.getProgress());
                    goProInterface.setISOMode(GoProInterface.ISO_MODE_LOCK);
                }
            }
        });

        seekBarISO_video.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                goProInterface.setISO(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private class GoProInterface {

        private OkHttpClient client = new OkHttpClient();

        static final String MODE_VIDEO = "VIDEO";
        static final String MODE_PHOTO = "PHOTO";
        static final String MODE_BURST = "BURST";
        static final String ISO_MODE_LOCK = "LOCK";
        static final String ISO_MODE_MAX = "MAX";

        public GoProInterface() {


            Log.e("GoPro", "Created new instance of GoProInterface");

            // Activate GPS Tag
            sendRequest("http://10.5.5.9/gp/gpControl/setting/83/1", false);

        }

        public void deleteLastMedia(){
            sendRequest("http://10.5.5.9/gp/gpControl/command/storage/delete/last", false);
        }

        public void shutter() {
            sendRequest("http://10.5.5.9/gp/gpControl/command/shutter?p=1", true);
        }

        public void shutterStop() {
            sendRequest("http://10.5.5.9/gp/gpControl/command/shutter?p=0", true);
        }

        public void setMode(String mode) {
            String url;

            switch (mode) {
                case MODE_VIDEO:
                    url = "http://10.5.5.9/gp/gpControl/command/sub_mode?mode=0&sub_mode=0";
                    break;
                case MODE_PHOTO:
                    url = "http://10.5.5.9/gp/gpControl/command/sub_mode?mode=1&sub_mode=1";
                    break;
                case MODE_BURST:
                    url = "http://10.5.5.9/gp/gpControl/command/sub_mode?mode=2&sub_mode=0";
                    break;
                default:
                    url = "";
            }

            sendRequest(url, false);

        }

        public void setFOVPhoto(String fov) {

            String url;

            switch (fov) {
                case "Narrow":
                    url = "http://10.5.5.9/gp/gpControl/setting/17/9";
                    break;
                case "Linear":
                    url = "http://10.5.5.9/gp/gpControl/setting/17/10";
                    break;
                case "Medium":
                    url = "http://10.5.5.9/gp/gpControl/setting/17/8";
                    break;
                case "Wide":
                    url = "http://10.5.5.9/gp/gpControl/setting/17/0";
                    break;
                default:
                    url = "";
            }

            sendRequest(url, false);

        }

        public void setProTune(Boolean enable, String mode) {
            String url = "http://10.5.5.9/gp/gpControl/setting/";
            switch (mode) {
                case MODE_PHOTO:
                    url = url + "21/";
                    break;
                case MODE_VIDEO:
                    url = url + "10/";
                    break;
                case MODE_BURST:
                    url = url + "34/";
                    break;
            }

            if (enable) {
                url = url + "1";
            } else {
                url = url + "0";
            }

            sendRequest(url, false);

        }

        public void setWBAuto(String mode) {
            String url = "http://10.5.5.9/gp/gpControl/setting/";
            switch (mode) {
                case MODE_PHOTO:
                    url = url + "22/";
                    break;
                case MODE_VIDEO:
                    url = url + "11/";
                    break;
                case MODE_BURST:
                    url = url + "35/";
                    break;
            }
            url = url + "0";
            sendRequest(url, false);
        }

        public void setISOMode(String mode) {
            String url = "http://10.5.5.9/gp/gpControl/setting/74/";
            switch (mode) {
                case ISO_MODE_LOCK:
                    url = url + "1";
                    break;
                case ISO_MODE_MAX:
                    url = url + "0";
                    break;
            }
            sendRequest(url, false);
        }

        public void setISO(Integer iso) {
            String url = "http://10.5.5.9/gp/gpControl/setting/13/";
            switch (iso) {
                case 0:
                    url = url + "2";
                    break;
                case 1:
                    url = url + "4";
                    break;
                case 2:
                    url = url + "1";
                    break;
                case 3:
                    url = url + "3";
                    break;
                case 4:
                    url = url + "0";
                    break;
            }
            sendRequest(url, false);
        }

        public void setISOMin(Integer iso_min, String mode) {
            String url = "http://10.5.5.9/gp/gpControl/setting/";

            switch (mode) {
                case MODE_PHOTO:
                    url = url + "75/";
                    break;
                case MODE_BURST:
                    url = url + "76/";
                    break;
            }
            switch (iso_min) {
                case 0:
                    url = url + "3";
                    break;
                case 1:
                    url = url + "2";
                    break;
                case 2:
                    url = url + "1";
                    break;
                case 3:
                    url = url + "0";
                    break;
                case 4:
                    url = url + "4";
                    break;
                default:
                    url = url + "4";
            }

            sendRequest(url, false);
        }

        public void setISOMax(Integer iso_max, String mode) {
            String url = "http://10.5.5.9/gp/gpControl/setting/";

            switch (mode) {
                case MODE_PHOTO:
                    url = url + "24/";
                    break;
                case MODE_BURST:
                    url = url + "37/";
                    break;
            }
            switch (iso_max) {
                case 0:
                    url = url + "3";
                    break;
                case 1:
                    url = url + "2";
                    break;
                case 2:
                    url = url + "1";
                    break;
                case 3:
                    url = url + "0";
                    break;
                case 4:
                    url = url + "4";
                    break;
                default:
                    url = url + "4";
            }

            sendRequest(url, false);
        }

        public void setShutterAuto() {
            sendRequest("http://10.5.5.9/gp/gpControl/setting/97/0", false);
        }

        public void setShutter(Integer shutter) {
            String url = "http://10.5.5.9/gp/gpControl/setting/97/";
            shutter = shutter + 1;
            url = url + shutter.toString();
            sendRequest(url, false);
        }

        public void setResVideo(String resolution) {
            String url = "http://10.5.5.9/gp/gpControl/setting/2/";
            switch (resolution) {
                case "480p":
                    url = url + "17";
                    break;
                case "720p":
                    url = url + "12";
                    break;
                case "960p":
                    url = url + "10";
                    break;
                case "1080p":
                    url = url + "9";
                    break;
                case "1440p":
                    url = url + "7";
                    break;
                case "2.7K 4:3":
                    url = url + "6";
                    break;
                case "2.7K":
                    url = url + "4";
                    break;
                case "4K":
                    url = url + "1";
                    break;
                default:
                    url = url + "9";
                    break;
            }

            sendRequest(url, false);
        }

        public void setFPSVideo(String fps) {
            String url = "http://10.5.5.9/gp/gpControl/setting/3/";

            switch (fps) {
                case "24 FPS":
                    url = url + "9";
                    break;
                case "30 FPS":
                    url = url + "8";
                    break;
                case "48 FPS":
                    url = url + "7";
                    break;
                case "60 FPS":
                    url = url + "5";
                    break;
                case "80 FPS":
                    url = url + "4";
                    break;
                case "90 FPS":
                    url = url + "3";
                    break;
                case "100 FPS":
                    url = url + "2";
                    break;
                case "120 FPS":
                    url = url + "1";
                    break;
                case "240 FPS":
                    url = url + "0";
                    break;
                default:
                    url = url + "5";
                    break;
            }

            sendRequest(url, false);
        }

        public void setEV(Integer ev, String mode) {
            String url = "http://10.5.5.9/gp/gpControl/setting/";

            switch (mode) {
                case MODE_PHOTO:
                    url = url + "26/";
                    break;
                case MODE_VIDEO:
                    url = url + "15/";
                    break;
                case MODE_BURST:
                    url = url + "39/";
                    break;
            }
            ev = 8 - ev;
            url = url + ev.toString();

            sendRequest(url, false);
        }

        public void setFOVVideo(String fov) {
            String url = "http://10.5.5.9/gp/gpControl/setting/4/";
            switch (fov) {
                case "Narrow":
                    url = url + "2";
                    break;
                case "Linear":
                    url = url + "4";
                    break;
                case "Medium":
                    url = url + "1";
                    break;
                case "Wide":
                    url = url + "0";
                    break;
                case "Super View":
                    url = url + "3";
                    break;
                default:
                    url = url + "0";
                    break;
            }

            sendRequest(url, false);

        }

        public void setBurstRate(String burstRate) {
            String url = "http://10.5.5.9/gp/gpControl/setting/29/";
            switch (burstRate) {
                case "3/1":
                    url = url + "0";
                    break;
                case "5/1":
                    url = url + "1";
                    break;
                case "10/1":
                    url = url + "2";
                    break;
                case "10/2":
                    url = url + "3";
                    break;
                case "10/3":
                    url = url + "4";
                    break;
                case "30/1":
                    url = url + "5";
                    break;
                case "30/2":
                    url = url + "6";
                    break;
                case "30/3":
                    url = url + "7";
                    break;
                case "30/6":
                    url = url + "8";
                    break;
            }

            sendRequest(url, false);
        }

        public void setFOVBurst(String fov) {
            String url;

            switch (fov) {
                case "Narrow":
                    url = "http://10.5.5.9/gp/gpControl/setting/28/9";
                    break;
                case "Linear":
                    url = "http://10.5.5.9/gp/gpControl/setting/28/10";
                    break;
                case "Medium":
                    url = "http://10.5.5.9/gp/gpControl/setting/28/8";
                    break;
                case "Wide":
                    url = "http://10.5.5.9/gp/gpControl/setting/28/0";
                    break;
                default:
                    url = "";
            }

            sendRequest(url, false);
        }

        public void setWB(Integer wb, String mode) {
            String url = "http://10.5.5.9/gp/gpControl/setting/";
            switch (mode) {
                case MODE_VIDEO:
                    url = url + "11/";
                    break;
                case MODE_PHOTO:
                    url = url + "22/";
                    break;
                case MODE_BURST:
                    url = url + "35/";
                    break;
            }
            switch (wb) {
                case 0:
                    url = url + "1";
                    break;
                case 1:
                    url = url + "5";
                    break;
                case 2:
                    url = url + "6";
                    break;
                case 3:
                    url = url + "2";
                    break;
                case 4:
                    url = url + "7";
                    break;
                case 5:
                    url = url + "3";
                    break;
                default:
                    url = url + "1";
            }

            sendRequest(url, false);
        }

        private void sendRequest(String url, boolean isShutter) {

            Request startpreview = new Request.Builder()
                    .url(HttpUrl.get(URI.create(url)))
                    .build();

            client.newCall(startpreview).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    //e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.d("GoPro", "Camera not connected");
                    }
                    if(isShutter){

                        long delay;
                        switch (MODE){
                            case MODE_BURST:
                                int time = Integer.parseInt(spinner_rate_burst.getSelectedItem().toString().split("/")[1]);
                                int nbImages = Integer.parseInt(spinner_rate_burst.getSelectedItem().toString().split("/")[0]);
                                delay = time*1000 + nbImages*400 + 1000;
                                break;
                            default:
                                delay = 2000;
                        }
                        // Launch task after some delay to wait for GoPro to be ready again
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.e("Shutter", "Start of attempt to change background");
                                new SetBackgroundImage().execute("http://10.5.5.9:8080/gp/gpMediaList");
                            }
                        }, delay);
                    }
                }
            });
        }
    }

    private class SetBackgroundImage extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                Log.e("debug", "Attempting to get datalist");

                URL url = new URL(arg0[0]); // here is your URL path

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }
                    in.close();

                    Log.e("debug", "result: " + sb.toString());
                    return sb.toString();

                } else {
                    return "Error";
                }
            } catch (Exception e) {
                return "Error";
            }
        }

        @Override
        protected void onPostExecute(String result) {

            Log.e("debug", "result: " + result);
            String lastImageName;

            //recover data names
            try {
                JSONObject obj = new JSONObject(result);
                JSONArray array = obj.getJSONArray("media").getJSONObject(0).getJSONArray("fs");
                String goProDir = obj.getJSONArray("media").getJSONObject(0).getString("d");
                JSONObject media = array.getJSONObject(array.length() - 1);
                Log.e("debug", "dir= " + goProDir + " name= " + media.getString("n"));
                lastImageName = media.getString("n");

                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(lastImageName);

            } catch (Throwable tx) {//recover pic info

            }
        }

    }

    private class DownloadTask extends AsyncTask<String, Void, Bitmap> {
        // Before the tasks execution
        protected void onPreExecute() {
            // Display the progress dialog on async task start
        }

        // Do the task in background/non UI thread
        protected Bitmap doInBackground(String... params) {
            URL url = null;
            try {
                if(params[0].contains(".MP4")){
                    url = new URL("http://10.5.5.9/gp/gpMediaMetadata?p=100GOPRO/" + params[0]);
                } else {
                    url = new URL("http://10.5.5.9:8080/videos/DCIM/100GOPRO/" + params[0]);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection connection = null;

            try {
                // Initialize a new http url connection
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                return bmp;

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Disconnect the http url connection
                connection.disconnect();
            }
            return null;
        }

        // When all async task done
        protected void onPostExecute(Bitmap result) {
            // Hide the progress dialog

            if (result != null) {
                // Display the downloaded image into ImageView
                ImageView backgroundImage = findViewById(R.id.backgroundImage);
                backgroundImage.setImageBitmap(result);
            } else {
                // Notify user that an error occurred while downloading image
            }
        }
    }

}