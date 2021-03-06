package com.goproapp.goproapp_wear;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Projection;

import java.util.ArrayList;
import java.util.List;

public class GalleryMapFragment extends Fragment {

    private MapView mapView;
    private ImageView galleryMapImg;

    private MapView.OnCameraDidChangeListener mCameraListener;
    private FloatingActionButton refreshBtn;
    private MapboxMap mMapboxMap;

    private List<MarkerViewOptions> markerViews = new ArrayList<MarkerViewOptions>();

    public GalleryMapFragment() {
        // Required empty public constructor
    }

    public static GalleryMapFragment newInstance() {
        GalleryMapFragment fragment = new GalleryMapFragment();
        return fragment;
    }

    //overrides required by MapBox
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getContext(), getString(R.string.accessToken));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_gallery_map, container, false);
        mapView = fragmentView.findViewById(R.id.galleryMapView);
        galleryMapImg = fragmentView.findViewById(R.id.galleryMapImg);

        refreshBtn = fragmentView.findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RefreshMarker();
            }
        });
        //listener required to remove picture preview on move
        mCameraListener = new MapView.OnCameraDidChangeListener() {
            @Override
            public void onCameraDidChange(boolean animated) {
                galleryMapImg.setAlpha(0.0f);
                mapView.removeOnCameraDidChangeListener(mCameraListener);
            }
        };
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mMapboxMap=mapboxMap;
                mapboxMap.getUiSettings().setAttributionEnabled(false);
                mapboxMap.getUiSettings().setLogoEnabled(false);
                int cnt = 0;//Keep track of the marker id
                //add the picture position to the map
                for (ImgData im : GalleryActivity.imgData) {
                    if(im.latLng!=null) {
                        MarkerViewOptions opt = new MarkerViewOptions().position(im.latLng).title(String.valueOf(cnt));
                        markerViews.add(opt);
                    }
                    cnt++;
                }
                mapboxMap.addMarkerViews(markerViews);

                //set initial map position
                if(GalleryActivity.imgData.size()>0) {
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(new LatLng(46.526732, 6.611953))// Sets the new camera position
                            .zoom(10) // Sets the zoom
                            .bearing(0) // Rotate the camera
                            .tilt(0) // Set the camera tilt
                            .build(); // Creates a CameraPosition from the builder
                    mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(camPos), 2000);
                }

                mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {
                        galleryMapImg.setAlpha(0.0f);
                    }
                });
                mapboxMap.getMarkerViewManager().setOnMarkerViewClickListener(new MapboxMap.OnMarkerViewClickListener() {
                    //show thumbnail on map if already downloaded
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker, @NonNull View view, @NonNull MapboxMap.MarkerViewAdapter adapter) {
                        if (GalleryActivity.imgData.get(Integer.parseInt(marker.getTitle())).imgString != null) {
                            Bitmap img = getBitmapFromString(GalleryActivity.imgData.get(Integer.parseInt(marker.getTitle())).imgString);

                            Bitmap resizedBitmap = Bitmap.createScaledBitmap(img, 320, 240, false);
                            galleryMapImg.setImageBitmap(resizedBitmap);
                            Projection mapProjection = mapboxMap.getProjection();
                            PointF screenPosition = mapProjection.toScreenLocation(marker.getPosition());
                            galleryMapImg.setX(screenPosition.x);
                            galleryMapImg.setY(screenPosition.y);
                            galleryMapImg.setAlpha(1.0f);
                            mapView.addOnCameraDidChangeListener(mCameraListener);
                        }
                        return true;
                    }
                });
            }
        });
        return fragmentView;
    }

    private void RefreshMarker() {
        int cnt = 0;//Keep track of the marker id
        markerViews.clear();
        for (ImgData im : GalleryActivity.imgData) {
            if(im.latLng!=null) {
                MarkerViewOptions opt = new MarkerViewOptions().position(im.latLng).title(String.valueOf(cnt));
                markerViews.add(opt);
            }
                cnt++;
        }
        if(mMapboxMap!=null) {
            mMapboxMap.addMarkerViews(markerViews);

            if(markerViews.size()>0) {
                //set initial map position
                CameraPosition camPos = new CameraPosition.Builder()
                        .target(markerViews.get(0).getPosition())// Sets the new camera position
                        .zoom(10) // Sets the zoom
                        .bearing(0) // Rotate the camera
                        .tilt(0) // Set the camera tilt
                        .build(); // Creates a CameraPosition from the builder
                mMapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(camPos), 2000);
            }
        }
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


    private Bitmap getBitmapFromString(String stringPicture) {
        /*
         * This Function converts the String back to Bitmap
         * */
        byte[] decodedString = Base64.decode(stringPicture, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }
}
