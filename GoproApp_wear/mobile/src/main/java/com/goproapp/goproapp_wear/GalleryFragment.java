package com.goproapp.goproapp_wear;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

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

    public static MapboxMap mMapboxMap;
    private int nPrevSelGridItem = -1; //used highlight selected picture
    private View viewPrev;

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

        if (GalleryActivity.imgData.size() > 0) {//consider the case of already downloaded images
            swipeHint.setAlpha(0.0f);
            adapter = new ImageAdapter(getContext());
            gridView.setAdapter(adapter);
        }
        mSwipeRefresh = fragmentView.findViewById(R.id.swiperefresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (GalleryActivity.imgData.size() > 0) {//check if you have images or not
                    swipeHint.setAlpha(0.0f);
                    adapter = new ImageAdapter(getContext());
                    gridView.setAdapter(adapter);
                } else {
                    swipeHint.setText(getString(R.string.galleryEmpty));
                    mSwipeRefresh.setRefreshing(false);
                }
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
