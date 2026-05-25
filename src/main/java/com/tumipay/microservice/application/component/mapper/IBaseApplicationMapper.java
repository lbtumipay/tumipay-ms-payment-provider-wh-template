package com.tumipay.microservice.application.component.mapper;

import com.tumipay.microservice.shared.util.CommonJsonUtils;

import java.time.Instant;
import java.util.Map;

/**
 * IBaseApplicationMapper
 * <p>
 * IBaseApplicationMapper interface.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 27/03/2026
 */
public interface IBaseApplicationMapper {


    default <T> String toJson(String identifier, T data) {
        if (data == null) {
            return null;
        }

        try {
            return CommonJsonUtils.toJson(data);
        } catch (Exception e) {
            return "{\"type\":\"" + identifier + "\",\"error\":\"serialization_error\"}";
        }
    }

    default Instant createInstant(String identifier) {
        return Instant.now();
    }

    default String mapMetadataToJson(Map<String, Object> metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            return CommonJsonUtils.toJson(metadata);
        } catch (Exception e) {
            return "{\"type\":\"metadata\",\"error\":\"serialization_error\"}";
        }
    }
}
