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
 *   <li>Consistent {@code snake_case} field naming using {@link com.google.gson.FieldNamingPolicy#LOWER_CASE_WITH_UNDERSCORES}</li>
 *   <li>Explicit serialization of {@code null} values to preserve API contracts</li>
 *   <li>A single, immutable {@link Gson} instance shared across the application</li>
 * </ul>
 * <p>
 * This component is designed to be framework-agnostic and can be safely used
 * in core libraries, SDKs, utilities, and shared components without introducing
 * dependencies on web or application frameworks.
 * <p>
 * <strong>Usage guidelines:</strong>
 * <ul>
 *   <li>All JSON serialization and deserialization within the library MUST use {@link #gson()}.</li>
 *   <li>Consumers should not create custom {@link Gson} instances to avoid contract inconsistencies.</li>
 *   <li>No Jackson annotations or serializers should be used in conjunction with this helper.</li>
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
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(Instant.class, instantSerializer())
        .registerTypeAdapter(Instant.class, instantDeserializer())
        .serializeNulls()
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
     * Converts JSON string to generic type (List, Map, etc.)
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
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return GSON.fromJson(json, type);
    }

    // =========================
    // VALIDATION
    // =========================

    /**
     * Validates if a string is a valid JSON.
     */
    public static boolean isValidJson(String json) {
        try {
            final Object instance = GSON.fromJson(json, Object.class);
            return Objects.nonNull(instance);
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }

    private static JsonSerializer<Instant> instantSerializer() {
        return (src, typeOfSrc, context) -> context.serialize(src.toString());
    }

    private static JsonDeserializer<Instant> instantDeserializer() {
        return (json, typeOfT, context) -> Instant.parse(json.getAsString());
    }
}
