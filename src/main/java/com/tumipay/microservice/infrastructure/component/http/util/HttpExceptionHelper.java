package com.tumipay.microservice.infrastructure.component.http.util;

import com.tumipay.microservice.infrastructure.component.dto.BaseApiResponse;
import com.tumipay.microservice.infrastructure.component.http.enums.HttpMethodEnum;
import com.tumipay.microservice.shared.dto.CommonValidationResult;
import com.tumipay.microservice.shared.enums.BaseErrorCodeEnum;
import com.tumipay.microservice.shared.exception.BusinessException;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

/**
 * HttpExceptionHelper
 * <p>
 * HttpExceptionHelper class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 21/05/2026
 */
@Log4j2
@UtilityClass
public class HttpExceptionHelper {

    public static Mono<BusinessException> getProcessException(ClientResponse clientResponse) {
        return clientResponse
            .bodyToMono(
                new ParameterizedTypeReference<BaseApiResponse<CommonValidationResult>>() {
                })
            .defaultIfEmpty(new BaseApiResponse<>())
            .map(errorsBaseUserResponse -> {

                if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                    return new BusinessException(
                        BaseErrorCodeEnum.RESOURCE_NOT_FOUND.getCode(),
                        "Error in http integration process resource not found"
                    );
                }

                log.error(
                    "Error in http integration process, status code: {}, response body: {}",
                    clientResponse.statusCode(),
                    errorsBaseUserResponse
                );

                return new BusinessException(
                    BaseErrorCodeEnum.HTTP_INTEGRATION_ERROR.getCode(),
                    "http integration process error: " + clientResponse.toBodilessEntity().toString()
                );
            });
    }

    @NotNull
    public static BusinessException getNotResourceException() {

        return new BusinessException(
            BaseErrorCodeEnum.RESOURCE_NOT_FOUND.getCode(),
            "Error in http integration process resource not found"
        );
    }

    public static Throwable getUnsupportedHttpMethodException(HttpMethodEnum method) {
        return new BusinessException(
            BaseErrorCodeEnum.HTTP_INTEGRATION_ERROR.getCode(),
            "Unsupported HTTP method: " + method
        );
    }
}