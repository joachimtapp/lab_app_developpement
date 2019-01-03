package com.goproapp.goproapp_wear;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class LiveStreamActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ImageButton mMenuDeployer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_stream);

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


        // Add items to spinner of menu
        Spinner spinner_res = findViewById(R.id.spinner_res);
        Spinner spinner_FOV = findViewById(R.id.spinner_FOV);
        Spinner spinner_FPS = findViewById(R.id.spinner_FPS);

        ArrayAdapter<CharSequence> adapter_res = ArrayAdapter.createFromResource(this, R.array.val_res, R.layout.spinner_item);
        ArrayAdapter<CharSequence> adapter_FOV = ArrayAdapter.createFromResource(this, R.array.val_FOV, R.layout.spinner_item);
        ArrayAdapter<CharSequence> adapter_FPS = ArrayAdapter.createFromResource(this, R.array.val_FPS, R.layout.spinner_item);

        adapter_res.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_FOV.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_FPS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_res.setAdapter(adapter_res);
        spinner_FOV.setAdapter(adapter_FOV);
        spinner_FPS.setAdapter(adapter_FPS);


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

                //mMenuDeployer.animate().rotation(deg).setInterpolator(new AccelerateDecelerateInterpolator());

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


        spinner_res.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(LiveStreamActivity.this, "New resolution selected : " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                //TODO : Update other spinners according to new resolution and send new parameter to GoPro
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_FOV.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(LiveStreamActivity.this, "New FOV selected : " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                //TODO : Update other spinners according to new FOV and send new parameters to GoPro
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_FPS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(LiveStreamActivity.this, "New FPS rate selected : " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                //TODO : Send data to GoPro.
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        SeekBar EV_bar = findViewById(R.id.EV_bar);
        TextView EV_text = findViewById(R.id.EV_val);

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
}
