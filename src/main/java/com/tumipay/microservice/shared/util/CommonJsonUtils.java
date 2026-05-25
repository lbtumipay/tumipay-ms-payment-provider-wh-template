package com.tumipay.microservice.shared.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JsonMapperUtils
 * <p>
 * Utility class responsible for providing a centralized and standardized
 * {@link com.google.gson.Gson} instance for JSON serialization and deserialization across the TumiPay platform.
 * <p>
 * This helper enforces the official JSON contract defined by TumiPay, ensuring:
 * <ul>
 *   <li>Consistent {@code snake_case} field naming</li>
 *   <li>Explicit serialization of {@code null} values</li>
 *   <li>Support for Java Time API types</li>
 *   <li>A single immutable {@link Gson} instance shared across the application</li>
 * </ul>
 *
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS
 * AND PROCEDURE AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 6/01/2026
 */
@UtilityClass
public class CommonJsonUtils {

    private static final Gson GSON = new GsonBuilder()

        // JSON contract
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .serializeNulls()

        // Java Time support
        .registerTypeAdapter(Instant.class, new InstantTypeAdapter())

        .create();

    public static Gson gson() {
        return GSON;
    }

    // =========================
    // SERIALIZATION
    // =========================

    /**
     * Converts an object to JSON string.
     */
    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    /**
     * Converts an object to JSON string safely.
     * Returns null if serialization fails.
     */
    public static String toJsonSafe(Object object) {
        try {
            return GSON.toJson(object);
        } catch (Exception ex) {
            return null;
        }
    }

    // =========================
    // DESERIALIZATION
    // =========================

    /**
     * Converts JSON string to object.
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /**
     * Converts JSON string to object safely.
     * Returns null if parsing fails.
     */
    public static <T> T fromJsonSafe(String json, Class<T> clazz) {
        try {
            return GSON.fromJson(json, clazz);
        } catch (JsonSyntaxException ex) {
            return null;
        }
    }

    /**
     * Converts JSON string to generic type.
     */
    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    /**
     * Converts JSON string to List of objects.
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        Type type = TypeToken.getParameterized(List.class, clazz).getType();
        return GSON.fromJson(json, type);
    }

    /**
     * Converts JSON string to Map.
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return GSON.fromJson(json, type);
    }

    // =========================
    // VALIDATION
    // =========================

    /**
     * Validates if a string is valid JSON.
     */
    public static boolean isValidJson(String json) {
        try {
            final Object instance = GSON.fromJson(json, Object.class);
            return Objects.nonNull(instance);
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }

    /**
     * Gson adapter for java.time.Instant.
     */
    private static final class InstantTypeAdapter
        implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

        @Override
        public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Instant.parse(json.getAsString());
        }
    }
}