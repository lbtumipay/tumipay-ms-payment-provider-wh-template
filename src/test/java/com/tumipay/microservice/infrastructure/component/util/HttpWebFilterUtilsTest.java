package com.tumipay.microservice.infrastructure.component.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HttpWebFilterUtilsTest2
 * <p>
 * HttpWebFilterUtilsTest2 class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@DisplayName("HttpWebFilterUtils Unit Tests")
class HttpWebFilterUtilsTest {

    @Test
    @DisplayName("validateActuator should return true for actuator path")
    void validateActuatorShouldReturnTrueForActuatorPath() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/actuator/health").build()
        );
        assertTrue(HttpWebFilterUtils.validateActuator.apply(exchange));
    }

    @Test
    @DisplayName("validateActuator should return false for non-actuator path")
    void validateActuatorShouldReturnFalseForNonActuatorPath() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/tp/payment/adapter/payin").build()
        );
        assertFalse(HttpWebFilterUtils.validateActuator.apply(exchange));
    }

    @Test
    @DisplayName("validateActuator should return false for null exchange")
    void validateActuatorShouldReturnFalseForNullExchange() {
        assertFalse(HttpWebFilterUtils.validateActuator.apply(null));
    }


    @Test
    @DisplayName("validateCommonHealthEndpoints should return true for swagger-ui path")
    void validateCommonHealthEndpointsShouldReturnTrueForSwaggerUiPath() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/swagger-ui/index.html").build()
        );
        assertTrue(HttpWebFilterUtils.validateCommonHealthEndpoints.apply(exchange));
    }

    @Test
    @DisplayName("validateCommonHealthEndpoints should return true for swagger v3 api-docs path")
    void validateCommonHealthEndpointsShouldReturnTrueForSwaggerV3ApiDocsPath() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/v3/api-docs").build()
        );
        assertTrue(HttpWebFilterUtils.validateCommonHealthEndpoints.apply(exchange));
    }

    @Test
    @DisplayName("validateCommonHealthEndpoints should return false for business path")
    void validateCommonHealthEndpointsShouldReturnFalseForBusinessPath() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/tp/payment/adapter/payin").build()
        );
        assertFalse(HttpWebFilterUtils.validateCommonHealthEndpoints.apply(exchange));
    }

    @Test
    @DisplayName("validateCommonHealthEndpoints should return false for null exchange")
    void validateCommonHealthEndpointsShouldReturnFalseForNullExchange() {
        assertFalse(HttpWebFilterUtils.validateCommonHealthEndpoints.apply(null));
    }


    @Test
    @DisplayName("generateRequestId should return TUMIPAY-prefixed uppercase UUID")
    void generateRequestIdShouldReturnTumiPayPrefixedUppercaseUuid() {
        String requestId = HttpWebFilterUtils.generateRequestId();

        assertNotNull(requestId);
        assertTrue(requestId.startsWith("TUMIPAY-"));
        assertEquals(requestId, requestId.toUpperCase());
    }

    @Test
    @DisplayName("generateRequestId should produce unique values")
    void generateRequestIdShouldProduceUniqueValues() {
        String id1 = HttpWebFilterUtils.generateRequestId();
        String id2 = HttpWebFilterUtils.generateRequestId();

        assertFalse(id1.equals(id2));
    }


    @Test
    @DisplayName("getOperationIdFromPath should return PAY_IN_TRANSACTION for payin path")
    void getOperationIdFromPathShouldReturnPayInForPayinPath() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/tp/payment/adapter/payin/transaction").build()
        );
        assertEquals("PAY_IN_TRANSACTION", HttpWebFilterUtils.getOperationIdFromPath(exchange));
    }

    @Test
    @DisplayName("getOperationIdFromPath should return fallback for unrecognized path")
    void getOperationIdFromPathShouldReturnFallbackForUnrecognizedPath() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/tp/payment/adapter/other").build()
        );
        assertEquals("operationId-not-found", HttpWebFilterUtils.getOperationIdFromPath(exchange));
    }

    @Test
    @DisplayName("getOperationIdFromPath should return fallback for null exchange")
    void getOperationIdFromPathShouldReturnFallbackForNullExchange() {
        assertEquals("operationId-not-found", HttpWebFilterUtils.getOperationIdFromPath(null));
    }
}
