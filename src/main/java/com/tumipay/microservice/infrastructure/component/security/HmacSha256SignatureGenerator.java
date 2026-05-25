package com.tumipay.microservice.infrastructure.component.security;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Generates HMAC-SHA256 signatures for Cobre webhook payload verification.
 * <p>
 * Algorithm: {@code HmacSHA256}
 * Encoding: {@code UTF-8} for both payload and key
 * Output: lowercase hexadecimal string (64 characters)
 */
@Log4j2
@Component
public class HmacSha256SignatureGenerator {

    private static final String ALGORITHM = "HmacSHA256";

    /**
     * Generates the HMAC-SHA256 signature of the given payload using the provided secret key.
     *
     * @param payload   the canonical payload string ({@code timestamp + "." + rawBody})
     * @param secretKey the {@code event_signature_key} configured in the Cobre subscription
     * @return lowercase hex string of the HMAC-SHA256 digest, or {@code null} if generation fails
     */
    public String generate(String payload, String secretKey) {
        if (secretKey == null || secretKey.isEmpty()) {
            log.error("[WEBHOOK-SIGNATURE] Cannot generate HMAC-SHA256: secret key is null or empty");
            return null;
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                ALGORITHM
            );
            mac.init(keySpec);
            byte[] hashBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("[WEBHOOK-SIGNATURE] Failed to generate HMAC-SHA256 signature", e);
            return null;
        }
    }

    /**
     * Returns the raw HMAC-SHA256 digest bytes — used for timing-safe byte comparison.
     *
     * @param payload   the canonical payload string
     * @param secretKey the signature secret key
     * @return byte array of the HMAC-SHA256 digest, or an empty array if generation fails
     */
    public byte[] generateBytes(String payload, String secretKey) {
        if (secretKey == null || secretKey.isEmpty()) {
            log.error("[WEBHOOK-SIGNATURE] Cannot generate HMAC-SHA256 bytes: secret key is null or empty");
            return new byte[0];
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                ALGORITHM
            );
            mac.init(keySpec);
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("[WEBHOOK-SIGNATURE] Failed to generate HMAC-SHA256 bytes", e);
            return new byte[0];
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
