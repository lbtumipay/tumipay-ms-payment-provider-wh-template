package com.tumipay.microservice.shared.mapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.mapstruct.Named;

import java.util.Map;

/**
 * ICommonMapper
 * <p>
 * ICommonMapper interface.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 31/03/2026
 */
public interface ICommonMapper {

    Gson GSON = new GsonBuilder()
        .serializeNulls()
        .create();

    @Named("objectToJson")
    default String objectToJson(Object value) {
        return value == null ? null : GSON.toJson(value);
    }

    @Named("stringToMap")
    default Map<String, Object> stringToMap(String value) {
        if (value == null) return null;
        return GSON.fromJson(value, new TypeToken<Map<String, Object>>(){}.getType());
    }

    @Named("mapMetadata")
    default Map<String, Object> mapMetadata(String value) {
        if (value == null) {
            return null;
        }
        return GSON.fromJson(value, new TypeToken<Map<String, Object>>() {}.getType());
    }
}
