package com.tumipay.microservice.infrastructure.component.util;

import com.tumipay.microservice.infrastructure.component.constant.BaseIntegrationConstant;
import io.micrometer.context.ContextRegistry;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.ThreadContext;
import reactor.core.publisher.Hooks;

/**
 * CommonContextUtils
 * <p>
 * CommonContextUtils class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 25/03/2026
 */
@UtilityClass
public class ContextUtils {

    /**
     * Enables automatic context propagation for reactive pipelines and registers
     * the default thread-local accessors for request tracking keys.
     */
    public static void enableAutomaticContextPropagation() {
        Hooks.enableAutomaticContextPropagation();

        registerThreadLocalAccessor(BaseIntegrationConstant.KEY_REQUEST_ID);
        registerThreadLocalAccessor(BaseIntegrationConstant.KEY_MERCHANT_ID);
        registerThreadLocalAccessor(BaseIntegrationConstant.KEY_OPERATION_ID);
    }

    /**
     * Registers a thread-local accessor for the given key into the {@link ContextRegistry},
     * enabling bidirectional propagation between reactive context and Log4j2 thread context.
     *
     * @param key the context key to register (e.g., request ID, merchant ID, operation ID).
     */
    public static void registerThreadLocalAccessor(final String key) {
        ContextRegistry.getInstance().registerThreadLocalAccessor(
            key, () -> ThreadContext.get(key),
            value -> ThreadContext.put(key, value),
            () -> ThreadContext.remove(key)
        );
    }
}