package com.tumipay.microservice.application.component.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IBaseApplicationMapper interface default methods.
 * <p>
 * Tests every default method of the base mapper to verify correct behaviour
 * for null inputs, success/failure statuses and JSON serialization.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IBaseApplicationMapper Unit Tests")
class IBaseApplicationMapperTest {

    /** Anonymous concrete implementation used to exercise the default methods. */
    private IBaseApplicationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new IBaseApplicationMapper() {};
    }


    // ── toJson ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toJson should return null when data is null")
    void testToJson_NullData() {
        String result = mapper.toJson("provider", null);
        assertNull(result);
    }

    @Test
    @DisplayName("toJson should serialize a String value to JSON")
    void testToJson_StringData() {
        String result = mapper.toJson("provider", "hello");
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    @DisplayName("toJson should serialize a Map to a non-empty JSON string")
    void testToJson_MapData() {
        Map<String, Object> data = new HashMap<>();
        data.put("transaction_id", "TX-001");
        data.put("amount", 1000);

        String result = mapper.toJson("payload", data);

        assertNotNull(result);
        assertTrue(result.contains("TX-001"));
    }

    @Test
    @DisplayName("toJson identifier parameter does not alter serialized output")
    void testToJson_IdentifierDoesNotAffectOutput() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");

        String resultA = mapper.toJson("identifierA", data);
        String resultB = mapper.toJson("identifierB", data);

        assertEquals(resultA, resultB);
    }

    // ── createInstant ─────────────────────────────────────────────────────

    @Test
    @DisplayName("createInstant should return a non-null Instant")
    void testCreateInstant_ReturnsNonNull() {
        Instant result = mapper.createInstant("updatedAt");
        assertNotNull(result);
    }

    @Test
    @DisplayName("createInstant should return an Instant close to now")
    void testCreateInstant_IsCloseToNow() {
        Instant before = Instant.now().minusMillis(100);
        Instant result = mapper.createInstant("updatedAt");
        Instant after = Instant.now().plusMillis(100);

        assertTrue(result.isAfter(before), "Instant should be after 'before' boundary");
        assertTrue(result.isBefore(after), "Instant should be before 'after' boundary");
    }

    @Test
    @DisplayName("createInstant should work with any identifier string")
    void testCreateInstant_AnyIdentifier() {
        assertNotNull(mapper.createInstant("createdAt"));
        assertNotNull(mapper.createInstant("processedAt"));
        assertNotNull(mapper.createInstant(""));
    }

    // ── mapMetadataToJson ─────────────────────────────────────────────────

    @Test
    @DisplayName("mapMetadataToJson should return null when metadata is null")
    void testMapMetadataToJson_NullMetadata() {
        String result = mapper.mapMetadataToJson(null);
        assertNull(result);
    }

    @Test
    @DisplayName("mapMetadataToJson should serialize an empty map to '{}'")
    void testMapMetadataToJson_EmptyMap() {
        String result = mapper.mapMetadataToJson(new HashMap<>());
        assertNotNull(result);
        assertEquals("{}", result);
    }

    @Test
    @DisplayName("mapMetadataToJson should serialize a non-empty map to JSON")
    void testMapMetadataToJson_WithEntries() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("provider", "stripe");
        metadata.put("retries", 3);

        String result = mapper.mapMetadataToJson(metadata);

        assertNotNull(result);
        assertTrue(result.contains("stripe"));
        assertTrue(result.contains("3"));
    }

    @Test
    @DisplayName("mapMetadataToJson should preserve nested structure")
    void testMapMetadataToJson_NestedMap() {
        Map<String, Object> nested = new HashMap<>();
        nested.put("code", "PSE");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("payment_method", nested);

        String result = mapper.mapMetadataToJson(metadata);

        assertNotNull(result);
        assertTrue(result.contains("PSE"));
    }
}

