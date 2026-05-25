package com.tumipay.microservice.infrastructure.adapter.input.http.base;

import com.tumipay.microservice.infrastructure.component.dto.BaseApiResponse;
import com.tumipay.microservice.shared.exception.ValidationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * BaseController
 * <p>
 * BaseController class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 27/03/2026
 */
@Log4j2
public class BaseController {

    public <T> Mono<ResponseEntity<BaseApiResponse<T>>> mapToApiResponse(final T response) {
        return Mono.just(ResponseEntity.ok(
            BaseApiResponse.success(response)
        ));
    }

    public <T> Function<Throwable, Mono<ResponseEntity<BaseApiResponse<T>>>> handleOnValidationError(String operation, T data) {

        return throwable -> {
            if (throwable instanceof ValidationException ve) {

                log.error("Error in {}}, validation exception occurred: {}", operation, ve.getMessage(), ve);
                log.error("Business exception occurred: {}", ve.getMessage(), ve);
                return Mono.just(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(BaseApiResponse.<T>builder()
                            .code(ve.getCode())
                            .message(ve.getMessage())
                            .data(data)
                            .build()
                        )
                );
            }

            return Mono.error(throwable);
        };
    }
}