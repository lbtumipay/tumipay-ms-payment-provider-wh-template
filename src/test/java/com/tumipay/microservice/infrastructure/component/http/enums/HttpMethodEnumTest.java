package com.tumipay.microservice.infrastructure.component.http.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("HttpMethodEnum Unit Tests")
class HttpMethodEnumTest {

    @Test
    @DisplayName("values - should contain all supported methods in declared order")
    void values_shouldContainAllSupportedMethods() {
        assertArrayEquals(
            new HttpMethodEnum[]{
                HttpMethodEnum.GET,
                HttpMethodEnum.POST,
                HttpMethodEnum.PUT,
                HttpMethodEnum.PATCH,
                HttpMethodEnum.DELETE
            },
            HttpMethodEnum.values()
        );
    }

    @Test
    @DisplayName("valueOf - should resolve declared enum names")
    void valueOf_shouldResolveDeclaredEnumNames() {
        assertEquals(HttpMethodEnum.GET, HttpMethodEnum.valueOf("GET"));
        assertEquals(HttpMethodEnum.POST, HttpMethodEnum.valueOf("POST"));
        assertEquals(HttpMethodEnum.PUT, HttpMethodEnum.valueOf("PUT"));
        assertEquals(HttpMethodEnum.PATCH, HttpMethodEnum.valueOf("PATCH"));
        assertEquals(HttpMethodEnum.DELETE, HttpMethodEnum.valueOf("DELETE"));
    }
}

