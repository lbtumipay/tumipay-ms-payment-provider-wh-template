package com.tumipay.microservice.infrastructure.component.constant;

import lombok.experimental.UtilityClass;

/**
 * InterceptorConstants
 * <p>
 * InterceptorConstants class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 26/12/2025
 */
@UtilityClass
public class BaseIntegrationConstant {

    // TumiPay standard headers for request tracing and idempotency
    public static final String HEADER_REQUEST_ID = "X-Request-ID";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    public static final String HEADER_MERCHANT_ID = "X-Merchant-ID";
    public static final String HEADER_IDEMPOTENCY_KEY = "X-Idempotency-Key";

    // Common header for API key authentication (if applicable)
    public static final String HEADER_API_KEY = "X-Api-Key";

    // Webhook specific headers
    public static final String HEADER_EVENT_ID = "X-Event-Id";
    public static final String HEADER_EVENT_TYPE = "X-Event-Type";

    // Gateway dispatch headers
    public static final String HEADER_ADAPTER_PROVIDER_CODE = "X-Adapter-Provider-Code";


    public static final String KEY_REQUEST_ID = "requestId";
    public static final String KEY_MERCHANT_ID = "merchantId";
    public static final String KEY_OPERATION_ID = "operationId";
    public static final String KEY_IDEMPOTENCY_KEY = "idempotencyKey";

    public static final String REQUEST_MESSAGE = "Request: {}";
    public static final String RESPONSE_MESSAGE = "Response: {}";
    public static final String REQUEST_PATH_VARIABLES_MESSAGE = "PathVariables: {}";
    public static final String REQUEST_QUERY_PARAMS_MESSAGE = "QueryParams: {}";
    public static final String REQUEST_HEADERS_MESSAGE = "Headers: {}";

    public static final String EXTERNAL_PATH_VARIABLES_MESSAGE = "ExternalPathVariables: {}";
    public static final String EXTERNAL_QUERY_PARAMS_MESSAGE = "ExternalQueryParams: {}";
    public static final String EXTERNAL_HEADERS_MESSAGE = "ExternalHeaders: {}";
    public static final String EXTERNAL_URI_MESSAGE = "ExternalUri: {}";
    public static final String EXTERNAL_REQUEST_MESSAGE = "ExternalRequest: {}";
    public static final String EXTERNAL_RESPONSE_MESSAGE = "ExternalResponse: {}";
    public static final String HTTP_WRITE_AND_FLUSH_WITH_FORMAT = "Response with state code[{}]";

    public static final String MICROSERVICE_PARAMETER_ACTUATOR_PATH_CONTAIN_VALUE = "actuator";
    public static final String MICROSERVICE_COMMON_PATH_ACTUATOR_VALUE = "/actuator/**";
    public static final String MICROSERVICE_COMMON_PATH_SWAGGER_RESOURCES_VALUE = "/swagger-resources/**";
    public static final String MICROSERVICE_COMMON_PATH_SWAGGER_UI_VALUE = "/swagger-ui/**";
    public static final String MICROSERVICE_COMMON_PATH_SWAGGER_V2_API_DOCS_VALUE = "/v2/api-docs";
    public static final String MICROSERVICE_COMMON_PATH_SWAGGER_V3_API_DOCS_VALUE = "/v3/api-docs";
}
