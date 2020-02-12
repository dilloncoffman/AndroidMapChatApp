package edu.temple.mapchatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyPair;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements UserRecyclerViewFragment.OnUserSelectedInterface {
    ArrayList<User> mUsers = new ArrayList<>();
    KeyPair myKeyPair;
    boolean mDoublePane;
    Fragment containerUserRecyclerViewFragment;
    Fragment containerMapFragment;

    // Volley
    private RequestQueue mQueue;
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

        if (findViewById(R.id.fragment_map_container) != null) {

            mDoublePane = true;
        }

        // TODO Fetch users using Volley and pass them to UserRecyclerViewFragment after attaching that fragment to it's container
        fetchUsers();
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

    public void fetchUsers() {
        mQueue = Volley.newRequestQueue(this);

        String usersUrl = "https://kamorris.com/lab/get_locations.php";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, usersUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
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
                        Log.d(TAG, "onCreate: mDoublePane is " + mDoublePane);

                        if (containerUserRecyclerViewFragment == null && !mDoublePane) {
                            // App opened in portrait mode
                            Log.d("App opened in portrait mode. Double pane should be false == ", String.valueOf(mDoublePane));
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(R.id.fragment_user_list_container, UserRecyclerViewFragment.newInstance(mUsers))
                                    .commit();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }
}
