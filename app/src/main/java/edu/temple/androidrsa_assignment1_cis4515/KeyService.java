package edu.temple.androidrsa_assignment1_cis4515;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class KeyService extends Service {
    private final KeyService.KeyBinder binder = new KeyService.KeyBinder();
    private KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    private KeyPair myKeyPair = null;
    public static HashMap<String, PublicKey> partnersMap = new HashMap<>();
    public static PublicKey myPublicKey;
    public static PrivateKey myPrivateKey;

    public KeyService() throws NoSuchAlgorithmException {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("KeyService", "KeyService bound");
        return this.binder;
    }

    /*
        Generate and/or retrieve a user’s RSA KeyPair. The first call to this
        method will generate and store the keypair before returning it.
        Subsequent calls will return the same key pair
     */
    public KeyPair getMyKeyPair() {
        if (myKeyPair == null) {
            myKeyPair = keyPairGenerator.generateKeyPair();
            myPrivateKey = myKeyPair.getPrivate();
            myPublicKey = myKeyPair.getPublic();
            return myKeyPair;
        } else {
            return myKeyPair;
        }
    }

    /*
        Store a key for a provided partner name
     */
    void storePublicKey (String partnerName, PublicKey publicKey) {
        // store public key in partnersMap HashMap
        partnersMap.put(partnerName, publicKey);
    }

    /*
        Returns the public key associated with the
        provided partner name
     */
    public PublicKey getPublicKey(String partnerName) {
        if (partnersMap.containsKey(partnerName)) {
            return partnersMap.get(partnerName);
        } else {
            Log.d("KeyService", "Partner: "+partnerName+" does not exist in the partnersMap");
            return null;
        }
    }

    /*
        Erases current KeyPair and replaces it with a new KeyPair
        Safe to assume resetting a KeyPair also entails generating a new KeyPair
     */
    public KeyPair resetMyKeyPair() {
        // Erase current key pair
        if (myKeyPair != null) {
            myKeyPair = null;
        }
        // Assuming you would want to generate a new key pair
        myKeyPair = keyPairGenerator.generateKeyPair();
        myPrivateKey = myKeyPair.getPrivate();
        myPublicKey = myKeyPair.getPublic();
        return myKeyPair;
    }

    /*
        Erases current public key for a specific partner
    */
    public void resetPublicKey(String partnerName) {
        if (partnersMap.containsKey(partnerName)) {
            // Reset that partner's public key
            partnersMap.replace(partnerName, null);
            myPublicKey = null;
        } else {
            Log.d("KeyService", "Partner: "+partnerName+" does not exist in the partnersMap");
        }
    }

    /*
        Encrypt a message using the receiver's public key
     */
    public byte[] encryptMessage(String message, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
    }

    /*
        Decrypt a message that was encrypted with the
        user's public key, using their private key
     */
    public String decryptMessage(byte[] encryptedMessage, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(encryptedMessage), StandardCharsets.UTF_8);
    }

    public class KeyBinder extends Binder {
        public KeyBinder() {
        }

        /*
        Generate and/or retrieve a user’s RSA KeyPair. The first call to this
        method will generate and store the keypair before returning it.
        Subsequent calls will return the same key pair
     */
        public KeyPair getMyKeyPair() {
            return KeyService.this.getMyKeyPair();
        }

        /*
            Store a key for a provided partner name
         */
        void storePublicKey (String partnerName, PublicKey publicKey) {
            KeyService.this.storePublicKey(partnerName, publicKey);
        }

        /*
            Returns the public key associated with the
            provided partner name
         */
        public PublicKey getPublicKey(String partnerName) {
            return KeyService.this.getPublicKey(partnerName);
        }

        /*
            Erases current KeyPair and replaces it with a new KeyPair
         */
        public KeyPair resetMyKeyPair() {
            return KeyService.this.resetMyKeyPair();
        }

        /*
            Erases current public key for a specific partner
         */
        public void resetPublicKey(String partnerName) {
            KeyService.this.resetPublicKey(partnerName);
        }

        /*
            Encrypt a message using the receiver's public key
        */
        public byte[] encryptMessage(String message, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
            return KeyService.this.encryptMessage(message, publicKey);
        }

        /*
            Decrypt a message that was encrypted with the
            user's public key, using their private key
         */
        public String decryptMessage(byte[] encryptedMessage, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
            return KeyService.this.decryptMessage(encryptedMessage, privateKey);
        }
    }
}