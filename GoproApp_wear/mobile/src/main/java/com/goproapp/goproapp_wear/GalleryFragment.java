package com.goproapp.goproapp_wear;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class GalleryFragment extends Fragment {

    private View fragmentView;
    private ImageAdapter adapter;
    private GridView gridView;
    private TextView dateView;
    private TextView swipeHint;
    private LinearLayout linearLayoutInfo;
    private MapView mapView;
    private Marker marker;
    private SwipeRefreshLayout mSwipeRefresh;
    private View uploadProgressBar;
    private FloatingActionButton uploadBtn;

    public static MapboxMap mMapboxMap;

    private MyFirebaseDataListener mFirebaseDataListListener;
    private DatabaseReference databaseRef;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private List<String> localData = new ArrayList<>();
    private List<String> goProData = new ArrayList<>();
    private List<Integer> imgDataToUpload = new ArrayList<>();
    private String goProDir;
    private LatLng lastLatLng;
    private View lastSelectedView;
    private Boolean firstTime;

    public GalleryFragment() {
        // Required empty public constructor
    }

    //Mapbox required overrides
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public static GalleryFragment newInstance() {
        GalleryFragment fragment = new GalleryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getContext(), getString(R.string.accessToken));
    }

    //launch the guided tour if first time
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        firstTime = sharedPref.getBoolean("galleryFirst", true);
        if (firstTime)
            GuidedTour();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getGPSPosition(); //launch position listener
        fragmentView = inflater.inflate(R.layout.fragment_gallery, container, false);
        gridView = fragmentView.findViewById(R.id.galleryThumbnails);
        dateView = fragmentView.findViewById(R.id.galleryDateValue);
        swipeHint = fragmentView.findViewById(R.id.swipeHint);
        mapView = fragmentView.findViewById(R.id.gallery_map);
        linearLayoutInfo = fragmentView.findViewById(R.id.linearLayoutInfo);
        uploadProgressBar = fragmentView.findViewById(R.id.uploadProgressBar);
        uploadBtn = fragmentView.findViewById(R.id.uploadBtn);
        mSwipeRefresh = fragmentView.findViewById(R.id.swiperefresh);

        //check the saved data
        localData.clear();
        for (int i = 0; i < GalleryActivity.imgData.size(); i++) {
            localData.add(GalleryActivity.imgData.get(i).name);
        }

        if (GalleryActivity.imgData.size() > 0) {//consider the case of already downloaded images
            swipeHint.setAlpha(0.0f);
            adapter = new ImageAdapter(getContext());
            gridView.setAdapter(adapter);
        }
        //upload data to cloud if possible
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LoginActivity.userID != null) {
                    WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo info = wifiManager.getConnectionInfo();
                    String ssid = info.getSSID();
                    if (ssid.equals(MainActivity.gopro_ssid)) {
                        Toast.makeText(getContext(), "You are currently connected to the GoPro", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    UploadImage();
                } else
                    Toast.makeText(getContext(), "Log in for dataBase synchronization", Toast.LENGTH_LONG).show();
            }
        });

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetGoProMediaList();
                if (LoginActivity.userID != null)
                    GetFirebaseMediaList();
                else {
                    mSwipeRefresh.setRefreshing(false);
                }
            }
        });
        //initialize map
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mMapboxMap = mapboxMap;
                mapboxMap.getUiSettings().setAttributionEnabled(false);
                mapboxMap.getUiSettings().setLogoEnabled(false);
                if (GalleryActivity.imgData.size() > 0) {
                    //set initial map position
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(new LatLng(46.526732, 6.611953))// Sets the new camera position
                            .zoom(0) // Sets the zoom
                            .bearing(0) // Rotate the camera
                            .tilt(0) // Set the camera tilt
                            .build(); // Creates a CameraPosition from the builder
                    mMapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(camPos), 2000);
                }
            }
        });
        //handle image suppression
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Deleting image")
                        .setMessage("Are you sure you want to delete?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = GalleryActivity.imgData.get(position).name.replace(".", "_");
                                if (LoginActivity.userID != null)
                                    FirebaseDatabase.getInstance().getReference().child("users").child(LoginActivity.userID).child("Data").child(name).removeValue();
                                GalleryActivity.imgData.remove(position);
                                adapter = new ImageAdapter(getContext());
                                gridView.setAdapter(adapter);
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
                return false;
            }
        });

        //handle image click
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                //highlight selected picture
                if (lastSelectedView != null)
                    lastSelectedView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
                v.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                lastSelectedView = v;
                //set picture info
                dateView.setText(GalleryActivity.imgData.get(position).date);
                linearLayoutInfo.setVisibility(View.VISIBLE);

                if (mMapboxMap != null) {
                    //move marker and create it if necessary
                    if (marker == null) {
                        if (GalleryActivity.imgData.get(position).latLng != null) {

                            CameraPosition camPos = new CameraPosition.Builder()
                                    .target(GalleryActivity.imgData.get(position).latLng)// Sets the new camera position
                                    .zoom(15) // Sets the zoom
                                    .bearing(0) // Rotate the camera
                                    .tilt(0) // Set the camera tilt
                                    .build(); // Creates a CameraPosition from the builder
                            mMapboxMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(camPos), 2000);
                            marker = mMapboxMap.addMarker(new MarkerOptions().position(GalleryActivity.imgData.get(position).latLng));
                        }
                    } else {
                        if (GalleryActivity.imgData.get(position).latLng != null) {
                            marker.setPosition(GalleryActivity.imgData.get(position).latLng);
                            // move camera
                            CameraPosition camPos = new CameraPosition.Builder()
                                    .target(GalleryActivity.imgData.get(position).latLng)// Sets the new camera position
                                    .zoom(mMapboxMap.getCameraPosition().zoom) // Sets the zoom
                                    .bearing(0) // Rotate the camera
                                    .tilt(0) // Set the camera tilt
                                    .build(); // Creates a CameraPosition from the builder
                            mMapboxMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(camPos), 2000);
                        }
                    }
                }
            }
        });
        return fragmentView;
    }

    private void getGPSPosition() {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                lastLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android"
                    + ".permission.ACCESS_COARSE_LOCATION", "android.permission.INTERNET"}, 0);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }


    //Shows and animate progressbar
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            uploadProgressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    uploadProgressBar.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            uploadProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            uploadProgressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    uploadProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            uploadProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void UploadImage() {
        //initialize list to upload
        if (imgDataToUpload.isEmpty()) {
            for (int i = 0; i < GalleryActivity.imgData.size(); i++) {
                if (!GalleryActivity.imgData.get(i).online)
                    imgDataToUpload.add(i);
            }
            if (imgDataToUpload.isEmpty()) {
                return;
            }
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();
        }
        showProgress(true);
        uploadBtn.hide();

        int id = imgDataToUpload.get(0);
        String imgString = GalleryActivity.imgData.get(id).imgString;
        StorageReference ref = storageReference.child(LoginActivity.userID).child(GalleryActivity.imgData.get(id).name);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        getBitmapFromString(imgString).compress(Bitmap.CompressFormat.PNG, 0, bos);
        //remove the point not allowed on firebase
        String name = GalleryActivity.imgData.get(id).name.replace(".", "_");
        databaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(LoginActivity.userID).child("Data").child(name);
        //send image bytes to firebase
        ref.putBytes(bos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference()
                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    //wait to get the uploaded image URL
                    @Override
                    public void onSuccess(final Uri uri) {
                        GalleryActivity.imgData.get(id).imgUrl = uri.toString();
                        databaseRef.child("picture").setValue(GalleryActivity.imgData.get(id).imgUrl);
                        Log.v("Myinfo", "img uploaded: " + id);
                        imgDataToUpload.remove(0);
                        GalleryActivity.imgData.get(id).online = true;
                        swipeHint.setAlpha(0.0f);
                        adapter = new ImageAdapter(getContext());
                        gridView.setAdapter(adapter);
                        //upload next image if any in the list
                        if (!imgDataToUpload.isEmpty()) {
                            UploadImage();
                        } else {
                            showProgress(false);
                            uploadBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cloud_done_black_24dp, null));
                            uploadBtn.show();
                        }
                    }
                });
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("debug", "image upload fail");
                    }
                });
        databaseRef.child("date").setValue(GalleryActivity.imgData.get(id).date);
        if(GalleryActivity.imgData.get(id).latLng!=null) {
            String pos = GalleryActivity.imgData.get(id).latLng.getLatitude() + ", " + GalleryActivity.imgData.get(id).latLng.getLongitude();
            databaseRef.child("position").setValue(pos);
        }

    }

    private void GetGoProMediaList() {
        new SendPostRequest().execute("http://10.5.5.9:8080/gp/gpMediaList", "noname");
    }

    private class Wrapper {//use to transmit more information to the post execute of async tasks
        Bitmap imgBmp;
        String text;
        String imgName;
    }

    //Download image from GoPro
    private class DownloadTask extends AsyncTask<String, Void, Wrapper> {
        // Do the task in background/non UI thread
        protected Wrapper doInBackground(String... params) {
            Wrapper wrap = new Wrapper();
            URL url = null;
            try {
                url = new URL("http://10.5.5.9/gp/gpMediaMetadata?p=" + goProDir + "/" + params[0]);
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

                // Return the downloaded bitmap
                wrap.imgBmp = bmp;
                wrap.imgName = params[0];
                return wrap;

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Disconnect the http url connection
                connection.disconnect();
            }
            return null;
        }

        protected void onPostExecute(Wrapper result) {
            if (result != null) {
                // Display the downloaded image into ImageView
                ImgData newImg = new ImgData();
                newImg.imgString = getStringFromBitmap(result.imgBmp);
                newImg.name = result.imgName;
                newImg.online = false;

                if (result.imgName.toLowerCase().contains("jpg")) {
                    GalleryActivity.imgData.add(newImg);
                    //get date and GPS metadata for image
                    new SendPostRequest().execute("http://10.5.5.9/gp/gpMediaMetadata?p=/" + goProDir + "/" + result.imgName + "&t=exif", result.imgName);
                } else {
                    //set download time and position for others since no timestamp available
                    newImg.latLng = lastLatLng;
                    Calendar now = Calendar.getInstance();
                    newImg.date = String.valueOf(now.get(Calendar.YEAR)) + ":" + String.format("%02d", now.get(Calendar.MONTH) + 1) + ":" + String.valueOf(now.get(Calendar.DATE));
                    GalleryActivity.imgData.add(newImg);
                    swipeHint.setAlpha(0.0f);
                    adapter = new ImageAdapter(getContext());
                    gridView.setAdapter(adapter);
                }
                localData.add(newImg.name);
            } else {
                // Notify user that an error occurred while downloading image
                Toast.makeText(getContext(), "Download from GoPro failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //get gopro data list or image metadata
    private class SendPostRequest extends AsyncTask<String, Void, Wrapper> {

        protected Wrapper doInBackground(String... arg0) {
            Wrapper imgAndInfo = new Wrapper();
            imgAndInfo.imgName = arg0[1];
            try {
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
                    imgAndInfo.text = sb.toString();
                    return imgAndInfo;
                } else {
                    return imgAndInfo;
                }
            } catch (Exception e) {
                return imgAndInfo;
            }
        }

        @Override
        protected void onPostExecute(Wrapper result) {

            Log.v("debug", "result: " + result.text);
            //try to use result as data list and recover data names
            try {
                JSONObject obj = new JSONObject(result.text);
                JSONArray array = obj.getJSONArray("media").getJSONObject(0).getJSONArray("fs");
                goProDir = obj.getJSONArray("media").getJSONObject(0).getString("d");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject media = array.getJSONObject(i);
                    Log.v("debug", "dir= " + goProDir + " name= " + media.getString("n"));
                    goProData.add(media.getString("n"));
                }
                //get image if any was found
                DownloadGoProData();

            } catch (Throwable tx) {
                Log.v("debug", "isn't the data list ");
                try {//try to use result as a picture metadata
                    JSONObject obj = new JSONObject(result.text);
                    String date = obj.getJSONObject("exif").getString("DateTimeOriginal");
                    String[] dateSplit = date.split("\\s+"); //only keep the day

                    //add this info to the already downloaded image
                    for (int i = 0; i < GalleryActivity.imgData.size(); i++) {
                        if (GalleryActivity.imgData.get(i).name == result.imgName) {
                            GalleryActivity.imgData.get(i).date = dateSplit[0];
                        }
                    }
                    swipeHint.setAlpha(0.0f);
                    adapter = new ImageAdapter(getContext());
                    gridView.setAdapter(adapter);
                } catch (Throwable tx2) {
                    Log.v("debug", "isn't the metadata of a picture");
                }

                try {//try to recover GPS coordinate if any available
                    JSONObject obj = new JSONObject(result.text);
                    String[] latitude = obj.getJSONObject("exif").getString("GPSLatitude").split(", ");
                    String[] longitude = obj.getJSONObject("exif").getString("GPSLongitude").split(", ");

                    double lat = Double.parseDouble(latitude[0]) + Double.parseDouble(latitude[1]) / 60 + Double.parseDouble(latitude[2]) / (60 * 60);
                    double lng = Double.parseDouble(longitude[0]) + Double.parseDouble(longitude[1]) / 60 + Double.parseDouble(longitude[2]) / (60 * 60);

                    LatLng latLng = new LatLng(lat, lng);

                    for (int i = 0; i < GalleryActivity.imgData.size(); i++) {
                        if (GalleryActivity.imgData.get(i).name == result.imgName) {
                            GalleryActivity.imgData.get(i).latLng = latLng;
                        }
                    }
                    Log.v("debug", "pos: " + latLng.toString());
                } catch (Throwable tx3) {
                    Log.v("debug", "no gps coords: \"" + result.text + "\"");
                }
            }
            mSwipeRefresh.setRefreshing(false);
        }
    }

    private void GetFirebaseMediaList() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseDataListListener = new MyFirebaseDataListener();
        databaseRef.child("users").child(LoginActivity.userID).child("Data").addListenerForSingleValueEvent
                (mFirebaseDataListListener);
        mSwipeRefresh.setRefreshing(false);
    }


    private void DownloadGoProData() {
        List<String> toGet = new ArrayList(goProData);
        toGet.removeAll(localData);
        for (String item : toGet) {
            Log.e("Myinfo", "item: " + item);
            new DownloadTask().execute(item);
        }
        mSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    //class used to create the views of the gridview
    static class ImageHolder {
        ImageView image;
        ImageView logo;
        ImageView latLng;
    }

    //adapter for the gridView
    private class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return GalleryActivity.imgData.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        //return the view of each element of the gridview
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ImageHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
                row = inflater.inflate(R.layout.gallery_image, parent, false);
                holder = new ImageHolder();
                holder.image = row.findViewById(R.id.imageView);
                holder.logo = (ImageView) row.findViewById(R.id.locationView);
                holder.latLng = (ImageView) row.findViewById(R.id.gpsView);
                holder.image.setAdjustViewBounds(true);
                holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.image.setPadding(8, 8, 8, 8);
                row.setTag(holder);
            } else {
                holder = (ImageHolder) row.getTag();
            }

//           assemble the View
            holder.image.setImageBitmap(getBitmapFromString(GalleryActivity.imgData.get(position).imgString));
            if (GalleryActivity.imgData.get(position).latLng != null)
                holder.latLng.setAlpha(1.0f);
            else
                holder.latLng.setAlpha(0.0f);
            if (GalleryActivity.imgData.get(position).online)
                holder.logo.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cloud_queue_black_24dp, null));
            else
                holder.logo.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cloud_off_black_24dp, null));
            return row;
        }
    }

    //    Download firebase data
    private class MyFirebaseDataListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            for (final DataSnapshot rec : dataSnapshot.getChildren()) {
                String imgName = rec.getKey().replace("_", ".");

                if (!localData.contains(imgName)) {
                    Log.e("Myinfo", imgName + " not in list");
                    ImgData newImage = new ImgData();
                    newImage.date = rec.child("date").getValue().toString();
                    String url = rec.child("picture").getValue().toString();
                    if(rec.child("position").getValue()!=null) {
                        String[] latLng = rec.child("position").getValue().toString().split(",");
                        double latitude = Double.parseDouble(latLng[0]);
                        double longitude = Double.parseDouble(latLng[1]);
                        newImage.latLng = new LatLng(latitude, longitude);
                    }
                    newImage.name = imgName;
                    StorageReference mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                    mStorageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(
                            new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    Bitmap newImg = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    newImage.imgString = getStringFromBitmap(newImg);
                                    newImage.online = true;
                                    GalleryActivity.imgData.add(newImage);
                                    localData.add(imgName);
                                    swipeHint.setAlpha(0.0f);
                                    adapter = new ImageAdapter(getContext());
                                    gridView.setAdapter(adapter);
                                    mSwipeRefresh.setRefreshing(false);
                                }
                            });
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.v("err", databaseError.toString());
        }
    }

    private Bitmap getBitmapFromString(String stringPicture) {
        /*
         * This Function converts the String back to Bitmap
         * */
        byte[] decodedString = Base64.decode(stringPicture, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    private String getStringFromBitmap(Bitmap bitmapPicture) {
        /*
         * This functions converts Bitmap picture to a string which can be
         * JSONified.
         * */
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    //presentation of the activity
    private void GuidedTour() {

        new ShowcaseView.Builder(getActivity())
                .setTarget(new ViewTarget(R.id.swipeHint, getActivity()))
                .setContentTitle(R.string.refresh_title)
                .setContentText(R.string.refresh_text)
                .setStyle(R.style.CustomShowcaseTheme)
                .setShowcaseEventListener(new SimpleShowcaseEventListener() {

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        new ShowcaseView.Builder(getActivity())
                                .setTarget(new ViewTarget(R.id.galleryThumbnails, getActivity()))
                                .setContentTitle(R.string.delete_title)
                                .setContentText(R.string.delete_text)
                                .setStyle(R.style.CustomShowcaseTheme)
                                .setShowcaseEventListener(new SimpleShowcaseEventListener() {

                                    @Override
                                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                                        new ShowcaseView.Builder(getActivity())
                                                .setTarget(new ViewTarget(R.id.uploadBtn, getActivity()))
                                                .setContentTitle(R.string.upload_title)
                                                .setContentText(R.string.upload_text)
                                                .setStyle(R.style.CustomShowcaseTheme)
                                                .build();
                                    }
                                })
                                .build();
                    }
                })
                .build();
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("galleryFirst", false);
        editor.apply();
    }
}
