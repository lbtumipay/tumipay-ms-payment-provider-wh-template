package com.tumipay.microservice.infrastructure.adapter.input.http.standard;

import com.tumipay.microservice.infrastructure.adapter.input.http.standard.response.MicroServiceInfoResponse;
import com.tumipay.microservice.infrastructure.component.properties.MicroServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringBootVersion;
import org.springframework.http.HttpStatus;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * MicroServiceHealthControllerTest
 * <p>
 * MicroServiceHealthControllerTest class.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 12/05/2026
 */
@ExtendWith(MockitoExtension.class)
class MicroServiceHealthControllerTest {

    @Mock
    private MicroServiceProperties microServiceProperties;

    private MicroServiceHealthController controller;

    @BeforeEach
    void setUp() {
        controller = new MicroServiceHealthController(microServiceProperties);
    }

    @Test
    void shouldReturnMicroserviceLivenessSuccessfully() {
        mockMicroserviceProperties();

        StepVerifier.create(controller.getMicroserviceLiveness())
            .assertNext(entity -> assertSuccessfulResponse(entity, "tp-ms", "payment provider", "1.0.0", "it"))
            .verifyComplete();
    }

    @Test
    void shouldReturnMicroserviceReadinessSuccessfully() {
        mockMicroserviceProperties();

        StepVerifier.create(controller.getMicroserviceReadiness())
            .assertNext(entity -> assertSuccessfulResponse(entity, "tp-ms", "payment provider", "1.0.0", "it"))
            .verifyComplete();
    }

    @Test
    void shouldReturnMicroserviceHealthSuccessfully() {
        mockMicroserviceProperties();

        StepVerifier.create(controller.getMicroserviceHealth())
            .assertNext(entity -> assertSuccessfulResponse(entity, "tp-ms", "payment provider", "1.0.0", "it"))
            .verifyComplete();
    }

    @Test
    void shouldReturnInternalServerErrorWhenPropertiesFail() {
        when(microServiceProperties.getName()).thenThrow(new RuntimeException("properties unavailable"));

        StepVerifier.create(controller.getMicroserviceLiveness())
            .assertNext(entity -> {
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
                assertEquals("INTERNAL_SERVER_ERROR", entity.getBody().getCode());
                assertEquals("ERROR", entity.getBody().getStatus());
                assertEquals("Internal Server Error", entity.getBody().getMessage());
            })
            .verifyComplete();
    }

    private void mockMicroserviceProperties() {
        when(microServiceProperties.getName()).thenReturn("tp-ms");
        when(microServiceProperties.getDescription()).thenReturn("payment provider");
        when(microServiceProperties.getVersion()).thenReturn("1.0.0");
        when(microServiceProperties.getEnvironment()).thenReturn("it");
    }

    private void assertSuccessfulResponse(
        org.springframework.http.ResponseEntity<com.tumipay.microservice.infrastructure.component.dto.BaseApiResponse<MicroServiceInfoResponse>> entity,
        String expectedName,
        String expectedDescription,
        String expectedVersion,
        String expectedEnvironment
    ) {
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertNotNull(entity.getBody());
        assertEquals("PROCESS_COMPLETED", entity.getBody().getCode());
        assertEquals("SUCCESS", entity.getBody().getStatus());
        assertEquals("Operation completed successfully", entity.getBody().getMessage());

        MicroServiceInfoResponse data = entity.getBody().getData();
        assertNotNull(data);
        assertEquals(expectedName, data.getServiceName());
        assertEquals(expectedDescription, data.getServiceDescription());
        assertEquals(expectedVersion, data.getVersion());
        assertEquals(expectedEnvironment, data.getEnvironment());
        assertEquals(System.getProperty("java.version"), data.getJavaVersion());
        assertEquals(SpringBootVersion.getVersion(), data.getSpringBootVersion());
        assertNotNull(data.getTimestamp());
    }
}
