package edu.temple.mapchatapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    static User mCurrentUser;
    private ArrayList<User> mUsers;
    private final static String CURRENT_USER_KEY = "currentUser";
    private final static String USERS_KEY = "users";
    private final static String DOUBLE_PANE_KEY = "doublePane";
    MapView mMapView;
    // 3. Every time you drop a marker, hold on to it
    Marker marker;
    // 4. Map of Markers to set on the map from some data source loading in coordinates
    Map<String, Marker> myMarkers = new HashMap<String, Marker>();


    private static final String TAG = "UserRecyclerViewFragment";
    private boolean mDoublePane;
    /**
     * The fragment argument representing the user name that this fragment
     * represents.
     */
    public static final String ARG_USER = "mUserClicked";

    private static User mUserClicked;

    private OnFragmentInteractionListener mListener;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param currentUser User.
     * @param users       ArrayList<User>.
     * @param doublePane  boolean.
     * @return A new instance of fragment MapFragment.
     */
    public static MapFragment newInstance(Parcelable currentUser, ArrayList<User> users, boolean doublePane) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putParcelable(CURRENT_USER_KEY, currentUser);
        args.putParcelableArrayList(USERS_KEY, users);
        args.putBoolean(DOUBLE_PANE_KEY, doublePane);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get data from bundle on newInstance
        Bundle args = getArguments();
        if (args != null) {
            mCurrentUser = args.getParcelable(CURRENT_USER_KEY);
            mUsers = args.getParcelableArrayList(USERS_KEY);
            mDoublePane = args.getBoolean(DOUBLE_PANE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Set up map
        if (view.findViewById(R.id.mapView) != null) {
            mMapView = view.findViewById(R.id.mapView);
            mMapView.onCreate(savedInstanceState);
            mMapView.getMapAsync(this);
        }

        return view;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (mCurrentUser != null) {
            Log.d(TAG, "onMapReady: Current user is: " + mCurrentUser.toString());
            LatLng latLng = new LatLng(mCurrentUser.getLatitude(), mCurrentUser.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            if (googleMap != null) {
                googleMap.animateCamera(cameraUpdate);
                // Put user marker down - addMarker returns a Marker you can hold on to
                if (marker == null) {
                    marker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(mCurrentUser.getName() + "'s Current Location")
                    );
                    myMarkers.put(latLng.toString(), marker);
                } else {
                    marker.setPosition(latLng);
                }
            } else {
                Log.d(TAG, "onMapReady: mGoogleMap was null");
            }
        } else {
            Log.d(TAG, "onMapReady: currentUser was null");
        }

        if (mUsers != null) {
            Log.d(TAG, "onMapReady: mUsers are not null");
            if (googleMap != null) {
                // TODO make sure to skip over mCurrentUser in list
                for (int i = 0; i < mUsers.size(); i++) {
                    LatLng latLng = new LatLng(mUsers.get(i).getLatitude(), mUsers.get(i).getLongitude());
                    marker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(mUsers.get(i).getName() + "'s Current Location") // all setter methods don't have 'set' in their names with Google Maps Android SDK
                    );
                    myMarkers.put(latLng.toString(), marker);

                }
            }
        }
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

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
