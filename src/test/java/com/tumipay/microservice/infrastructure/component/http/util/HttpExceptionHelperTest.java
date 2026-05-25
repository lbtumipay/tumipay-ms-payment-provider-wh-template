package com.tumipay.microservice.infrastructure.component.http.util;

import com.tumipay.microservice.infrastructure.component.http.enums.HttpMethodEnum;
import com.tumipay.microservice.shared.enums.BaseErrorCodeEnum;
import com.tumipay.microservice.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("HttpExceptionHelper Unit Tests")
class HttpExceptionHelperTest {

    @Test
    @DisplayName("getProcessException - should map NOT_FOUND to RESOURCE_NOT_FOUND code")
    void getProcessException_shouldMapNotFoundToResourceNotFound() {
        ClientResponse response = ClientResponse.create(HttpStatus.NOT_FOUND).build();

        StepVerifier.create(HttpExceptionHelper.getProcessException(response))
            .assertNext(ex -> {
                assertEquals(BaseErrorCodeEnum.RESOURCE_NOT_FOUND.getCode(), ex.getCode());
                assertTrue(ex.getMessage().contains("resource not found"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("getProcessException - should map non-404 errors to HTTP_INTEGRATION_ERROR")
    void getProcessException_shouldMapServerErrorToHttpIntegrationError() {
        ClientResponse response = ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build();

        StepVerifier.create(HttpExceptionHelper.getProcessException(response))
            .assertNext(ex -> {
                assertEquals(BaseErrorCodeEnum.HTTP_INTEGRATION_ERROR.getCode(), ex.getCode());
                assertTrue(ex.getMessage().contains("http integration process error"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("getNotResourceException - should return RESOURCE_NOT_FOUND business exception")
    void getNotResourceException_shouldReturnExpectedBusinessException() {
        BusinessException exception = HttpExceptionHelper.getNotResourceException();

        assertEquals(BaseErrorCodeEnum.RESOURCE_NOT_FOUND.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("resource not found"));
    }

    @Test
    @DisplayName("getUnsupportedHttpMethodException - should include method name")
    void getUnsupportedHttpMethodException_shouldIncludeMethodName() {
        Throwable throwable = HttpExceptionHelper.getUnsupportedHttpMethodException(HttpMethodEnum.GET);

        assertTrue(throwable instanceof BusinessException);
        BusinessException exception = (BusinessException) throwable;
        assertEquals(BaseErrorCodeEnum.HTTP_INTEGRATION_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("GET"));
    }

    @Test
    @DisplayName("getUnsupportedHttpMethodException - should support null method")
    void getUnsupportedHttpMethodException_shouldSupportNullMethod() {
        Throwable throwable = HttpExceptionHelper.getUnsupportedHttpMethodException(null);

        assertTrue(throwable instanceof BusinessException);
        BusinessException exception = (BusinessException) throwable;
        assertEquals(BaseErrorCodeEnum.HTTP_INTEGRATION_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("null"));
    }
}

