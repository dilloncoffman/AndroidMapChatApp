package edu.temple.mapchatapp;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class KeyService extends Service {
    private final KeyService.KeyBinder binder = new KeyService.KeyBinder();
    private KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    private KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    private String privateKeyString, publicKeyString;
    private KeyPair myKeyPair = null;
    public static RSAPublicKey myPublicKey;
    public static RSAPrivateKey myPrivateKey;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        return super.onStartCommand(intent, flags, startId);
    }

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
            myPrivateKey = (RSAPrivateKey) myKeyPair.getPrivate();
            myPublicKey = (RSAPublicKey) myKeyPair.getPublic();

            privateKeyString =  myPrivateKey.getPrivateExponent().toString();
            publicKeyString = myPublicKey.getPublicExponent().toString();

            RSAPrivateKeySpec privKeySpec = new RSAPrivateKeySpec(myPrivateKey.getModulus(), new BigInteger(privateKeyString));
            RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(myPublicKey.getModulus(), new BigInteger(publicKeyString));

            try {
                myPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(privKeySpec);
                myPublicKey = (RSAPublicKey) keyFactory.generatePublic(pubKeySpec);
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }

            // Store keys for main user
            editor.putString("mainUserPrivateKey", myPrivateKey.toString()).commit();
            storePublicKey("mainUser", myPublicKey);

            return myKeyPair;
        } else {
            return myKeyPair;
        }
    }

    /*
        Store a key for a provided partner name
     */
    void storePublicKey (String partnerName, RSAPublicKey publicKey) {
        // Store publicKey's exponent in order to convert the partner's public key back to RSAPublicKey on retrieval (RSAPublicKeySpec needs exponent)
        String publicKeyExponent = publicKey.getPublicExponent().toString();
        editor.putString(partnerName + "ExponentForPublicKey", publicKeyExponent); // add key-value to default SharedPreferences
        // Need to store publicKey's modulus for this specific partner in order to convert the partner's public key back to RSAPublicKey on retrieval (RSAPublicKeySpec needs modulus)
        String publicKeyModulus = publicKey.getModulus().toString();
        editor.putString(partnerName + "ModulusForPublicKey", publicKeyModulus);
        editor.commit(); // save key-value of partnerName and publicKeyExponent and of partnerName and publicKeyModulus
    }

    /*
        Returns the public key associated with the
        provided partner name
     */
    public RSAPublicKey getPublicKey(String partnerName) {
        // Convert stored partner's publicKey string to RSAPublicKey, need public key's exponent and modulus to do so
        String publicKeyModulus = null, publicKeyExponent = null;
        // Make sure partnerName exists somewhere in what's stored
        for (Map.Entry<String, ?> entry: prefs.getAll().entrySet()) {
            if (entry.getKey().contains(partnerName) && entry.getKey().contains("Modulus")) {
                // Get exponent for partner's public key
                publicKeyModulus = (String) entry.getValue();
            }
            if (entry.getKey().contains(partnerName) && entry.getKey().contains("Exponent")) {
                // Get exponent for partner's public key
                publicKeyExponent = (String) entry.getValue();
            }
        }

        // Generate key from same exponent and modulus as original key
        if (publicKeyExponent != null && publicKeyModulus != null) {
            RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(new BigInteger(publicKeyModulus), new BigInteger(publicKeyExponent));
            try {
                return (RSAPublicKey) keyFactory.generatePublic(pubKeySpec);
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }

        Log.d("KeyService", "Partner: "+partnerName+"may not have a public key stored to retrieve..");
        return null;
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
        myPrivateKey = (RSAPrivateKey) myKeyPair.getPrivate();
        myPublicKey = (RSAPublicKey) myKeyPair.getPublic();

        privateKeyString =  myPrivateKey.getPrivateExponent().toString();
        publicKeyString = myPublicKey.getPublicExponent().toString();

        RSAPrivateKeySpec privKeySpec = new RSAPrivateKeySpec(myPrivateKey.getModulus(), new BigInteger(privateKeyString));
        RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(myPublicKey.getModulus(), new BigInteger(publicKeyString));

        try {
            myPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(privKeySpec);
            myPublicKey = (RSAPublicKey) keyFactory.generatePublic(pubKeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return myKeyPair;
    }

    /*
        Erases current public key for a specific partner
    */
    public void resetPublicKey(String partnerName) {
        // Erase partner's stored public key's modulus and exponent values so it can't be regenerated
        for (Map.Entry<String, ?> entry: prefs.getAll().entrySet()) {
            if (entry.getKey().contains(partnerName) && (entry.getKey().contains("Modulus") || entry.getKey().contains("Exponent"))) {
                // Erase partner's key's modulus and exponent
                editor.putString(partnerName, "").commit();
            }
        }
    }

    /*
        Encrypt a message using the receiver's public key
     */
    public byte[] encryptMessage(String message, RSAPublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
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
        void storePublicKey (String partnerName, RSAPublicKey publicKey) {
            KeyService.this.storePublicKey(partnerName, publicKey);
        }

        /*
            Returns the public key associated with the
            provided partner name
            TODO Update RSAPublicKey to return RSARSAPublicKey, see code gist of casting kp.getPrivate() and public() to (RSARSAPublicKey)
         */
        public RSAPublicKey getPublicKey(String partnerName) {
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
            TODO KeyFactory should allow you to reset just RSAPublicKey for this instance, see code gist
         */
        public void resetPublicKey(String partnerName) {
            KeyService.this.resetPublicKey(partnerName);
        }

        /*
            Encrypt a message using the receiver's public key
        */
        public byte[] encryptMessage(String message, RSAPublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
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