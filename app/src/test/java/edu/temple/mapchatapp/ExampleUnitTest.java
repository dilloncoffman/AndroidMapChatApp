package edu.temple.mapchatapp;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void isKeyGenValid() {
        // test getMyKeyPair() that a KeyPair is actually generated
    }

    @Test
    public void canSaveAndRetrievePublicKey() {
        // test storePublicKey and getPublicKey
    }

    @Test
    public void canRetrieveKeyByName() {
        // test getMyKeyPair() that you're able to access both the private and public keys in that KeyPair
    }

    @Test
    public void canEncryptAndDecryptArbitraryText() {
        // test encrypting and decrypting using KeyPair
    }
}