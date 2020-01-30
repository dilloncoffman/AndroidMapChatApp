package edu.temple.androidrsa_assignment1_cis4515;

import android.content.Intent;
import android.os.IBinder;
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
import java.security.PublicKey;
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
    private HashMap<String, PublicKey> testPartnersMap = new HashMap<>();
    private String originalMessage = "hi there";

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
        ((KeyService.KeyBinder) binder).storePublicKey("dillon", kp.getPublic());
        // Ensure partnersMap is not null after storing public key
        assertNotNull(KeyService.partnersMap);

        // Store same public key in local testPartnersMap and check that they're equal
        testPartnersMap.put("dillon", kp.getPublic());
        assertEquals(testPartnersMap, KeyService.partnersMap);
    }

    @Test
    public void canGetPublicKey() {
        // Store public key for a user
        ((KeyService.KeyBinder) binder).storePublicKey("dillon", kp.getPublic());
        // Ensure partnersMap is not null after storing public key
        assertNotNull(KeyService.partnersMap);

        // Check that you can get a partner's public key by their name
        PublicKey pk = ((KeyService.KeyBinder) binder).getPublicKey("dillon");
        assertEquals(kp.getPublic(), pk);
    }

    @Test
    public void canResetMyKeyPair() {
        KeyPair newKeyPair = ((KeyService.KeyBinder) binder).resetMyKeyPair();
        assertNotEquals(kp, newKeyPair);
    }

    @Test
    public void canResetPublicKey() {
        // Store public key for a user
        ((KeyService.KeyBinder) binder).storePublicKey("dillon", kp.getPublic());
        // Ensure partnersMap is not null after storing public key
        assertNotNull(KeyService.partnersMap);
        // Check that you can get a partner's public key by their name
        PublicKey pk = ((KeyService.KeyBinder) binder).getPublicKey("dillon");
        assertEquals(kp.getPublic(), pk);

        // Reset partner's public key
        ((KeyService.KeyBinder) binder).resetPublicKey("dillon");
        // Public key in KeyService should be null while kp.getPublic() should still have the same public key it did before it was reset
        assertNotEquals(kp.getPublic(), KeyService.myPublicKey);
    }

    @Test
    public void canEncryptMessage() throws NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException {
        // Encrypt originalMessage
        byte[] encryptedMsg = ((KeyService.KeyBinder) binder).encryptMessage(originalMessage, kp.getPublic());
        System.out.println(Arrays.toString(encryptedMsg));
        // Ensure newly encrypted message is not null
        assertNotNull(encryptedMsg);
        // Ensure encrypted message is not the same as the original message
        assertNotEquals(encryptedMsg, originalMessage);
    }

    @Test
    public void canDecryptMessage() throws NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException {
        // Encrypt originalMessage first
        byte[] encryptedMsg = ((KeyService.KeyBinder) binder).encryptMessage(originalMessage, kp.getPublic());
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