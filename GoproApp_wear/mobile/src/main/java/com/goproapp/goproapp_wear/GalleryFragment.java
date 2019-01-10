package com.goproapp.goproapp_wear;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.JsonArray;
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
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.http.Url;

public class GalleryFragment extends Fragment {

    //declaration of view elements
    private OnFragmentInteractionListener mListener;
    private View fragmentView;
    private ImageAdapter adapter;
    private GridView gridView;
    private TextView dateView;
    private TextView swipeHint;
    private TextView bpmView;
    private LinearLayout linearLayoutInfo;
    private MapView mapView;
    private Marker marker;
    private SwipeRefreshLayout mSwipeRefresh;
    private List<String> localData;
    private List<String> goProData = new ArrayList<>();
    private String goProDir;
    public static MapboxMap mMapboxMap;
    private int nPrevSelGridItem = -1; //used highlight selected picture
    private View viewPrev;

    private ImageView feckinView;

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
    public void onDestroy() {
        super.onDestroy();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_gallery, container, false);
        gridView = fragmentView.findViewById(R.id.galleryThumbnails);
        dateView = fragmentView.findViewById(R.id.galleryDateValue);
        bpmView = fragmentView.findViewById(R.id.bpmValue);
        swipeHint = fragmentView.findViewById(R.id.swipeHint);
        mapView = (MapView) fragmentView.findViewById(R.id.gallery_map);
        linearLayoutInfo = fragmentView.findViewById(R.id.linearLayoutInfo);
        feckinView = fragmentView.findViewById(R.id.feckinView);

        if (GalleryActivity.imgData.size() > 0) {//consider the case of already downloaded images
            swipeHint.setAlpha(0.0f);
            adapter = new ImageAdapter(getContext());
            gridView.setAdapter(adapter);
        }
        mSwipeRefresh = fragmentView.findViewById(R.id.swiperefresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

//                GetGoProMediaList();
                DownloadGoProData();
                mSwipeRefresh.setRefreshing(false);
//                if (GalleryActivity.imgData.size() > 0) {//check if you have images or not
//                    swipeHint.setAlpha(0.0f);
//                    adapter = new ImageAdapter(getContext());
//                    gridView.setAdapter(adapter);
//                } else {
//                    swipeHint.setText(getString(R.string.galleryEmpty));
//                    mSwipeRefresh.setRefreshing(false);
//                }
            }
        });

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mMapboxMap = mapboxMap;
                if (GalleryActivity.imgData.size() > 0) {
                    //set initial map position
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(new LatLng(48.769183, 21.661252))// Sets the new camera position
                            .zoom(0) // Sets the zoom
                            .bearing(0) // Rotate the camera
                            .tilt(0) // Set the camera tilt
                            .build(); // Creates a CameraPosition from the builder
                    mMapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(camPos), 2000);
                }

            }
        });

        //handle image click
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                //highlight selected picture
                try {
                    if (nPrevSelGridItem != -1) {
                        viewPrev = (View) gridView.getChildAt(nPrevSelGridItem);
                        viewPrev.setBackgroundColor(Color.WHITE);
                    }
                    nPrevSelGridItem = position;
                    if (nPrevSelGridItem == position) {
                        v.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dateView.setText(GalleryActivity.imgData.get(position).date);
                bpmView.setText(GalleryActivity.imgData.get(position).bpm);
                linearLayoutInfo.setVisibility(viewPrev.VISIBLE);
                //move marker and create it if necessary
                if (marker == null) {
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(GalleryActivity.imgData.get(position).latLng)// Sets the new camera position
                            .zoom(10) // Sets the zoom
                            .bearing(0) // Rotate the camera
                            .tilt(0) // Set the camera tilt
                            .build(); // Creates a CameraPosition from the builder
                    mMapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(camPos), 2000);
                    marker = mMapboxMap.addMarker(new MarkerOptions().position(GalleryActivity.imgData.get(position).latLng));

                } else {
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
        });
        return fragmentView;
    }

    private void GetGoProMediaList() {
//        sendRequest("http://10.5.5.9:8080/gp/gpMediaList");
        new SendPostRequest().execute("http://10.5.5.9:8080/gp/gpMediaList");
    }


    private class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

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
                    Log.v("debug", "ici " + sb.toString());
                    return sb.toString();

                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {

            Log.v("debug", result);
            byte[] b = result.getBytes();
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            feckinView.setImageBitmap(bitmap);
            Toast.makeText(getContext(), "maybe??????", Toast.LENGTH_SHORT).show();

            try {
                JSONObject obj = new JSONObject(result);
                JSONArray array =obj.getJSONArray("media").getJSONObject(0).getJSONArray("fs");
                goProDir=obj.getJSONArray("media").getJSONObject(0).getString("d");
                for(int i=0;i<array.length();i++) {

                    JSONObject media =array.getJSONObject(i);
                    Log.v("debug","dir= "+goProDir+" name= "+media.getString("n"));
                    goProData.add(media.getString("n"));
                }

            } catch (Throwable tx) {
                Log.v("debug", "Could not parse malformed JSON: \"" + result + "\"");
            }


        }
    }

    private class SendHttpRequestTask extends AsyncTask<String, Void, byte[]> {

        @Override
        protected byte[] doInBackground(String... params) {
            String url = params[0];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {

                HttpURLConnection con = (HttpURLConnection) ( new URL(url)).openConnection();
//                con.setRequestMethod("POST");
//                con.setDoInput(true);
//                con.setDoOutput(true);
//                con.connect();

                con.setReadTimeout(15000 /* milliseconds */);
                con.setConnectTimeout(15000 /* milliseconds */);
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);


                int responseCode = con.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {


                    BufferedInputStream bin =new BufferedInputStream(con.getInputStream());
                    byte[] buf = new byte[1024];

//                    while ( is.read(b) != -1)
//                        baos.write(b);
                    int bytesRead=0;
                    while ((bin.read(buf))!=-1){
                        baos.write(buf);
                    }
                    con.disconnect();


                    return baos.toByteArray();

                } else {
                    Toast.makeText(getContext(), "fail", Toast.LENGTH_SHORT).show();
                    return new byte[0];
                }
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
            Toast.makeText(getContext(), "fail2", Toast.LENGTH_SHORT).show();
            return new byte[0];
        }

        @Override
        protected void onPostExecute(byte[] result) {
            Bitmap img = BitmapFactory.decodeByteArray(result, 0, result.length);
            feckinView.setImageBitmap(img);
        }
    }
    public byte[] downloadImage(String imgName,String url) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.out.println("URL ["+url+"] - Name ["+imgName+"]");

            HttpURLConnection con = (HttpURLConnection) ( new URL(url)).openConnection();
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();
            con.getOutputStream();

            InputStream is = con.getInputStream();
            byte[] b = new byte[1024];

            while ( is.read(b) != -1)
                baos.write(b);

            con.disconnect();
        }
        catch(Throwable t) {
            t.printStackTrace();
        }

        return baos.toByteArray();
    }


    private void DownloadGoProData() {
        Toast.makeText(getContext(), "maybe??", Toast.LENGTH_SHORT).show();

//        new SendPostRequest().execute(" http://10.5.5.9/gp/gpMediaMetadata?p=100GOPRO/GOPR0050.JPG");
//        Drawable img=LoadImageFromWebOperations(" http://10.5.5.9/gp/gpMediaMetadata?p=100GOPRO/GOPR0050.JPG");
//        feckinView.setImageDrawable(getResources().getDrawable(R.drawable.sample_0));URL url = new URL("urlPath");

    new SendHttpRequestTask().execute(" http://10.5.5.9/gp/gpMediaMetadata?p=100GOPRO/GOPR0050.JPG");

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

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

        //Download the images and create a new ImageView for each
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            StorageReference mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(GalleryActivity.imgData.get(position).imgUrl);
            mStorageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(
                    new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap newImg = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            imageView.setImageBitmap(newImg);
                            GalleryActivity.imgData.get(position).img = newImg;
                            mSwipeRefresh.setRefreshing(false);
                        }
                    });
            return imageView;
        }
    }
}
