package com.tumipay.microservice.shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CommonUuidUtilsTest2
 * <p>
 * CommonUuidUtilsTest2 class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@DisplayName("CommonUuidUtils Unit Tests")
class CommonUuidUtilsTest {

    @Test
    @DisplayName("Should validate UUID object references")
    void shouldValidateUuidObjectReferences() {
        assertFalse(CommonUuidUtils.isValidId((UUID) null));
        assertTrue(CommonUuidUtils.isValidId(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Should validate UUID string values")
    void shouldValidateUuidStringValues() {
        String validUuid = "550e8400-e29b-41d4-a716-446655440000";

        assertFalse(CommonUuidUtils.isValidId((String) null));
        assertFalse(CommonUuidUtils.isValidId(""));
        assertFalse(CommonUuidUtils.isValidId("not-a-uuid"));
        assertTrue(CommonUuidUtils.isValidId(validUuid));
    }
}