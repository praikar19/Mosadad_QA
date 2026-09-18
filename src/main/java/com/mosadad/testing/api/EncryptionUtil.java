package com.mosadad.testing.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Reproduces the Angular front end's field-level "encryption" used on
 * {@code POST /tenant/User/Login} (and password-reset flows) — the login
 * form never sends plaintext email/password.
 *
 * Reverse-engineered from the live QA bundle
 * ({@code chunk-AEIDNK5X.js}, class around {@code xorEncrypt}/{@code createDerivedKey}):
 *
 * <pre>
 *   derivedKey = SHA-256( UTF8(staticEncryptionKey) )      // 32 bytes
 *   cipherBytes[i] = plaintextBytes[i] XOR derivedKey[i % 32]
 *   ciphertext = Base64(cipherBytes)
 * </pre>
 *
 * This is a transport-obfuscation scheme, not real cryptography (the key is
 * a static string shipped in the public JS bundle) — it exists purely so
 * the API test suite can drive the real {@code /User/Login} endpoint the
 * same way the browser does, without a browser. Verified byte-for-byte
 * against a live captured login request on 2026-09-17: encrypting
 * "Dubaiqa@gmail.com" with this exact algorithm reproduces the ciphertext
 * the Angular app actually sent.
 */
public final class EncryptionUtil {

    /**
     * Static key embedded in the Angular bundle (chunk-AEIDNK5X.js →
     * {@code encryptionKey:"WAgwm7f3!cMeqS&r*!K?Wq9qfteX5kXH"}). Not a
     * secret in any real sense — it ships to every browser — but kept as a
     * named constant rather than inlined so a future key rotation is a
     * one-line fix.
     */
    public static final String LOGIN_ENCRYPTION_KEY = "WAgwm7f3!cMeqS&r*!K?Wq9qfteX5kXH";

    private EncryptionUtil() {}

    /** Derives the 32-byte XOR key: SHA-256 of the UTF-8 bytes of {@code key}. */
    private static byte[] deriveKey(String key) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(key.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Encrypts {@code plaintext} exactly as the Angular {@code AuthService}
     * does before putting it in the {@code /User/Login} request body.
     *
     * @param plaintext the raw value (email or password)
     * @param key       the static key to derive the XOR keystream from —
     *                  pass {@link #LOGIN_ENCRYPTION_KEY} for login/reset flows
     * @return Base64-encoded ciphertext, or "" if plaintext is null/empty (matches JS behavior)
     */
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

    /** Convenience overload using {@link #LOGIN_ENCRYPTION_KEY}. */
    public static String encryptLoginField(String plaintext) {
        return xorEncrypt(plaintext, LOGIN_ENCRYPTION_KEY);
    }

    /** Inverse of {@link #xorEncrypt} — XOR is symmetric, so encrypt and decrypt share the same routine. */
    public static String xorDecrypt(String base64Ciphertext, String key) {
        if (base64Ciphertext == null || base64Ciphertext.isEmpty()) return "";
        byte[] derivedKey = deriveKey(key);
        byte[] cipherBytes = Base64.getDecoder().decode(base64Ciphertext);
        byte[] plainBytes = new byte[cipherBytes.length];
        for (int i = 0; i < cipherBytes.length; i++) {
            plainBytes[i] = (byte) (cipherBytes[i] ^ derivedKey[i % derivedKey.length]);
        }
        return new String(plainBytes, StandardCharsets.UTF_8);
    }
}
