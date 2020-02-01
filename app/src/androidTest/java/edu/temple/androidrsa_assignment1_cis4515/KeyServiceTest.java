package edu.temple.androidrsa_assignment1_cis4515;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ServiceTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class KeyServiceTest {
    private IBinder binder;
    private KeyPair kp;
    private HashMap<String, RSAPublicKey> testPartnersMap = new HashMap<>();
    private String originalMessage = "hi there";
    private SharedPreferences prefs;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Before
    public void setUp() throws Exception {
        // Ensures KeyService is started before any tests run
        mServiceRule.startService(
                new Intent(InstrumentationRegistry.getTargetContext(), KeyService.class));

        // Create the service Intent
        Intent keyServiceIntent = new Intent(ApplicationProvider.getApplicationContext(),
                KeyService.class);

        // Bind the service and grab a reference to the binder to use in multiple test cases
        binder = mServiceRule.bindService(keyServiceIntent);

        // Generate key pair in set up since we'll be using that same key pair for other test cases
        kp = ((KeyService.KeyBinder) binder).getMyKeyPair();

        // SharedPreferences used to store partner's public keys
        prefs = PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getTargetContext());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void canGenerateKeyPair() {
        Log.d("KeyPair Generated: ", " ");
        Log.d("Private key: ", kp.getPrivate().toString());
        Log.d("Public key: ", kp.getPublic().toString());
        assertNotNull(kp);
        assertNotNull(kp.getPrivate());
        assertNotNull(kp.getPublic());
    }

    @Test
    public void canStorePublicKey() {
        // Stores Dillon's public key's modulus and exponent in SharedPreferences as Strings
        ((KeyService.KeyBinder) binder).storePublicKey("dillon", (RSAPublicKey) kp.getPublic());

        // Test String values of what partner's public key's modulus and exponent should be
        String testPartnerPublicKeyExponent = ((RSAPublicKey) kp.getPublic()).getPublicExponent().toString();
        String testPartnerPublicKeyModulus = ((RSAPublicKey) kp.getPublic()).getModulus().toString();

        // Data in SharedPreferences after being stored
        String storedPartnerPublicKeyExponent = prefs.getString("dillonExponentForPublicKey", testPartnerPublicKeyExponent);
        String storedPartnerPublicKeyModulus = prefs.getString("dillonModulusForPublicKey", testPartnerPublicKeyExponent);

        // Are the test values equal to what was actually stored in SharedPreferences?
        assertEquals(testPartnerPublicKeyExponent, storedPartnerPublicKeyExponent);
        assertEquals(testPartnerPublicKeyModulus, storedPartnerPublicKeyModulus);
    }

    @Test
    public void canGetPublicKey() {
        // Store public key for a user - really this entails storing their original key's modulus and exponent to be used to regenerate their key when a user requests it
        ((KeyService.KeyBinder) binder).storePublicKey("dillon", (RSAPublicKey) kp.getPublic());
        // Check that you can get a partner's public key by their name
        RSAPublicKey pk = ((KeyService.KeyBinder) binder).getPublicKey("dillon");
        assertEquals(kp.getPublic(), pk);
    }

    @Test
    public void canResetMyKeyPair() {
        KeyPair newKeyPair = ((KeyService.KeyBinder) binder).resetMyKeyPair();
        assertNotEquals(kp, newKeyPair);
    }

    @Test
    public void canResetPublicKey() {
        // Store test partner's public key
        ((KeyService.KeyBinder) binder).storePublicKey("dillon", (RSAPublicKey) kp.getPublic());

        // Reset test partner's public key
        ((KeyService.KeyBinder) binder).resetPublicKey("dillon");

        // Public key stored for test partner dillon should not be equal to current public key generated in KeyPair
        assertNotEquals((((RSAPublicKey) kp.getPublic()).getPublicExponent()), prefs.getString("dillonExponentForPublicKey", ""));
        assertNotEquals((((RSAPublicKey) kp.getPublic()).getModulus()), prefs.getString("dillonModulusForPublicKey", ""));
    }

    @Test
    public void canEncryptMessage() throws NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException {
        // Encrypt originalMessage
        byte[] encryptedMsg = ((KeyService.KeyBinder) binder).encryptMessage(originalMessage, (RSAPublicKey) kp.getPublic());
        System.out.println(Arrays.toString(encryptedMsg));
        // Ensure newly encrypted message is not null
        assertNotNull(encryptedMsg);
        // Ensure encrypted message is not the same as the original message
        assertNotEquals(encryptedMsg, originalMessage);
    }

    @Test
    public void canDecryptMessage() throws NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException {
        // Encrypt originalMessage
        byte[] encryptedMsg = ((KeyService.KeyBinder) binder).encryptMessage(originalMessage, (RSAPublicKey) kp.getPublic());
        System.out.println("Original message is: "+originalMessage);
        System.out.println("Encrypted message is: "+Arrays.toString(encryptedMsg));
        // Ensure newly encrypted message is not null
        assertNotNull(encryptedMsg);
        // Ensure encrypted message is not the same as the original message
        assertNotEquals(encryptedMsg, originalMessage);
        // Decrypt encrypted message and see if it matches original
        String decryptedMessage = ((KeyService.KeyBinder) binder).decryptMessage(encryptedMsg, kp.getPrivate());
        System.out.println("Decrypted message is: "+decryptedMessage);
        assertEquals(originalMessage, decryptedMessage);
    }
}