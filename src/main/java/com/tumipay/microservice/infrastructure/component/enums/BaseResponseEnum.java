package com.tumipay.microservice.infrastructure.component.enums;

import com.tumipay.microservice.infrastructure.component.constant.BaseResponseConstant;
import lombok.Getter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * BaseResponseEnum
 * <p>
 * BaseResponseEnum enum.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 26/12/2025
 */
@Getter
public enum BaseResponseEnum {

    /**
     * Enum representing a successful HTTP response with its code, status and message.
     */
    SUCCESS_RESPONSE(BaseResponseConstant.SUCCESS_RESPONSE_CODE, BaseResponseConstant.SUCCESS_RESPONSE_STATUS, BaseResponseConstant.SUCCESS_RESPONSE_MESSAGE),

    /**
     * Enum representing an internal server error HTTP response with its code, status and message.
     */
    INTERNAL_ERROR_RESPONSE(BaseResponseConstant.INTERNAL_ERROR_RESPONSE_CODE, BaseResponseConstant.INTERNAL_ERROR_RESPONSE_STATUS, BaseResponseConstant.INTERNAL_ERROR_RESPONSE_MESSAGE);

    private static final Map<String, BaseResponseEnum> documentTypeMap = new HashMap<>();

    static {
        for (final BaseResponseEnum documentTypeEnum : EnumSet.allOf(BaseResponseEnum.class)) {
            documentTypeMap.put(documentTypeEnum.getCode(), documentTypeEnum);
        }
    }

    private final String code;
    private final String status;
    private final String message;

    BaseResponseEnum(final String code, final String status, final String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    @Override
    public String toString() {
        return code;
    }

    public static BaseResponseEnum getResponseByCode(final String code) {
        return !code.isEmpty() ? documentTypeMap.get(code.toUpperCase(Locale.ROOT)) : null;
    }

    public static boolean exists(final String code) {
        return !code.isEmpty() && (documentTypeMap.containsKey(code.toUpperCase(Locale.ROOT)));
    }
}
