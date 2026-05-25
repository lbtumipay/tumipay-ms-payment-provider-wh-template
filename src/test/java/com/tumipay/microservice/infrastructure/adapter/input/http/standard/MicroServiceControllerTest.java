package com.tumipay.microservice.infrastructure.adapter.input.http.standard;

import com.tumipay.microservice.infrastructure.adapter.input.http.standard.response.MicroServiceInfoResponse;
import com.tumipay.microservice.infrastructure.component.properties.MicroServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * MicroServiceControllerTest
 * <p>
 * MicroServiceControllerTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@ExtendWith(MockitoExtension.class)
class MicroServiceControllerTest {

    @Mock
    private MicroServiceProperties microServiceProperties;

    private MicroServiceController controller;

    @BeforeEach
    void setUp() {
        controller = new MicroServiceController(microServiceProperties);
    }

    @Test
    void shouldReturnMicroserviceInfoSuccessfully() {
        when(microServiceProperties.getName()).thenReturn("tp-ms");
        when(microServiceProperties.getDescription()).thenReturn("payment provider");
        when(microServiceProperties.getVersion()).thenReturn("1.0.0");
        when(microServiceProperties.getEnvironment()).thenReturn("it");

        StepVerifier.create(controller.getMicroserviceInfo())
            .assertNext(entity -> {
                assertEquals(HttpStatus.OK, entity.getStatusCode());
                MicroServiceInfoResponse data = entity.getBody().getData();
                assertNotNull(data);
                assertEquals("tp-ms", data.getServiceName());
                assertEquals("payment provider", data.getServiceDescription());
                assertEquals("1.0.0", data.getVersion());
                assertEquals("it", data.getEnvironment());
                assertEquals("SUCCESS", entity.getBody().getStatus());
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnInternalServerErrorWhenPropertiesFail() {
        when(microServiceProperties.getName()).thenThrow(new RuntimeException("properties unavailable"));

        StepVerifier.create(controller.getMicroserviceInfo())
            .assertNext(entity -> {
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
                assertEquals("INTERNAL_SERVER_ERROR", entity.getBody().getCode());
                assertEquals("ERROR", entity.getBody().getStatus());
            })
            .verifyComplete();
    }
}