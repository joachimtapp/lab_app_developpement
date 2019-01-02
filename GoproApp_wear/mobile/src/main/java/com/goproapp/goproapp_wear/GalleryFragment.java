package com.goproapp.goproapp_wear;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.mapbox.mapboxsdk.annotations.BaseMarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GalleryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private View fragmentView;
    private ImageAdapter adapter;
    private GridView gridView;
    private TextView dateView;
    private TextView bpmView;
    private MapView mapView;
    private Marker marker;
    public static MapboxMap mMapboxMap;
    private int nPrevSelGridItem = -1; //used highlight selected picture
    private View viewPrev;

    public GalleryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GalleryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GalleryFragment newInstance() {
        GalleryFragment fragment = new GalleryFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.fragment_gallery, container, false);
        gridView = fragmentView.findViewById(R.id.galleryThumbnails);
        dateView = fragmentView.findViewById(R.id.galleryDateValue);
        bpmView = fragmentView.findViewById(R.id.bpmValue);
        mapView = (MapView) fragmentView.findViewById(R.id.gallery_map);
        adapter = new ImageAdapter(getContext());
        gridView.setAdapter(adapter);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mMapboxMap = mapboxMap;

                CameraPosition camPos = new CameraPosition.Builder()
                        .target(GalleryActivity.imgData.get(0).latLng)// Sets the new camera position
                        .zoom(0) // Sets the zoom
                        .bearing(0) // Rotate the camera
                        .tilt(0) // Set the camera tilt
                        .build(); // Creates a CameraPosition from the builder
                mMapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(camPos), 2000);
            }
        });


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                try {
                    if (nPrevSelGridItem != -1) {
                        viewPrev = (View) gridView.getChildAt(nPrevSelGridItem);
                        viewPrev.setBackgroundColor(Color.WHITE);
                    }
                    nPrevSelGridItem = position;
                    if (nPrevSelGridItem == position) {
                        //View viewPrev = (View) gridview.getChildAt(nPrevSelGridItem);
                        v.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dateView.setText(GalleryActivity.imgData.get(position).date);
                bpmView.setText(GalleryActivity.imgData.get(position).bpm);

                //move marker
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

                    Toast.makeText(getContext(), "first", Toast.LENGTH_SHORT).show();
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

    private class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {

            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
//                imageView.setLayoutParams(new ViewGroup.LayoutParams(85, 85));
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mThumbIds[position]);
            return imageView;
        }

        // references to our images
        private Integer[] mThumbIds = {
                R.drawable.sample_2, R.drawable.sample_3,
                R.drawable.sample_4,
//                R.drawable.sample_5,
//                R.drawable.sample_6, R.drawable.sample_7,
//                R.drawable.sample_0, R.drawable.sample_1,
//                R.drawable.sample_2, R.drawable.sample_3,
//                R.drawable.sample_4, R.drawable.sample_5,
//                R.drawable.sample_6, R.drawable.sample_7,
//                R.drawable.sample_0, R.drawable.sample_1,
//                R.drawable.sample_2, R.drawable.sample_3,
//                R.drawable.sample_4, R.drawable.sample_5,
//                R.drawable.sample_6, R.drawable.sample_7
        };

    }


}
