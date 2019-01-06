package com.goproapp.goproapp_wear;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Projection;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GalleryMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GalleryMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryMapFragment extends Fragment {

    private MapView mapView;
    private ImageView galleryMapImg;

    private OnFragmentInteractionListener mListener;
    private MapView.OnCameraDidChangeListener mCameraListener;

    private List<MarkerViewOptions> markerViews = new ArrayList<MarkerViewOptions>();

    public GalleryMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GalleryMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GalleryMapFragment newInstance() {
        GalleryMapFragment fragment = new GalleryMapFragment();

        return fragment;
    }
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
    public void onSaveInstanceState (final Bundle outState){
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
        mapView = (MapView) fragmentView.findViewById(R.id.galleryMapView);
        galleryMapImg = fragmentView.findViewById(R.id.galleryMapImg);

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
                int cnt = 0;
                for (ImgData im : GalleryActivity.imgData) {
                    MarkerViewOptions opt = new MarkerViewOptions().position(im.latLng).title(String.valueOf(cnt));
                    markerViews.add(opt);
                    cnt++;
                }
                mapboxMap.addMarkerViews(markerViews);

                CameraPosition camPos = new CameraPosition.Builder()
                        .target(GalleryActivity.imgData.get(0).latLng)// Sets the new camera position
                        .zoom(0) // Sets the zoom
                        .bearing(0) // Rotate the camera
                        .tilt(0) // Set the camera tilt
                        .build(); // Creates a CameraPosition from the builder
                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(camPos), 2000);
                mapboxMap.getMarkerViewManager().setOnMarkerViewClickListener(new MapboxMap.OnMarkerViewClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker, @NonNull View view, @NonNull MapboxMap.MarkerViewAdapter adapter) {
                        Bitmap img = GalleryActivity.imgData.get(Integer.parseInt(marker.getTitle())).img;
                        Bitmap img_resize = resizeImage(img, 200);
                        galleryMapImg.setImageBitmap(img_resize);
                        Projection mapProjection = mapboxMap.getProjection();
                        PointF screenPosition = mapProjection.toScreenLocation(marker.getPosition());
                        galleryMapImg.setX(screenPosition.x);
                        galleryMapImg.setY(screenPosition.y);
                        galleryMapImg.setAlpha(1.0f);
                        mapView.addOnCameraDidChangeListener(mCameraListener);
                        return true;
                    }
                });

            }
        });
        // Inflate the layout for this fragment
        return fragmentView;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private Bitmap resizeImage(Bitmap bitmap, int newSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Image smaller, return it as is!
        if (width <= newSize && height <= newSize) return bitmap;

        int newWidth;
        int newHeight;

        if (width > height) {
            newWidth = newSize;
            newHeight = (newSize * height) / width;
        } else if (width < height) {
            newHeight = newSize;
            newWidth = (newSize * width) / height;
        } else {
            newHeight = newSize;
            newWidth = newSize;
        }

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(bitmap, 0, 0,
                width, height, matrix, true);
    }
}
