package com.tumipay.microservice.infrastructure.component.util;

import com.tumipay.microservice.infrastructure.component.constant.BaseIntegrationConstant;
import lombok.experimental.UtilityClass;
import org.springframework.http.server.PathContainer;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * HttpWebFilterUtils
 * <p>
 * Utility class that provides helper functions used by HTTP WebFlux filters
 * to evaluate request paths and generate correlation identifiers within
 * the microservice infrastructure layer.
 * <p>
 * This component is designed to support reactive request filtering
 * mechanisms implemented using Spring WebFlux {@link ServerWebExchange}.
 * It contains reusable predicates that help determine whether an incoming
 * request belongs to infrastructure endpoints such as:
 * <ul>
 *     <li>Spring Boot Actuator endpoints</li>
 *     <li>Swagger / OpenAPI documentation endpoints</li>
 *     <li>Common health or monitoring endpoints</li>
 * </ul>
 * <p>
 * Additionally, this utility provides a method to generate a standardized
 * request correlation identifier used for:
 * <ul>
 *     <li>Distributed tracing</li>
 *     <li>Request tracking across microservices</li>
 *     <li>Logging correlation</li>
 * </ul>
 * <p>
 * The generated identifier follows the internal TumiPay format:
 * <pre>
 * TUMIPAY-{UUID}
 * </pre>
 * Example:
 * <pre>
 * TUMIPAY-9F4A2B88-4C9C-4D0A-A2C1-6B3B91A2D9B4
 * </pre>
 * <p>
 * This class is annotated with {@link UtilityClass}, meaning:
 * <ul>
 *     <li>All members are static</li>
 *     <li>No instances of this class can be created</li>
 *     <li>The class acts purely as a reusable helper container</li>
 * </ul>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT
 * STANDARDS AND PROCEDURE AND IS PROTECTED BY INTELLECTUAL PROPERTY
 * AND COPYRIGHT LAWS.
 *
 * @author TumiPay
 * @since 03/03/2026
 */
@UtilityClass
public class HttpWebFilterUtils {

    /**
     * Predicate function that evaluates whether the incoming request targets an Actuator endpoint.
     * Returns {@code false} if the exchange is null.
     */
    public final static Function<ServerWebExchange, Boolean> validateActuator = serverWebExchange -> {

        if (Objects.isNull(serverWebExchange))
            return false;

        return serverWebExchange.getRequest().getURI().getPath().contains(BaseIntegrationConstant.MICROSERVICE_PARAMETER_ACTUATOR_PATH_CONTAIN_VALUE);
    };

    /**
     * Predicate function that evaluates whether the incoming request targets a common
     * infrastructure endpoint (Actuator, Swagger, OpenAPI docs).
     * Returns {@code false} if the exchange is null.
     */
    public final static Function<ServerWebExchange, Boolean> validateCommonHealthEndpoints = serverWebExchange -> {

        if (Objects.isNull(serverWebExchange))
            return false;

        final PathContainer pathContainer = PathContainer.parsePath(serverWebExchange.getRequest().getPath().pathWithinApplication().value());

        return new PathPatternParser().parse(BaseIntegrationConstant.MICROSERVICE_COMMON_PATH_ACTUATOR_VALUE).matches(pathContainer) ||
            new PathPatternParser().parse(BaseIntegrationConstant.MICROSERVICE_COMMON_PATH_SWAGGER_RESOURCES_VALUE).matches(pathContainer) ||
            new PathPatternParser().parse(BaseIntegrationConstant.MICROSERVICE_COMMON_PATH_SWAGGER_UI_VALUE).matches(pathContainer) ||
            new PathPatternParser().parse(BaseIntegrationConstant.MICROSERVICE_COMMON_PATH_SWAGGER_V2_API_DOCS_VALUE).matches(pathContainer) ||
            new PathPatternParser().parse(BaseIntegrationConstant.MICROSERVICE_COMMON_PATH_SWAGGER_V3_API_DOCS_VALUE).matches(pathContainer);
    };

    /**
     * Generates a unique request correlation identifier following the TumiPay format:
     * {@code TUMIPAY-{UUID}}.
     *
     * @return a non-null uppercase request ID string.
     */
    public static String generateRequestId() {
        return String.format(
            "TUMIPAY-%s",
            UUID.randomUUID()
        ).toUpperCase(Locale.getDefault());
    }

    /**
     * Resolves the operation identifier from the request path of the given {@link ServerWebExchange}.
     * <p>
     * Returns {@code "operationId-not-found"} if the exchange is null or the path does not
     * match any known operation pattern.
     *
     * @param serverWebExchange the current server web exchange containing the request.
     * @return a string representing the resolved operation ID.
     */
    public static String getOperationIdFromPath(final ServerWebExchange serverWebExchange) {

        if (Objects.isNull(serverWebExchange))
            return "operationId-not-found";

        final var pathSegments = serverWebExchange.getRequest().getURI().getPath();

        if (pathSegments.contains("payin/transaction")) {
            return "PAY_IN_TRANSACTION";
        }

        if (pathSegments.equals("payout/transaction")) {
            return "PAY_OUT_TRANSACTION";
        }

        return "operationId-not-found";
    }
}