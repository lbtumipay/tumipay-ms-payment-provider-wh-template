package com.tumipay.microservice.infrastructure.component.handler;

import com.fasterxml.jackson.core.JsonParseException;
import com.tumipay.microservice.infrastructure.component.dto.BaseApiResponse;
import com.tumipay.microservice.shared.dto.ValidationError;
import com.tumipay.microservice.shared.exception.BusinessException;
import com.tumipay.microservice.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebInputException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BaseGlobalExceptionHandler.
 * <p>
 * Verifies that every @ExceptionHandler method returns the correct HTTP status,
 * response codes, messages and validation error payloads, without starting
 * a Spring context.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BaseGlobalExceptionHandler Unit Tests")
class BaseGlobalExceptionHandlerTest {

    private BaseGlobalExceptionHandler handler;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MethodArgumentNotValidException beanValidationEx;

    @Mock
    private WebExchangeBindException webExchangeEx;

    @Mock
    private MethodArgumentTypeMismatchException typeMismatchEx;

    @BeforeEach
    void setUp() {
        handler = new BaseGlobalExceptionHandler();
    }

    // ── handleBeanValidation ───────────────────────────────────────────────────

    @Test
    @DisplayName("handleBeanValidation should return 400 with validation errors")
    void handleBeanValidationShouldReturn400WithValidationErrors() {
        FieldError fieldError = new FieldError("request", "amount", "must be positive");
        when(beanValidationEx.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Object> response = handler.handleBeanValidation(beanValidationEx);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        assertNotNull(body);
        assertEquals("VALIDATION_ERROR", body.getCode());
        assertEquals("ERROR", body.getStatus());
        assertEquals("Validation failed", body.getMessage());
        @SuppressWarnings("unchecked")
        List<ValidationError> errors = (List<ValidationError>) body.getData();
        assertEquals(1, errors.size());
        assertEquals("amount", errors.get(0).getField());
        assertEquals("must be positive", errors.get(0).getMessage());
    }

    @Test
    @DisplayName("handleBeanValidation should return empty errors list when binding has no field errors")
    void handleBeanValidationShouldReturnEmptyErrorsListWhenNoFieldErrors() {
        when(beanValidationEx.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<Object> response = handler.handleBeanValidation(beanValidationEx);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        @SuppressWarnings("unchecked")
        List<ValidationError> errors = (List<ValidationError>) body.getData();
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("handleBeanValidation should set Content-Type application/json")
    void handleBeanValidationShouldSetContentTypeApplicationJson() {
        when(beanValidationEx.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<Object> response = handler.handleBeanValidation(beanValidationEx);

        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    // ── handleValidationException ──────────────────────────────────────────────

    @Test
    @DisplayName("handleValidationException should return 400 propagating code message and errors")
    void handleValidationExceptionShouldReturn400PropagatingCodeMessageAndErrors() {
        List<ValidationError> errors = List.of(
                ValidationError.builder().field("currency").message("must not be blank").build()
        );
        ValidationException ex = new ValidationException("VAL-001", "validation failed", errors);

        ResponseEntity<Object> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        assertNotNull(body);
        assertEquals("VAL-001", body.getCode());
        assertEquals("ERROR", body.getStatus());
        assertEquals("validation failed", body.getMessage());
        assertEquals(errors, body.getData());
    }

    @Test
    @DisplayName("handleValidationException should handle null errors list")
    void handleValidationExceptionShouldHandleNullErrorsList() {
        ValidationException ex = new ValidationException("VAL-002", "no detail", null);

        ResponseEntity<Object> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        assertEquals("VAL-002", body.getCode());
    }

    // ── handleBusinessException ────────────────────────────────────────────────

    @Test
    @DisplayName("handleBusinessException should return 400 propagating code and message")
    void handleBusinessExceptionShouldReturn400PropagatingCodeAndMessage() {
        BusinessException ex = new BusinessException("BIZ-001", "business rule violated");

        ResponseEntity<Object> response = handler.handleBusinessException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        assertNotNull(body);
        assertEquals("BIZ-001", body.getCode());
        assertEquals("ERROR", body.getStatus());
        assertEquals("business rule violated", body.getMessage());
    }

    @Test
    @DisplayName("handleBusinessException should return 404 for RESOURCE_NOT_FOUND")
    void handleBusinessExceptionShouldReturn404ForResourceNotFound() {
        BusinessException ex = new BusinessException("RESOURCE_NOT_FOUND", "Transaction not found");

        ResponseEntity<Object> response = handler.handleBusinessException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        assertNotNull(body);
        assertEquals("RESOURCE_NOT_FOUND", body.getCode());
        assertEquals("ERROR", body.getStatus());
        assertEquals("Transaction not found", body.getMessage());
    }

    @Test
    @DisplayName("handleBusinessException response should have no data payload")
    void handleBusinessExceptionResponseShouldHaveNoDataPayload() {
        BusinessException ex = new BusinessException("BIZ-002", "some error");

        ResponseEntity<Object> response = handler.handleBusinessException(ex);

        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        assertNotNull(body);
        assertTrue(body.getData() == null || "".equals(body.getData()));
    }

    // ── handleTypeMismatch ─────────────────────────────────────────────────────

    @Test
    @DisplayName("handleTypeMismatch should return 400 with TYPE_MISMATCH code")
    void handleTypeMismatchShouldReturn400WithTypeMismatchCode() {
        when(typeMismatchEx.getMessage()).thenReturn("failed to convert value");

        ResponseEntity<Object> response = handler.handleTypeMismatch(typeMismatchEx);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        assertNotNull(body);
        assertEquals("TYPE_MISMATCH", body.getCode());
        assertEquals("ERROR", body.getStatus());
        assertEquals("Invalid parameter type", body.getMessage());
    }

    // ── handleWebInputException ────────────────────────────────────────────────

    @Test
    @DisplayName("handleWebInputException should return malformed JSON message when cause is JsonParseException")
    void handleWebInputExceptionShouldReturnMalformedJsonMessageWhenCauseIsJsonParseException() {
        JsonParseException cause = mock(JsonParseException.class);
        ServerWebInputException ex = mock(ServerWebInputException.class);
        when(ex.getCause()).thenReturn(cause);

        ResponseEntity<Object> response = handler.handleWebInputException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        assertNotNull(body);
        assertEquals("INVALID_REQUEST_BODY", body.getCode());
        assertEquals("ERROR", body.getStatus());
        assertEquals("Malformed JSON: syntax error in request body", body.getMessage());
    }

    @Test
    @DisplayName("handleWebInputException should return invalid structure message when cause is DecodingException")
    void handleWebInputExceptionShouldReturnInvalidStructureMessageWhenCauseIsDecodingException() {
        DecodingException cause = new DecodingException("cannot decode field");
        ServerWebInputException ex = new ServerWebInputException("decoding failed", null, cause);

        ResponseEntity<Object> response = handler.handleWebInputException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        assertNotNull(body);
        assertEquals("INVALID_REQUEST_BODY", body.getCode());
        assertEquals("Invalid request structure or field types", body.getMessage());
    }

    @Test
    @DisplayName("handleWebInputException should return generic message when cause is unknown")
    void handleWebInputExceptionShouldReturnGenericMessageWhenCauseIsUnknown() {
        ServerWebInputException ex = new ServerWebInputException("bad input");

        ResponseEntity<Object> response = handler.handleWebInputException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        assertNotNull(body);
        assertEquals("INVALID_REQUEST_BODY", body.getCode());
        assertEquals("Invalid request body", body.getMessage());
    }

    @Test
    @DisplayName("handleWebInputException should set Content-Type application/json")
    void handleWebInputExceptionShouldSetContentTypeApplicationJson() {
        ServerWebInputException ex = new ServerWebInputException("bad input");

        ResponseEntity<Object> response = handler.handleWebInputException(ex);

        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    // ── handleAllExceptions ────────────────────────────────────────────────────

    @Test
    @DisplayName("handleAllExceptions should return 500 with INTERNAL_SERVER_ERROR code")
    void handleAllExceptionsShouldReturn500WithInternalServerErrorCode() {
        Exception ex = new RuntimeException("unexpected failure");

        ResponseEntity<Object> response = handler.handleAllExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        assertNotNull(body);
        assertEquals("INTERNAL_SERVER_ERROR", body.getCode());
        assertEquals("ERROR", body.getStatus());
        assertEquals("unexpected failure", body.getMessage());
    }

    @Test
    @DisplayName("handleAllExceptions should propagate null message without throwing")
    void handleAllExceptionsShouldPropagateNullMessageWithoutThrowing() {
        Exception ex = new RuntimeException((String) null);

        ResponseEntity<Object> response = handler.handleAllExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        assertNotNull(body);
        assertEquals("INTERNAL_SERVER_ERROR", body.getCode());
    }

    // ── handleWebExchangeBindException ────────────────────────────────────────

    @Test
    @DisplayName("handleWebExchangeBindException should return 400 with field-level validation errors")
    void handleWebExchangeBindExceptionShouldReturn400WithFieldLevelErrors() {
        FieldError fieldError = new FieldError("request", "transactionId", "must not be null");
        when(webExchangeEx.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Object> response = handler.handleWebExchangeBindException(webExchangeEx);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        assertNotNull(body);
        assertEquals("VALIDATION_ERROR", body.getCode());
        assertEquals("ERROR", body.getStatus());
        assertEquals("Validation failed", body.getMessage());
        @SuppressWarnings("unchecked")
        List<ValidationError> errors = (List<ValidationError>) body.getData();
        assertEquals(1, errors.size());
        assertEquals("transactionId", errors.get(0).getField());
        assertEquals("must not be null", errors.get(0).getMessage());
    }

    @Test
    @DisplayName("handleWebExchangeBindException should return empty errors list when no field errors")
    void handleWebExchangeBindExceptionShouldReturnEmptyErrorsListWhenNoFieldErrors() {
        when(webExchangeEx.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<Object> response = handler.handleWebExchangeBindException(webExchangeEx);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        BaseApiResponse<?> body = (BaseApiResponse<?>) response.getBody();
        @SuppressWarnings("unchecked")
        List<ValidationError> errors = (List<ValidationError>) body.getData();
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("handleWebExchangeBindException should map multiple field errors correctly")
    void handleWebExchangeBindExceptionShouldMapMultipleFieldErrors() {
        List<FieldError> fieldErrors = List.of(
                new FieldError("request", "amount",   "must be positive"),
                new FieldError("request", "currency", "must not be blank")
        );
        when(webExchangeEx.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<Object> response = handler.handleWebExchangeBindException(webExchangeEx);

        @SuppressWarnings("unchecked")
        List<ValidationError> errors =
                (List<ValidationError>) ((BaseApiResponse<?>) response.getBody()).getData();
        assertEquals(2, errors.size());
        assertEquals("amount",   errors.get(0).getField());
        assertEquals("currency", errors.get(1).getField());
    }
}

