package com.tumipay.microservice.infrastructure.adapter.output.persistence.trigger;

/**
 * H2 alias target used to keep an equivalent object name for tp_fn_webhook_status_history.
 */
public final class H2WebhookStatusHistoryFunction {

    private H2WebhookStatusHistoryFunction() {
    }

    public static void tpFnWebhookStatusHistory() {
        // No-op: history persistence is implemented by the H2 trigger class.
    }
}

