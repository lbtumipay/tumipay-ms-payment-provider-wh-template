DROP ALIAS IF EXISTS tp_fn_webhook_status_history;

CREATE ALIAS IF NOT EXISTS tp_fn_webhook_status_history
FOR "com.tumipay.microservice.infrastructure.adapter.output.persistence.trigger.H2WebhookStatusHistoryFunction.tpFnWebhookStatusHistory";

