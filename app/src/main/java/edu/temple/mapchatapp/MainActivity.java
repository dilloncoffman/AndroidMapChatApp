package edu.temple.mapchatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.security.KeyPair;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements UserRecyclerViewFragment.OnUserSelectedInterface {
    ArrayList<User> mUsers;

    KeyPair myKeyPair;
    boolean mDoublePane;

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
            // The map container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mDoublePane = true;
        }

        // TODO Fetch users using Volley and pass them to UserRecyclerViewFragment after attaching that fragment to it's container

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
}
