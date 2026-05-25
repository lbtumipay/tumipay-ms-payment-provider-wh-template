package com.tumipay.microservice.shared.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonMapperUtilsTest
 * <p>
 * JsonMapperUtilsTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@DisplayName("JsonMapperUtils Unit Tests")
class CommonJsonUtilsTest {

    @Test
    @DisplayName("Should expose same Gson singleton instance")
    void shouldExposeSameGsonSingletonInstance() {
        Gson first = CommonJsonUtils.gson();
        Gson second = CommonJsonUtils.gson();

        assertNotNull(first);
        assertSame(first, second);
    }

    @Test
    @DisplayName("Should serialize object using snake_case and null fields")
    void shouldSerializeObjectUsingSnakeCaseAndNullFields() {
        SamplePayload payload = new SamplePayload("Ana", null);

        String json = CommonJsonUtils.toJson(payload);

        assertEquals("{\"first_name\":\"Ana\",\"middle_name\":null}", json);
    }

    @Test
    @DisplayName("Should return null on unsafe serialization errors")
    void shouldReturnNullOnUnsafeSerializationErrors() {
        String json = CommonJsonUtils.toJsonSafe(Double.POSITIVE_INFINITY);

        assertNull(json);
    }

    @Test
    @DisplayName("Should deserialize object and generic structures")
    void shouldDeserializeObjectAndGenericStructures() {
        SamplePayload payload = CommonJsonUtils.fromJson("{\"first_name\":\"Ana\",\"middle_name\":\"Maria\"}", SamplePayload.class);
        List<SamplePayload> payloads = CommonJsonUtils.fromJsonToList("[{\"first_name\":\"Ana\"}]", SamplePayload.class);
        Map<String, Object> map = CommonJsonUtils.fromJsonToMap("{\"amount\":10,\"currency\":\"USD\"}");

        assertEquals("Ana", payload.firstName);
        assertEquals("Maria", payload.middleName);
        assertEquals(1, payloads.size());
        assertEquals("Ana", payloads.get(0).firstName);
        assertEquals("USD", map.get("currency"));
    }

    @Test
    @DisplayName("Should support fromJson with explicit Type")
    void shouldSupportFromJsonWithExplicitType() {
        Type type = new TypeToken<List<String>>() { }.getType();

        List<String> values = CommonJsonUtils.fromJson("[\"A\",\"B\"]", type);

        assertEquals(List.of("A", "B"), values);
    }

    @Test
    @DisplayName("Should safely deserialize invalid JSON and validate JSON content")
    void shouldSafelyDeserializeInvalidJsonAndValidateJsonContent() {
        SamplePayload safeInvalid = CommonJsonUtils.fromJsonSafe("{invalid", SamplePayload.class);

        assertNull(safeInvalid);
        assertTrue(CommonJsonUtils.isValidJson("{\"k\":\"v\"}"));
        assertFalse(CommonJsonUtils.isValidJson("{invalid"));
        assertFalse(CommonJsonUtils.isValidJson(null));
    }

    private static class SamplePayload {
        private final String firstName;
        private final String middleName;

        private SamplePayload(String firstName, String middleName) {
            this.firstName = firstName;
            this.middleName = middleName;
        }
    }
}