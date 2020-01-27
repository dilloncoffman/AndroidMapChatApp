package edu.temple.androidrsa_assignment1_cis4515;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.security.Key;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

public class KeyService extends Service {
    private final KeyService.KeyBinder binder = new KeyService.KeyBinder();

    public KeyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.i("KeyService", "KeyService bound");
        return this.binder;
    }

    /*
        Generate and/or retrieve a user’s RSA KeyPair. The first call to this
        method will generate and store the keypair before returning it. Subsequent calls will return the
        same key pair
     */
    public KeyPair getMyKeyPair() {
        return null;
    }

    /*
        Store a key for a provided partner name
     */
    void storePublicKey (String partnerName, String publicKey) {

    }

    /*
        Returns the public key associated with the
        provided partner name
     */
    public RSAPublicKey getPublicKey(String partnerName) {
        return null;
    }

    /*
        Erases current KeyPair and replaces it with a new KeyPair
     */
    public void resetMyKeyPair() {

    }

    /*
        Erases current public key for a specific partner
     */
    public void resetKey(String partnerName) {

    }

    public class KeyBinder extends Binder {
        public KeyBinder() {
        }

        /*
        Generate and/or retrieve a user’s RSA KeyPair. The first call to this
        method will generate and store the keypair before returning it. Subsequent calls will return the
        same key pair
     */
        public KeyPair getMyKeyPair() {
            return KeyService.this.getMyKeyPair();
        }

        /*
            Store a key for a provided partner name
         */
        void storePublicKey (String partnerName, String publicKey) {
            KeyService.this.storePublicKey(partnerName, publicKey);
        }

        /*
            Returns the public key associated with the
            provided partner name
         */
        public RSAPublicKey getPublicKey(String partnerName) {
            return KeyService.this.getPublicKey(partnerName);
        }

        /*
            Erases current KeyPair and replaces it with a new KeyPair
         */
        public void resetMyKeyPair() {
            KeyService.this.resetMyKeyPair();
        }

        /*
            Erases current public key for a specific partner
         */
        public void resetKey(String partnerName) {
            KeyService.this.resetKey(partnerName);
        }
    }
}
