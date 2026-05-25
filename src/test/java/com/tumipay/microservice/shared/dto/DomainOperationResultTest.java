package com.tumipay.microservice.shared.dto;

import com.tumipay.microservice.domain.component.enums.OperationStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DomainOperationResultTest
 * <p>
 * Unit tests for {@link DomainOperationResult}.
 */
@DisplayName("DomainOperationResult Unit Tests")
class DomainOperationResultTest {

    @Test
    @DisplayName("success(entity) - should return SUCCESS status and entity")
    void success_shouldReturnSuccessStatusAndEntity() {
        DomainOperationResult<String> result = DomainOperationResult.success("OK");

        assertNotNull(result);
        assertEquals(OperationStatusEnum.SUCCESS, result.getStatus());
        assertEquals("OK", result.getEntity());
        assertNull(result.getErrorMessage());
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("failure(message) - should return FAILED status with message")
    void failureWithMessage_shouldReturnFailedStatus() {
        DomainOperationResult<String> result = DomainOperationResult.failure("validation failed");

        assertEquals(OperationStatusEnum.FAILED, result.getStatus());
        assertEquals("validation failed", result.getErrorMessage());
        assertNull(result.getEntity());
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("failure(message, errors) - should keep detailed errors")
    void failureWithMessageAndErrors_shouldKeepDetailedErrors() {
        List<String> errors = List.of("amount required", "currency invalid");

        DomainOperationResult<String> result = DomainOperationResult.failure("invalid payload", errors);

        assertEquals(OperationStatusEnum.FAILED, result.getStatus());
        assertEquals("invalid payload", result.getErrorMessage());
        assertEquals(errors, result.getErrors());
        assertTrue(result.isFailed());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("failure(message, errors) - should accept empty error list")
    void failureWithMessageAndErrors_shouldAcceptEmptyList() {
        DomainOperationResult<String> result = DomainOperationResult.failure("no details", Collections.emptyList());

        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("builder - should build instance with explicit fields")
    void builder_shouldBuildWithExplicitFields() {
        List<String> errors = List.of("e1", "e2");

        DomainOperationResult<String> result = DomainOperationResult.<String>builder()
            .status(OperationStatusEnum.FAILED)
            .errorMessage("builder error")
            .errors(errors)
            .entity("entity-x")
            .build();

        assertEquals(OperationStatusEnum.FAILED, result.getStatus());
        assertEquals("builder error", result.getErrorMessage());
        assertEquals(errors, result.getErrors());
        assertEquals("entity-x", result.getEntity());
    }

    @Test
    @DisplayName("noArgsConstructor - should create empty instance")
    void noArgsConstructor_shouldCreateEmptyInstance() {
        DomainOperationResult<Object> result = new DomainOperationResult<>();

        assertNotNull(result);
        assertNull(result.getStatus());
        assertNull(result.getErrorMessage());
        assertNull(result.getEntity());
    }

    @Test
    @DisplayName("isSuccess/isFailed - should return false when status is null")
    void statusChecks_shouldReturnFalseWhenStatusIsNull() {
        DomainOperationResult<Object> result = new DomainOperationResult<>();

        assertFalse(result.isSuccess());
        assertFalse(result.isFailed());
    }

    @Test
    @DisplayName("should implement Serializable")
    void shouldImplementSerializable() {
        assertInstanceOf(java.io.Serializable.class, DomainOperationResult.success("value"));
    }
}
