package com.tumipay.microservice.domain.component.enums;

import lombok.Getter;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * ProviderWebhookEventStatusEnum
 * <p>
 * ProviderWebhookEventStatusEnum enum.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
@Getter
public enum ProviderWebhookEventStatusEnum {

    MONEY_MOVEMENTS_STATUS_INITIATED(
        "money_movements.status.initiated",
        "COBRE PayIn Transaction Webhook Event: This event is triggered when a pay-in transaction has been initiated. It indicates that the transaction has been created and is awaiting further processing."
    ),


    MONEY_MOVEMENTS_STATUS_PENDING_APPROVAL(
        "money_movements.status.pending_approval",
        "COBRE PayIn Transaction Webhook Event: This event is triggered when a pay-in transaction is pending approval. It indicates that the transaction is awaiting review and approval before it can proceed to the next stage."
    ),

    MONEY_MOVEMENTS_STATUS_PROCESSING(
        "money_movements.status.processing",
        "COBRE PayIn Transaction Webhook Event: This event is triggered when a pay-in transaction is being processed. It indicates that the transaction has been received and is currently being handled by the system."
    ),

    MONEY_MOVEMENTS_STATUS_COMPLETED(
        "money_movements.status.completed",
        "COBRE PayOut Transaction Webhook Event: This event is triggered when a pay-out transaction is being processed. It indicates that the transaction has been received and is currently being handled by the system."
    ),

    MONEY_MOVEMENTS_STATUS_REJECTED(
        "money_movements.status.rejected",
        "COBRE PayIn Transaction Webhook Event: This event is triggered when a pay-in transaction has been rejected. It indicates that the transaction has been reviewed and has not been approved for processing."
    ),

    UNKNOWN_STATUS(
        "UNKNOWN_STATUS",
        "Unknown or unrecognized webhook status."
    );

    private static final Map<String, ProviderWebhookEventStatusEnum> providerWebhookStatusTypeMap = new HashMap<>();

    static {
        for (final ProviderWebhookEventStatusEnum providerWebhookEventStatusEnum : EnumSet.allOf(ProviderWebhookEventStatusEnum.class)) {
            providerWebhookStatusTypeMap.put(providerWebhookEventStatusEnum.getCode(), providerWebhookEventStatusEnum);
        }
    }

    private final String code;
    private final String description;

    ProviderWebhookEventStatusEnum(final String code, final String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        return code;
    }

    public static ProviderWebhookEventStatusEnum getProviderWebhookEventStatusByCode(final String code) {
        return !code.isEmpty() ? providerWebhookStatusTypeMap.get(code.toUpperCase(Locale.ROOT)) : null;
    }

    public static boolean exists(final String code) {
        return !code.isEmpty() && (providerWebhookStatusTypeMap.containsKey(code.toUpperCase(Locale.ROOT)));
    }
}
