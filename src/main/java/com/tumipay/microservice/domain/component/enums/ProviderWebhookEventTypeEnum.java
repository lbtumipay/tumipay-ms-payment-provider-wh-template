package com.tumipay.microservice.domain.component.enums;

import lombok.Getter;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * ProviderWebhookEventTypeEnum
 * <p>
 * ProviderWebhookEventTypeEnum enum.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
@Getter
public enum ProviderWebhookEventTypeEnum {

    /**
     * Enum representing a successfully approved pay-out transaction webhook event.
     */
    MONEY_MOVEMENTS(
        "money_movements",
        "COBRE PayOut Transaction Webhook Event: This event is triggered when a pay-out transaction is being processed. It indicates that the transaction has been received and is currently being handled by the system."
    ),
    /**
     * Enum representing an unknown or unrecognized webhook event type.
     */
    UNKNOWN_EVENT(
        "UNKNOWN_EVENT",
        "Unknown or unrecognized webhook event type."
    );

    private static final Map<String, ProviderWebhookEventTypeEnum> providerWebhookEventTypeMap = new HashMap<>();

    static {
        for (final ProviderWebhookEventTypeEnum providerWebhookEventTypeEnum : EnumSet.allOf(ProviderWebhookEventTypeEnum.class)) {
            providerWebhookEventTypeMap.put(providerWebhookEventTypeEnum.getCode(), providerWebhookEventTypeEnum);
        }
    }

    private final String code;
    private final String description;

    ProviderWebhookEventTypeEnum(final String code, final String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        return code;
    }

    public static ProviderWebhookEventTypeEnum getProviderWebhookEventTypeByCode(final String code) {
        return !code.isEmpty() ? providerWebhookEventTypeMap.get(code.toUpperCase(Locale.ROOT)) : null;
    }

    public static boolean exists(final String code) {
        return !code.isEmpty() && (providerWebhookEventTypeMap.containsKey(code.toUpperCase(Locale.ROOT)));
    }
}
