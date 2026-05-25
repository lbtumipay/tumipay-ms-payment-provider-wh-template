package com.tumipay.microservice.application.component.mapper;

import com.tumipay.microservice.domain.component.enums.WebhookEventTypeEnum;
import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IWebhookEventMapperTest2
 * <p>
 * IWebhookEventMapperTest2 class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@DisplayName("IWebhookEventMapper Unit Tests")
class IWebhookEventMapperTest {

    private IWebhookEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(IWebhookEventMapper.class);
    }

    @Test
    @DisplayName("mapToPending returns null when both parameters are null")
    void mapToPendingBothNull() {
        assertNull(mapper.mapToPending(null, null));
    }

    @Test
    @DisplayName("mapToPending handles null event without throwing and applies defaults")
    void mapToPendingNullEvent() {
        WebhookEvent mapped = mapper.mapToPending(null, WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED);

        assertNotNull(mapped);
        assertEquals(WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED.name(), mapped.getEventType());
        assertEquals(WebhookProcessingStatusEnum.PENDING, mapped.getProcessingStatus());
        assertEquals(0, mapped.getRetryCount());
        assertNotNull(mapped.getNextRetryAt());
        assertNotNull(mapped.getUpdatedAt());
    }

    @Test
    @DisplayName("mapToPending uses UNKNOWN_EVENT when classifiedType is null")
    void mapToPendingNullClassifiedType() {
        Instant now = Instant.now();
        WebhookEvent source = WebhookEvent.builder()
            .id(10L)
            .uuid("w-10")
            .eventRequest("{}")
            .retryCount(3)
            .receivedAt(now)
            .createdAt(now)
            .build();

        WebhookEvent mapped = mapper.mapToPending(source, null);

        assertNotNull(mapped);
        assertEquals(10L, mapped.getId());
        assertEquals("w-10", mapped.getUuid());
        assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT.name(), mapped.getEventType());
        assertEquals(3, mapped.getRetryCount());
        assertEquals(WebhookProcessingStatusEnum.PENDING, mapped.getProcessingStatus());
    }

    @Test
    @DisplayName("mapToPending sets retryCount to zero when source retryCount is null")
    void mapToPendingNullRetryCount() {
        WebhookEvent source = WebhookEvent.builder()
            .id(11L)
            .uuid("w-11")
            .eventRequest("{}")
            .retryCount(null)
            .build();

        WebhookEvent mapped = mapper.mapToPending(source, WebhookEventTypeEnum.PAYIN_TRANSACTION_APPROVED);

        assertNotNull(mapped);
        assertEquals(0, mapped.getRetryCount());
        assertEquals(WebhookEventTypeEnum.PAYIN_TRANSACTION_APPROVED.name(), mapped.getEventType());
    }
}