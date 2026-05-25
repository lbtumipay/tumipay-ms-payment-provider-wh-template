DROP TRIGGER IF EXISTS tp_trg_provider_webhook_event_history;

CREATE TRIGGER tp_trg_provider_webhook_event_history
AFTER UPDATE ON tp_provider_webhook_event
FOR EACH ROW
CALL "com.tumipay.microservice.infrastructure.adapter.output.persistence.trigger.H2WebhookStatusHistoryTrigger";

