package edu.temple.androidrsa_assignment1_cis4515;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;

public class KeyService extends Service {
    private final KeyService.KeyBinder binder = new KeyService.KeyBinder();
    private KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    private KeyPair myKeyPair = null;


    public KeyService() throws NoSuchAlgorithmException {
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
        if (myKeyPair == null) {
            myKeyPair = keyPairGenerator.generateKeyPair();
            return myKeyPair;
        } else {
            return myKeyPair;
        }
    }

    /*
        Store a key for a provided partner name
        TODO Should we store KeyPairs using KeyStore?
     */
    void storePublicKey (String partnerName, String publicKey) {

    }

    /*
        Returns the public key associated with the
        provided partner name
     */
    public RSAPublicKey getPublicKey(String partnerName) {
        // TODO Get public key associated with a specific partner in HashTable (HashTable<partnerName, partnerPublicKey>) OR should we represent a "partner" in a different way?
        return null;
    }

    /*
        Erases current KeyPair and replaces it with a new KeyPair
        TODO Safe to assume resetting KeyPair also entails generating a new KeyPair?
     */
    public void resetMyKeyPair() {
        // Erase current key pair
        if (myKeyPair != null) {
            myKeyPair = null;
        } else {
            Log.d("KeyService", "Currently no key pair set");
            return;
        }
        // Assuming you would want to generate a new key pair
        myKeyPair = keyPairGenerator.generateKeyPair();
    }

    /*
        Erases current public key for a specific partner
        TODO Safe to assume resetting KeyPair also entails generating a new KeyPair?
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
