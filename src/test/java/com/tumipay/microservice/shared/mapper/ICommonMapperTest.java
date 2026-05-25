package com.tumipay.microservice.shared.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ICommonMapper interface default methods.
 * <p>
 * Tests every default method of the common mapper to verify correct behaviour
 * for null inputs, JSON serialization and JSON-to-Map deserialization.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ICommonMapper Unit Tests")
class ICommonMapperTest {

    /** Anonymous concrete implementation used to exercise the default methods. */
    private ICommonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ICommonMapper() {};
    }

    // ── objectToJson ───────────────────────────────────────────────────────

    @Test
    @DisplayName("objectToJson should return null when value is null")
    void objectToJsonShouldReturnNullWhenValueIsNull() {
        assertNull(mapper.objectToJson(null));
    }

    @Test
    @DisplayName("objectToJson should serialize a simple object to JSON")
    void objectToJsonShouldSerializeSimpleObject() {
        SampleObject obj = new SampleObject("TumiPay", 42);

        String json = mapper.objectToJson(obj);

        assertNotNull(json);
        assertTrue(json.contains("TumiPay"));
        assertTrue(json.contains("42"));
    }

    @Test
    @DisplayName("objectToJson should serialize a Map to JSON")
    void objectToJsonShouldSerializeMap() {
        Map<String, Object> map = Map.of("currency", "USD", "amount", 100);

        String json = mapper.objectToJson(map);

        assertNotNull(json);
        assertTrue(json.contains("currency"));
        assertTrue(json.contains("USD"));
    }

    @Test
    @DisplayName("objectToJson should serialize a List to JSON")
    void objectToJsonShouldSerializeList() {
        List<String> list = List.of("A", "B", "C");

        String json = mapper.objectToJson(list);

        assertNotNull(json);
        assertTrue(json.contains("A"));
        assertTrue(json.contains("B"));
        assertTrue(json.contains("C"));
    }

    @Test
    @DisplayName("objectToJson should include null fields in JSON output")
    void objectToJsonShouldIncludeNullFields() {
        SampleObject obj = new SampleObject(null, 10);

        String json = mapper.objectToJson(obj);

        assertNotNull(json);
        assertTrue(json.contains("null"), "Gson serializeNulls must include null fields");
    }

    // ── stringToMap ────────────────────────────────────────────────────────

    @Test
    @DisplayName("stringToMap should return null when value is null")
    void stringToMapShouldReturnNullWhenValueIsNull() {
        assertNull(mapper.stringToMap(null));
    }

    @Test
    @DisplayName("stringToMap should deserialize a valid JSON object to Map")
    void stringToMapShouldDeserializeValidJsonToMap() {
        String json = "{\"currency\":\"COP\",\"amount\":500}";

        Map<String, Object> result = mapper.stringToMap(json);

        assertNotNull(result);
        assertEquals("COP", result.get("currency"));
        assertEquals(500.0, ((Number) result.get("amount")).doubleValue());
    }

    @Test
    @DisplayName("stringToMap should return map with nested objects")
    void stringToMapShouldReturnMapWithNestedObjects() {
        String json = "{\"provider\":{\"code\":\"PROV_001\",\"active\":true}}";

        Map<String, Object> result = mapper.stringToMap(json);

        assertNotNull(result);
        assertTrue(result.containsKey("provider"));
        @SuppressWarnings("unchecked")
        Map<String, Object> provider = (Map<String, Object>) result.get("provider");
        assertEquals("PROV_001", provider.get("code"));
    }

    // ── mapMetadata ────────────────────────────────────────────────────────

    @Test
    @DisplayName("mapMetadata should return null when value is null")
    void mapMetadataShouldReturnNullWhenValueIsNull() {
        assertNull(mapper.mapMetadata(null));
    }

    @Test
    @DisplayName("mapMetadata should deserialize a valid JSON metadata string to Map")
    void mapMetadataShouldDeserializeValidJsonToMap() {
        String json = "{\"requestId\":\"abc-123\",\"retries\":3}";

        Map<String, Object> result = mapper.mapMetadata(json);

        assertNotNull(result);
        assertEquals("abc-123", result.get("requestId"));
        assertEquals(3.0, ((Number) result.get("retries")).doubleValue());
    }

    @Test
    @DisplayName("mapMetadata should return empty map for empty JSON object")
    void mapMetadataShouldReturnEmptyMapForEmptyJsonObject() {
        Map<String, Object> result = mapper.mapMetadata("{}");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("mapMetadata and stringToMap should produce consistent results for same input")
    void mapMetadataAndStringToMapShouldProduceConsistentResultsForSameInput() {
        String json = "{\"key\":\"value\",\"count\":7}";

        Map<String, Object> fromStringToMap = mapper.stringToMap(json);
        Map<String, Object> fromMapMetadata  = mapper.mapMetadata(json);

        assertNotNull(fromStringToMap);
        assertNotNull(fromMapMetadata);
        assertEquals(fromStringToMap, fromMapMetadata);
    }

    // ── helper ────────────────────────────────────────────────────────────

    private static class SampleObject {
        private final String name;
        private final int value;

        SampleObject(String name, int value) {
            this.name  = name;
            this.value = value;
        }
    }
}

