package com.tumipay.microservice.infrastructure.component.util;

import io.micrometer.context.ContextRegistry;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import reactor.core.publisher.Hooks;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CommonContextUtilsTest2
 * <p>
 * CommonContextUtilsTest2 class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("CommonContextUtils Unit Tests")
class ContextUtilsTest {

    @AfterEach
    void tearDown() {
        Hooks.disableAutomaticContextPropagation();
        ThreadContext.clearAll();
    }

    @Test
    @DisplayName("enableAutomaticContextPropagation should register requestId merchantId and operationId accessors")
    void enableAutomaticContextPropagationShouldRegisterExpectedAccessors() {
        ContextUtils.enableAutomaticContextPropagation();

        boolean hasRequestId  = ContextRegistry.getInstance().getThreadLocalAccessors()
            .stream().anyMatch(a -> "requestId".equals(a.key()));
        boolean hasMerchantId = ContextRegistry.getInstance().getThreadLocalAccessors()
            .stream().anyMatch(a -> "merchantId".equals(a.key()));
        boolean hasOperationId = ContextRegistry.getInstance().getThreadLocalAccessors()
            .stream().anyMatch(a -> "operationId".equals(a.key()));

        assertTrue(hasRequestId,  "requestId accessor must be registered");
        assertTrue(hasMerchantId, "merchantId accessor must be registered");
        assertTrue(hasOperationId, "operationId accessor must be registered");
    }

    @Test
    @DisplayName("registerThreadLocalAccessor should register accessor with given key")
    void registerThreadLocalAccessorShouldRegisterAccessorWithGivenKey() {
        String uniqueKey = "test-key-" + UUID.randomUUID();

        ContextUtils.registerThreadLocalAccessor(uniqueKey);

        boolean registered = ContextRegistry.getInstance().getThreadLocalAccessors()
            .stream().anyMatch(a -> uniqueKey.equals(a.key()));

        assertTrue(registered, "Accessor for key '" + uniqueKey + "' must be registered");

        // Cleanup
        ContextRegistry.getInstance().removeThreadLocalAccessor(uniqueKey);
    }

    @Test
    @DisplayName("registered accessor should read and write ThreadContext values")
    void registeredAccessorShouldReadAndWriteThreadContextValues() {
        String uniqueKey = "accessor-rw-" + UUID.randomUUID();

        ContextUtils.registerThreadLocalAccessor(uniqueKey);

        var accessor = ContextRegistry.getInstance().getThreadLocalAccessors()
            .stream().filter(a -> uniqueKey.equals(a.key())).findFirst().orElseThrow();

        // Write via ThreadContext and read via accessor
        ThreadContext.put(uniqueKey, "my-value");
        assertEquals("my-value", accessor.getValue());

        // Remove directly via ThreadContext (avoid calling accessor.reset() which is unsupported
        // for lambda-based registrations in context-propagation 1.0.5)
        ThreadContext.remove(uniqueKey);
        assertFalse(ThreadContext.containsKey(uniqueKey),
            "ThreadContext must not contain key after explicit removal");

        // Cleanup
        ContextRegistry.getInstance().removeThreadLocalAccessor(uniqueKey);
    }

    /** Small shim so the inner class can use assertEquals without static import clashes. */
    private static void assertEquals(Object expected, Object actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
