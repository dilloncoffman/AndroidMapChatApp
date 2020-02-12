package edu.temple.mapchatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

/*
    The detail Activity launched by MainActivity when a recycler
    view item is selected in portrait mode

    This Activity should attach a MapFragment to display the
    user's current location and others

    The MapFragment should update its data every 30 seconds
    and update the currentUser's location after every 10 meters
    travelled.
 */
public class UserDetailActivity extends AppCompatActivity implements MapFragment.OnFragmentInteractionListener {

    private static final String TAG = "UserDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(MapFragment.ARG_USER,
                    getIntent().getParcelableExtra(MapFragment.ARG_USER));
            MapFragment mapFragment = new MapFragment();
            mapFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_map_container, mapFragment)
                    .commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
