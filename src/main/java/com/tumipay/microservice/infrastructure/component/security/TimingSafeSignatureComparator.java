package com.tumipay.microservice.infrastructure.component.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Performs timing-safe comparison of HMAC-SHA256 signatures.
 * <p>
 * Uses {@link MessageDigest#isEqual(byte[], byte[])} instead of {@code String.equals()}
 * to prevent timing attacks where an attacker could infer partial signature matches
 * by measuring the response time of failed validation attempts.
 */
@Component
public class TimingSafeSignatureComparator {

    /**
     * Compares two signature strings in constant time.
     *
     * @param generated the HMAC-SHA256 signature computed locally
     * @param received  the signature received in the {@code event-signature} header
     * @return {@code true} if both signatures are identical; {@code false} otherwise
     */
    public boolean matches(String generated, String received) {
        if (generated == null || received == null) {
            return false;
        }
        byte[] generatedBytes = generated.getBytes(StandardCharsets.UTF_8);
        byte[] receivedBytes = received.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(generatedBytes, receivedBytes);
    }

    /**
     * Compares two signature byte arrays in constant time.
     * <p>
     * Preferred over {@link #matches(String, String)} when the generated signature
     * is already available as a byte array (avoids intermediate hex encoding).
     *
     * @param generatedBytes raw HMAC-SHA256 digest bytes
     * @param receivedHex    the hex-encoded signature from the {@code event-signature} header
     * @return {@code true} if the signatures match; {@code false} otherwise
     */
    public boolean matchesBytes(byte[] generatedBytes, String receivedHex) {
        if (generatedBytes == null || receivedHex == null) {
            return false;
        }
        byte[] receivedBytes = receivedHex.getBytes(StandardCharsets.UTF_8);
        StringBuilder hex = new StringBuilder(generatedBytes.length * 2);
        for (byte b : generatedBytes) {
            hex.append(String.format("%02x", b));
        }
        byte[] generatedHexBytes = hex.toString().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(generatedHexBytes, receivedBytes);
    }
}
