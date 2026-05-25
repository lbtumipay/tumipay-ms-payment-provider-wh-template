package com.tumipay.microservice.infrastructure.adapter.input.http.base;

import com.tumipay.microservice.infrastructure.component.dto.BaseApiResponse;
import com.tumipay.microservice.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaseControllerTest
 * <p>
 * Unit tests for {@link BaseController}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 07/04/2026
 */
@ExtendWith(MockitoExtension.class)
class BaseControllerTest {

    private BaseController baseController;

    @BeforeEach
    void setUp() {
        baseController = new BaseController();
    }

    // ────────────────────────────────────────────────────────────────────────────
    // mapToApiResponse
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("mapToApiResponse - should return HTTP 200 with SUCCESS status and the given data")
    void mapToApiResponse_shouldReturnOkResponseWithData() {
        final String data = "sample-payload";

        StepVerifier.create(baseController.mapToApiResponse(data))
            .assertNext(entity -> {
                assertNotNull(entity);
                assertEquals(HttpStatus.OK, entity.getStatusCode());

                BaseApiResponse<String> body = entity.getBody();
                assertNotNull(body);
                assertEquals("PROCESS_COMPLETED", body.getCode());
                assertEquals("SUCCESS", body.getStatus());
                assertEquals("Operation completed successfully", body.getMessage());
                assertEquals(data, body.getData());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("mapToApiResponse - should work with null data")
    void mapToApiResponse_shouldHandleNullData() {
        StepVerifier.create(baseController.<String>mapToApiResponse(null))
            .assertNext(entity -> {
                assertEquals(HttpStatus.OK, entity.getStatusCode());
                assertNotNull(entity.getBody());
                assertEquals("SUCCESS", entity.getBody().getStatus());
                assertNull(entity.getBody().getData());
            })
            .verifyComplete();
    }

    // ────────────────────────────────────────────────────────────────────────────
    // handleOnValidationError - ValidationException path
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleOnValidationError - should return HTTP 400 with error details when ValidationException is thrown")
    void handleOnValidationError_withValidationException_shouldReturnBadRequest() {
        final String errorCode    = "VAL-001";
        final String errorMessage = "Validation failed";
        final String fallbackData = "fallback";

        final ValidationException validationException = ValidationException.builder()
            .code(errorCode)
            .message(errorMessage)
            .errors(List.of())
            .build();

        final Function<Throwable, Mono<ResponseEntity<BaseApiResponse<String>>>> handler =
            baseController.handleOnValidationError("testOperation", fallbackData);

        StepVerifier.create(handler.apply(validationException))
            .assertNext(entity -> {
                assertNotNull(entity);
                assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

                BaseApiResponse<String> body = entity.getBody();
                assertNotNull(body);
                assertEquals(errorCode, body.getCode());
                assertEquals(errorMessage, body.getMessage());
                assertEquals(fallbackData, body.getData());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("handleOnValidationError - should propagate the error when throwable is not a ValidationException")
    void handleOnValidationError_withOtherException_shouldPropagateError() {
        final RuntimeException unexpectedException = new RuntimeException("unexpected-error");

        final Function<Throwable, Mono<ResponseEntity<BaseApiResponse<String>>>> handler =
            baseController.handleOnValidationError("testOperation", "someData");

        StepVerifier.create(handler.apply(unexpectedException))
            .expectErrorSatisfies(thrown -> {
                assertInstanceOf(RuntimeException.class, thrown);
                assertEquals("unexpected-error", thrown.getMessage());
            })
            .verify();
    }

    @Test
    @DisplayName("handleOnValidationError - should include null fallback data in the response body")
    void handleOnValidationError_withNullFallbackData_shouldReturnBadRequestWithNullData() {
        final ValidationException validationException = ValidationException.builder()
            .code("VAL-002")
            .message("Field required")
            .errors(List.of())
            .build();

        final Function<Throwable, Mono<ResponseEntity<BaseApiResponse<String>>>> handler =
            baseController.handleOnValidationError("nullDataOperation", null);

        StepVerifier.create(handler.apply(validationException))
            .assertNext(entity -> {
                assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
                assertNotNull(entity.getBody());
                assertEquals("VAL-002", entity.getBody().getCode());
                assertEquals("Field required", entity.getBody().getMessage());
                assertNull(entity.getBody().getData());
            })
            .verifyComplete();
    }
}

