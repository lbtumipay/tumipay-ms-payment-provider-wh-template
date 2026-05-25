package com.tumipay.microservice.infrastructure.component.security;

import com.tumipay.microservice.shared.properties.WebhookSignatureProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ProviderWebhookSignatureValidator.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProviderWebhookSignatureValidator Unit Tests")
class ProviderWebhookSignatureValidatorTest {

    @Mock private WebhookSignatureProperties webhookSignatureProperties;
    @Mock private WebhookPayloadBuilder payloadBuilder;
    @Mock private HmacSha256SignatureGenerator signatureGenerator;
    @Mock private WebhookTimestampValidator timestampValidator;
    @Mock private TimingSafeSignatureComparator signatureComparator;

    private ProviderWebhookSignatureValidator validator;

    private WebhookSignatureProperties enabledConfig;

    @BeforeEach
    void setUp() {
        validator = new ProviderWebhookSignatureValidator(
            webhookSignatureProperties,
            payloadBuilder,
            signatureGenerator,
            timestampValidator,
            signatureComparator
        );

        enabledConfig = new WebhookSignatureProperties();
        enabledConfig.setEnabled(true);
        enabledConfig.setSecret("test-secret");
        enabledConfig.setToleranceSeconds(300);
        enabledConfig.setSignatureHeader("event-signature");
        enabledConfig.setTimestampHeader("event-timestamp");
    }

    private HttpHeaders headersWithBoth(String timestamp, String signature) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("event-timestamp", timestamp);
        headers.set("event-signature", signature);
        return headers;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Happy path — valid signature
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When signature is valid")
    class ValidSignature {

        @Test
        @DisplayName("Should return success when all components pass validation")
        void validate_whenSignatureValid_thenSuccess() {
            when(webhookSignatureProperties.isEnabled()).thenReturn(enabledConfig.isEnabled());
            when(webhookSignatureProperties.getSecret()).thenReturn(enabledConfig.getSecret());
            when(webhookSignatureProperties.getToleranceSeconds()).thenReturn(enabledConfig.getToleranceSeconds());
            when(webhookSignatureProperties.getSignatureHeader()).thenReturn(enabledConfig.getSignatureHeader());
            when(webhookSignatureProperties.getTimestampHeader()).thenReturn(enabledConfig.getTimestampHeader());
            when(timestampValidator.isValid("valid-ts", 300)).thenReturn(true);
            when(payloadBuilder.build("valid-ts", "raw-body")).thenReturn("valid-ts.raw-body");
            when(signatureGenerator.generate("valid-ts.raw-body", "test-secret")).thenReturn("generated-sig");
            when(signatureComparator.matches("generated-sig", "valid-sig")).thenReturn(true);

            HttpHeaders headers = headersWithBoth("valid-ts", "valid-sig");

            StepVerifier.create(validator.validate("raw-body", headers))
                .assertNext(r -> {
                    assertThat(r.valid()).isTrue();
                    assertThat(r.reason()).isNull();
                })
                .verifyComplete();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Signature mismatch
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When signature does not match")
    class SignatureMismatch {

        @Test
        @DisplayName("Should return failure with 'Signature mismatch' when comparator returns false")
        void validate_whenSignatureMismatch_thenFailure() {
            when(webhookSignatureProperties.isEnabled()).thenReturn(enabledConfig.isEnabled());
            when(webhookSignatureProperties.getSecret()).thenReturn(enabledConfig.getSecret());
            when(webhookSignatureProperties.getToleranceSeconds()).thenReturn(enabledConfig.getToleranceSeconds());
            when(webhookSignatureProperties.getSignatureHeader()).thenReturn(enabledConfig.getSignatureHeader());
            when(webhookSignatureProperties.getTimestampHeader()).thenReturn(enabledConfig.getTimestampHeader());
            when(timestampValidator.isValid(anyString(), anyInt())).thenReturn(true);
            when(payloadBuilder.build(anyString(), anyString())).thenReturn("canonical");
            when(signatureGenerator.generate(anyString(), anyString())).thenReturn("generated-sig");
            when(signatureComparator.matches("generated-sig", "wrong-sig")).thenReturn(false);

            HttpHeaders headers = headersWithBoth("valid-ts", "wrong-sig");

            StepVerifier.create(validator.validate("raw-body", headers))
                .assertNext(r -> {
                    assertThat(r.valid()).isFalse();
                    assertThat(r.reason()).isEqualTo("Signature mismatch");
                })
                .verifyComplete();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Missing event-signature header
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When event-signature header is missing")
    class MissingSignatureHeader {

        @Test
        @DisplayName("Should return failure with 'Missing signature headers'")
        void validate_whenSignatureHeaderMissing_thenFailure() {
            when(webhookSignatureProperties.isEnabled()).thenReturn(enabledConfig.isEnabled());
            when(webhookSignatureProperties.getSignatureHeader()).thenReturn(enabledConfig.getSignatureHeader());
            when(webhookSignatureProperties.getTimestampHeader()).thenReturn(enabledConfig.getTimestampHeader());

            HttpHeaders headers = new HttpHeaders();
            headers.set("event-timestamp", "valid-ts");

            StepVerifier.create(validator.validate("raw-body", headers))
                .assertNext(r -> {
                    assertThat(r.valid()).isFalse();
                    assertThat(r.reason()).contains("Missing");
                })
                .verifyComplete();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Missing event-timestamp header
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When event-timestamp header is missing")
    class MissingTimestampHeader {

        @Test
        @DisplayName("Should return failure with 'Missing signature headers'")
        void validate_whenTimestampHeaderMissing_thenFailure() {
            when(webhookSignatureProperties.isEnabled()).thenReturn(enabledConfig.isEnabled());
            when(webhookSignatureProperties.getSignatureHeader()).thenReturn(enabledConfig.getSignatureHeader());
            when(webhookSignatureProperties.getTimestampHeader()).thenReturn(enabledConfig.getTimestampHeader());

            HttpHeaders headers = new HttpHeaders();
            headers.set("event-signature", "some-sig");

            StepVerifier.create(validator.validate("raw-body", headers))
                .assertNext(r -> {
                    assertThat(r.valid()).isFalse();
                    assertThat(r.reason()).contains("Missing");
                })
                .verifyComplete();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Timestamp expired
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When timestamp is expired")
    class ExpiredTimestamp {

        @Test
        @DisplayName("Should return failure with 'Timestamp expired or invalid'")
        void validate_whenTimestampExpired_thenFailure() {
            when(webhookSignatureProperties.isEnabled()).thenReturn(enabledConfig.isEnabled());
            when(webhookSignatureProperties.getToleranceSeconds()).thenReturn(enabledConfig.getToleranceSeconds());
            when(webhookSignatureProperties.getSignatureHeader()).thenReturn(enabledConfig.getSignatureHeader());
            when(webhookSignatureProperties.getTimestampHeader()).thenReturn(enabledConfig.getTimestampHeader());
            when(timestampValidator.isValid(anyString(), anyInt())).thenReturn(false);

            HttpHeaders headers = headersWithBoth("old-ts", "some-sig");

            StepVerifier.create(validator.validate("raw-body", headers))
                .assertNext(r -> {
                    assertThat(r.valid()).isFalse();
                    assertThat(r.reason()).isEqualTo("Timestamp expired or invalid");
                })
                .verifyComplete();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Validation disabled (enabled = false)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When signature validation is disabled")
    class ValidationDisabled {

        @Test
        @DisplayName("Should return success immediately without calling any crypto component")
        void validate_whenDisabled_thenSuccessWithoutCryptoComponents() {
            WebhookSignatureProperties disabledConfig =
                new WebhookSignatureProperties();
            disabledConfig.setEnabled(false);
            when(webhookSignatureProperties.isEnabled()).thenReturn(disabledConfig.isEnabled());

            HttpHeaders headers = new HttpHeaders();

            StepVerifier.create(validator.validate("raw-body", headers))
                .assertNext(r -> assertThat(r.valid()).isTrue())
                .verifyComplete();

            verifyNoInteractions(signatureGenerator, payloadBuilder, timestampValidator, signatureComparator);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. Signature generation returns null
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When signature generation fails")
    class SignatureGenerationFails {

        @Test
        @DisplayName("Should return failure with 'Signature generation failed' when generator returns null")
        void validate_whenGeneratorReturnsNull_thenFailure() {
            when(webhookSignatureProperties.isEnabled()).thenReturn(enabledConfig.isEnabled());
            when(webhookSignatureProperties.getSecret()).thenReturn(enabledConfig.getSecret());
            when(webhookSignatureProperties.getToleranceSeconds()).thenReturn(enabledConfig.getToleranceSeconds());
            when(webhookSignatureProperties.getSignatureHeader()).thenReturn(enabledConfig.getSignatureHeader());
            when(webhookSignatureProperties.getTimestampHeader()).thenReturn(enabledConfig.getTimestampHeader());
            when(timestampValidator.isValid(anyString(), anyInt())).thenReturn(true);
            when(payloadBuilder.build(anyString(), anyString())).thenReturn("canonical");
            when(signatureGenerator.generate(anyString(), anyString())).thenReturn(null);

            HttpHeaders headers = headersWithBoth("valid-ts", "some-sig");

            StepVerifier.create(validator.validate("raw-body", headers))
                .assertNext(r -> {
                    assertThat(r.valid()).isFalse();
                    assertThat(r.reason()).isEqualTo("Signature generation failed");
                })
                .verifyComplete();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. Unexpected RuntimeException — Mono never errors
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When an unexpected exception occurs")
    class UnexpectedException {

        @Test
        @DisplayName("Should emit failure result instead of Mono error")
        void validate_whenRuntimeException_thenFailureNotMonoError() {
            when(webhookSignatureProperties.isEnabled())
                .thenThrow(new RuntimeException("unexpected"));

            HttpHeaders headers = new HttpHeaders();

            StepVerifier.create(validator.validate("raw-body", headers))
                .assertNext(r -> {
                    assertThat(r.valid()).isFalse();
                    assertThat(r.reason()).isEqualTo("Internal validation error");
                })
                .verifyComplete();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. Replay attack simulation — comparator never reached
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Replay attack prevention")
    class ReplayAttack {

        @Test
        @DisplayName("Should fail before reaching comparator when timestamp is expired")
        void validate_whenReplayAttack_thenComparatorNeverInvoked() {
            when(webhookSignatureProperties.isEnabled()).thenReturn(enabledConfig.isEnabled());
            when(webhookSignatureProperties.getToleranceSeconds()).thenReturn(enabledConfig.getToleranceSeconds());
            when(webhookSignatureProperties.getSignatureHeader()).thenReturn(enabledConfig.getSignatureHeader());
            when(webhookSignatureProperties.getTimestampHeader()).thenReturn(enabledConfig.getTimestampHeader());
            when(timestampValidator.isValid(anyString(), anyInt())).thenReturn(false);

            HttpHeaders headers = headersWithBoth("400-seconds-ago", "some-sig");

            StepVerifier.create(validator.validate("raw-body", headers))
                .assertNext(r -> assertThat(r.valid()).isFalse())
                .verifyComplete();

            verifyNoInteractions(signatureComparator);
        }
    }
}
