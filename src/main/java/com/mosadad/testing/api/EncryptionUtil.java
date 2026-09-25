package com.mosadad.testing.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Reproduces the front end's login field obfuscation (not real crypto):
 * Base64(plaintext XOR SHA-256(key)), so API tests can call /User/Login.
 */
public final class EncryptionUtil {

    /** Static key from the Angular bundle — public, not a secret. */
    public static final String LOGIN_ENCRYPTION_KEY = "WAgwm7f3!cMeqS&r*!K?Wq9qfteX5kXH";

    private EncryptionUtil() {}

    private static byte[] deriveKey(String key) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(key.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static String xorEncrypt(String plaintext, String key) {
        if (plaintext == null || plaintext.isEmpty()) return "";
        byte[] derivedKey = deriveKey(key);
        byte[] plainBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] cipherBytes = new byte[plainBytes.length];
        for (int i = 0; i < plainBytes.length; i++) {
            cipherBytes[i] = (byte) (plainBytes[i] ^ derivedKey[i % derivedKey.length]);
        }
        return Base64.getEncoder().encodeToString(cipherBytes);
    }

    public static String encryptLoginField(String plaintext) {
        return xorEncrypt(plaintext, LOGIN_ENCRYPTION_KEY);
    }

}
