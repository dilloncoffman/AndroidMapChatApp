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

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Hashtable;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class KeyServiceTest {
    private IBinder binder;
    private KeyPair kp;
    private Hashtable<String, PublicKey> testPartnersTable = new Hashtable<>();
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
    }

    @Test
    public void canStorePublicKey() {
        ((KeyService.KeyBinder) binder).storePublicKey("dillon", kp.getPublic());
        // Ensure partnersTable is not null after storing public key
        assertNotNull(KeyService.partnersTable);

        // Store same public key in local testPartnersTable and check that they're equal
        testPartnersTable.put("dillon", kp.getPublic());
        assertEquals(testPartnersTable, KeyService.partnersTable);
    }

    @Test
    public void canGetPublicKey() {
    }

    @Test
    public void canResetMyKeyPair() {
    }

    @Test
    public void canResetPublicKey() {
    }

    @Test
    public void canEncryptMessage() {
    }

    @Test
    public void canDecryptMessage() {
    }
}