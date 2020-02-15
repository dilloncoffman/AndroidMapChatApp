package edu.temple.mapchatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements UserRecyclerViewFragment.OnUserSelectedInterface, MapFragment.OnFragmentInteractionListener, VolleyCallback {
    ArrayList<User> mUsers = new ArrayList<>();
    User currentUser = new User("notARealUser", 10.0, 10.0);
    KeyPair myKeyPair;
    boolean mDoublePane;
    Fragment containerUserRecyclerViewFragment;
    Fragment containerMapFragment;
    EditText userInput;
    Button submitBtn;

    // Location
    LocationManager locationManager;
    LocationListener locationListener;
    // Volley
    private RequestQueue mQueue;
    // Handler to fetch users every 30 seconds
    Handler handler;
    int fetchInterval = 30000; // in milliseconds
    // Debug tag
    private static final String TAG = "MainActivity";

    // KeyService
    boolean connected;
    Intent keyIntent;
    KeyService.KeyBinder keyBinder;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connected = true;
            Log.d("KeyServiceConnection: ", "Connected");
            keyBinder = (KeyService.KeyBinder) service; // hold reference to KeyBinder that KeyService is returning that describes interactions you can perform
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("KeyServiceConnection: ", "Disconnected: Service was killed for some reason");
            connected = false; // no longer connected
            keyBinder = null; // to protect against memory leak
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Bind to KeyService
        keyIntent = new Intent(this, KeyService.class);
        Log.d("KeyService", "Bound to service connection");
        bindService(keyIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        if (keyBinder != null) {
            myKeyPair = keyBinder.getMyKeyPair(); // hold ref to user's keyPair
        }

        userInput = findViewById(R.id.userNameInput);
        submitBtn = findViewById(R.id.submitBtn);

        if (findViewById(R.id.fragment_map_container) != null) {
            mDoublePane = true;
        }

        // Get currentUser's location using device permission
        locationManager = getSystemService(LocationManager.class);

        locationListener = new LocationListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged: " + currentUser.toString());
                // Get currentUserLat & currentUserLng
                currentUser.setLatitude(location.getLatitude());
                currentUser.setLongitude(location.getLongitude());

                // Will POST every 10 meters currentUser has moved or as soon as user allows location permission for the first time
                Location locationA = new Location("Location A"); // User's last known location
                locationA.setLatitude(Objects.requireNonNull(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).getLatitude());
                locationA.setLongitude(Objects.requireNonNull(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).getLongitude());
                Location locationB = new Location("Location B"); // User's current location
                locationB.setLatitude(currentUser.getLatitude());
                locationB.setLongitude(currentUser.getLongitude());
                if (locationA.distanceTo(locationB) > 10 && !(currentUser.getName().equals("notARealUser") || currentUser.getName().equals(""))) { // distanceTo returns distance in meters
                    try {
                         postUser(currentUser);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // TODO Update MapFragment with user's new location
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        } else {
            showLocationUpdates();
        }

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                currentUser = new User(userInput.getText().toString(), Objects.requireNonNull(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).getLatitude(), Objects.requireNonNull(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).getLongitude());

                // Set user's name with what was in text input
                Log.d(TAG, "onClick: " + currentUser.toString());

                // POST to endpoint using Volley and currentUser
                if (!(currentUser.getName().equals("notARealUser") || currentUser.getName().equals(""))) { // distanceTo returns distance in meters
                    try {
                        postUser(currentUser);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        fetchUsers(new VolleyCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject user = response.getJSONObject(i);
                        // Create User using JSONArray
                        User newUser = new User(
                                user.getString("username"),
                                Double.parseDouble(user.getString("latitude")),
                                Double.parseDouble(user.getString("longitude"))
                        );
                        Log.d(TAG, "onResponse: " + newUser.toString());
                        // Add newUser to ArrayList<User> mUsers
                        mUsers.add(newUser);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // Get reference to fragment containers
                containerUserRecyclerViewFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_user_list_container);
                containerMapFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_map_container);
                // The map container view will be present only in the large-screen layouts (res/values-w900dp).
                // If fragment_map_container is present, then the activity should be in two-pane mode.
                mDoublePane = (findViewById(R.id.fragment_map_container) != null);

                // TODO sort users by distance from the currentUser

                // TODO list of users should be updated every 30 seconds - fetchUsers(users)

                if (containerUserRecyclerViewFragment == null && !mDoublePane) {
                    // App opened in portrait mode
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.fragment_user_list_container, UserRecyclerViewFragment.newInstance(mUsers, mDoublePane))
                            .commitAllowingStateLoss();
                } else if (containerUserRecyclerViewFragment == null) {
                    // App opened in landscape mode
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_user_list_container, UserRecyclerViewFragment.newInstance(mUsers, mDoublePane))
                            .commitAllowingStateLoss();
                }

                // Handle portrait to landscape and vice versa orientation changes
                // from landscape to portrait
                if (containerUserRecyclerViewFragment instanceof UserRecyclerViewFragment && !mDoublePane) {
                    if (((UserRecyclerViewFragment) containerUserRecyclerViewFragment).getUsers() != null) {
                        mUsers = ((UserRecyclerViewFragment) containerUserRecyclerViewFragment).getUsers();
                        mDoublePane = (findViewById(R.id.fragment_map_container) != null);
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_user_list_container, UserRecyclerViewFragment.newInstance(mUsers, mDoublePane))
                                .commit();
                    }
                } else if (containerUserRecyclerViewFragment instanceof UserRecyclerViewFragment && mDoublePane) {
                    Log.d("Went from portrait to landscape. Double pane should be true == ", String.valueOf(mDoublePane));
                    if (containerUserRecyclerViewFragment != null && ((UserRecyclerViewFragment) containerUserRecyclerViewFragment).getUsers() != null) {
                        mUsers = ((UserRecyclerViewFragment) containerUserRecyclerViewFragment).getUsers();
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_user_list_container, UserRecyclerViewFragment.newInstance(mUsers, mDoublePane))
                                .commit();
                    }
                }
            }
        });

        // Fetch list of users every 30 seconds
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mUsers.size() > 0) {
                    mUsers.clear();
                }
                Log.d(TAG, "run: Fetched new list of users");
                fetchUsers(new VolleyCallback() {
                    @Override
                    public void onSuccess(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject user = response.getJSONObject(i);
                                // Create User using JSONArray
                                User newUser = new User(
                                        user.getString("username"),
                                        Double.parseDouble(user.getString("latitude")),
                                        Double.parseDouble(user.getString("longitude"))
                                );
                                Log.d(TAG, "onResponse: " + newUser.toString());
                                // Add newUser to ArrayList<User> mUsers
                                mUsers.add(newUser);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        // Get reference to fragment containers
                        containerUserRecyclerViewFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_user_list_container);
                        containerMapFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_map_container);
                        // The map container view will be present only in the large-screen layouts (res/values-w900dp).
                        // If fragment_map_container is present, then the activity should be in two-pane mode.
                        mDoublePane = (findViewById(R.id.fragment_map_container) != null);

                        // TODO sort users by distance from the currentUser

                        // TODO list of users should be updated every 30 seconds - fetchUsers(users)

                        if (containerUserRecyclerViewFragment == null && !mDoublePane) {
                            // App opened in portrait mode
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(R.id.fragment_user_list_container, UserRecyclerViewFragment.newInstance(mUsers, mDoublePane))
                                    .commitAllowingStateLoss();
                        } else if (containerUserRecyclerViewFragment == null) {
                            // App opened in landscape mode
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_user_list_container, UserRecyclerViewFragment.newInstance(mUsers, mDoublePane))
                                    .commitAllowingStateLoss();
                        }

                        // Handle portrait to landscape and vice versa orientation changes
                        // from landscape to portrait
                        if (containerUserRecyclerViewFragment instanceof UserRecyclerViewFragment && !mDoublePane) {
                            if (((UserRecyclerViewFragment) containerUserRecyclerViewFragment).getUsers() != null) {
                                mUsers = ((UserRecyclerViewFragment) containerUserRecyclerViewFragment).getUsers();
                                mDoublePane = (findViewById(R.id.fragment_map_container) != null);
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_user_list_container, UserRecyclerViewFragment.newInstance(mUsers, mDoublePane))
                                        .commit();
                            }
                        } else if (containerUserRecyclerViewFragment instanceof UserRecyclerViewFragment && mDoublePane) {
                            Log.d("Went from portrait to landscape. Double pane should be true == ", String.valueOf(mDoublePane));
                            if (containerUserRecyclerViewFragment != null && ((UserRecyclerViewFragment) containerUserRecyclerViewFragment).getUsers() != null) {
                                mUsers = ((UserRecyclerViewFragment) containerUserRecyclerViewFragment).getUsers();
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_user_list_container, UserRecyclerViewFragment.newInstance(mUsers, mDoublePane))
                                        .commit();
                            }
                        }
                    }
                });
                // TODO update MapView with all user's new locations every 30 seconds - just create a new MapFragment.getInstance(mUsers)
                handler.postDelayed(this, fetchInterval);
            }
        }, fetchInterval);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unbind from KeyService
        Log.d("KeyService", "Unbound from service connection");
        unbindService(serviceConnection);
    }

    @Override
    public void userSelected(int position) {
        User user = mUsers.get(position);

        // TODO if doublePane
        /*
            Add user to bundle to be passed to MapFragment???
            What should MapFragment have in it or really be doing?
         */
        /*
            else
                Launch UserDetailActivity that then has a MapFragment
         */
    }

    public void postUser(final User user) throws JSONException {
        if (user != null) {
            mQueue = Volley.newRequestQueue(this);
            String url = "https://kamorris.com/lab/register_location.php";
            Log.d(TAG, "postUser: " + user.toString());

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "onResponse: " + response);
                            Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "onErrorResponse: " + error.toString());
                            Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    // Data to make POST request with
                    Map<String, String> params = new HashMap<>();
                    params.put("user", user.getName());
                    params.put("latitude", String.valueOf(user.getLatitude()));
                    params.put("longitude", String.valueOf(user.getLongitude()));
                    return params;
                }
            };

            mQueue.add(stringRequest);
        } else {
            Log.d(TAG, "postUser: " + "No user exists to POST with.");
        }
    }

    public void fetchUsers(final VolleyCallback callback) {
        mQueue = Volley.newRequestQueue(this);

        String usersUrl = "https://kamorris.com/lab/get_locations.php";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, usersUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    // Location methods and necessary lifecycle callbacks
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showLocationUpdates();
        }
    }

    @SuppressLint("MissingPermission") // Already checking necessary permission before calling
    private void showLocationUpdates() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, this.locationListener); // GPS
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, this.locationListener); // Cell sites
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 10, this.locationListener); // WiFi
    }

    @Override
    public void onSuccess(JSONArray response) {
    }
}
