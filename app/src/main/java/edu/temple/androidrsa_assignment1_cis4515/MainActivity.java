package edu.temple.androidrsa_assignment1_cis4515;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.security.KeyPair;

public class MainActivity extends AppCompatActivity {

    TextView publicKeyText;
    TextView privateKeyText;
    Button getKeyPairButton;
    KeyPair myKeyPair;

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

        publicKeyText = findViewById(R.id.publicKey);
        privateKeyText = findViewById(R.id.privateKey);
        getKeyPairButton = findViewById(R.id.getKeyPairButton);

        // Bind to KeyService
        keyIntent = new Intent(this, KeyService.class);
        Log.d("KeyService", "Bound to service connection");
        bindService(keyIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        getKeyPairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(keyIntent);
                myKeyPair = keyBinder.getMyKeyPair();
                publicKeyText.setText(String.valueOf(myKeyPair.getPublic()));
                privateKeyText.setText(String.valueOf(myKeyPair.getPrivate()));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unbind from KeyService
        Log.d("KeyService", "Unbound from service connection");
        unbindService(serviceConnection);
    }
}
