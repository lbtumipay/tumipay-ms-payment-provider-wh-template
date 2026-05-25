package com.tumipay.microservice.application.dto;

import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.model.webhook.WebhookEventResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * WebhookEventCompositionTest
 * <p>
 * WebhookEventCompositionTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookEventComposition Unit Tests")
class WebhookEventCompositionTest {

    private WebhookEventComposition composition;
    private WebhookEvent webhookEvent;
    private WebhookEventResult webhookEventResult;

    @BeforeEach
    void setUp() {
        webhookEvent = mock(WebhookEvent.class);
        webhookEventResult = mock(WebhookEventResult.class);

        composition = WebhookEventComposition.builder()
            .webhookEvent(webhookEvent)
            .webhookEventResult(webhookEventResult)
            .build();
    }

    @Test
    @DisplayName("Should create WebhookEventComposition with builder")
    void testBuilderCreation() {
        assertNotNull(composition);
        assertEquals(webhookEvent, composition.getWebhookEvent());
        assertEquals(webhookEventResult, composition.getWebhookEventResult());
    }

    @Test
    @DisplayName("Should create WebhookEventComposition with no-args constructor")
    void testNoArgsConstructor() {
        WebhookEventComposition emptyComposition = new WebhookEventComposition();
        assertNotNull(emptyComposition);
        assertNull(emptyComposition.getWebhookEvent());
        assertNull(emptyComposition.getWebhookEventResult());
    }

    @Test
    @DisplayName("Should create WebhookEventComposition with all-args constructor")
    void testAllArgsConstructor() {
        WebhookEventComposition allArgsComposition = new WebhookEventComposition(
            webhookEvent,
            webhookEventResult
        );
        assertNotNull(allArgsComposition);
        assertEquals(webhookEvent, allArgsComposition.getWebhookEvent());
        assertEquals(webhookEventResult, allArgsComposition.getWebhookEventResult());
    }

    @Test
    @DisplayName("Should set webhookEvent field")
    void testSetWebhookEvent() {
        WebhookEvent newWebhookEvent = mock(WebhookEvent.class);
        composition.setWebhookEvent(newWebhookEvent);
        assertEquals(newWebhookEvent, composition.getWebhookEvent());
    }

    @Test
    @DisplayName("Should set webhookEventResult field")
    void testSetWebhookEventResult() {
        WebhookEventResult newResult = mock(WebhookEventResult.class);
        composition.setWebhookEventResult(newResult);
        assertEquals(newResult, composition.getWebhookEventResult());
    }

    @Test
    @DisplayName("Should generate equal objects with same data")
    void testEquality() {
        WebhookEventComposition composition2 = WebhookEventComposition.builder()
            .webhookEvent(webhookEvent)
            .webhookEventResult(webhookEventResult)
            .build();

        assertEquals(composition, composition2);
    }

    @Test
    @DisplayName("Should not be equal when webhookEvent differs")
    void testInequalityOnWebhookEvent() {
        WebhookEventComposition composition2 = WebhookEventComposition.builder()
            .webhookEvent(mock(WebhookEvent.class))
            .webhookEventResult(webhookEventResult)
            .build();

        assertNotEquals(composition, composition2);
    }

    @Test
    @DisplayName("Should not be equal when webhookEventResult differs")
    void testInequalityOnWebhookEventResult() {
        WebhookEventComposition composition2 = WebhookEventComposition.builder()
            .webhookEvent(webhookEvent)
            .webhookEventResult(mock(WebhookEventResult.class))
            .build();

        assertNotEquals(composition, composition2);
    }

    @Test
    @DisplayName("Should generate consistent hashCode")
    void testHashCode() {
        WebhookEventComposition composition2 = WebhookEventComposition.builder()
            .webhookEvent(webhookEvent)
            .webhookEventResult(webhookEventResult)
            .build();

        assertEquals(composition.hashCode(), composition2.hashCode());
    }

    @Test
    @DisplayName("Should generate meaningful toString")
    void testToString() {
        String toString = composition.toString();
        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        // The toString should contain class name
        assertTrue(toString.contains("WebhookEventComposition"));
    }

    @Test
    @DisplayName("Should serialize and deserialize correctly")
    void testSerialization() throws Exception {
        WebhookEventComposition serializableComposition = WebhookEventComposition.builder()
            .build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(serializableComposition);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        WebhookEventComposition deserializedComposition = (WebhookEventComposition) ois.readObject();
        ois.close();

        assertNotNull(deserializedComposition);
    }

    @Test
    @DisplayName("Should handle null webhookEvent")
    void testNullWebhookEvent() {
        WebhookEventComposition nullWebhookComposition = WebhookEventComposition.builder()
            .webhookEvent(null)
            .webhookEventResult(webhookEventResult)
            .build();

        assertNull(nullWebhookComposition.getWebhookEvent());
        assertEquals(webhookEventResult, nullWebhookComposition.getWebhookEventResult());
    }

    @Test
    @DisplayName("Should handle null webhookEventResult")
    void testNullWebhookEventResult() {
        WebhookEventComposition nullResultComposition = WebhookEventComposition.builder()
            .webhookEvent(webhookEvent)
            .webhookEventResult(null)
            .build();

        assertEquals(webhookEvent, nullResultComposition.getWebhookEvent());
        assertNull(nullResultComposition.getWebhookEventResult());
    }

    @Test
    @DisplayName("Should handle both fields null")
    void testBothFieldsNull() {
        WebhookEventComposition nullComposition = WebhookEventComposition.builder()
            .webhookEvent(null)
            .webhookEventResult(null)
            .build();

        assertNull(nullComposition.getWebhookEvent());
        assertNull(nullComposition.getWebhookEventResult());
    }

    @Test
    @DisplayName("Should correctly handle builder with only webhookEvent")
    void testBuilderWithOnlyWebhookEvent() {
        WebhookEventComposition partialComposition = WebhookEventComposition.builder()
            .webhookEvent(webhookEvent)
            .build();

        assertEquals(webhookEvent, partialComposition.getWebhookEvent());
        assertNull(partialComposition.getWebhookEventResult());
    }

    @Test
    @DisplayName("Should correctly handle builder with only webhookEventResult")
    void testBuilderWithOnlyWebhookEventResult() {
        WebhookEventComposition partialComposition = WebhookEventComposition.builder()
            .webhookEventResult(webhookEventResult)
            .build();

        assertNull(partialComposition.getWebhookEvent());
        assertEquals(webhookEventResult, partialComposition.getWebhookEventResult());
    }

    @Test
    @DisplayName("Should allow chaining with builder pattern")
    void testBuilderChaining() {
        WebhookEventComposition chainedComposition = WebhookEventComposition.builder()
            .webhookEvent(webhookEvent)
            .webhookEventResult(webhookEventResult)
            .build();

        assertNotNull(chainedComposition);
        assertNotNull(chainedComposition.getWebhookEvent());
        assertNotNull(chainedComposition.getWebhookEventResult());
    }

    @Test
    @DisplayName("Should be used for webhook processing pipeline")
    void testWebhookProcessingPipeline() {
        WebhookEventComposition processingComposition = WebhookEventComposition.builder()
            .webhookEvent(webhookEvent)
            .build();

        // Initially, webhookEventResult might be null during processing
        assertNull(processingComposition.getWebhookEventResult());

        // Later, it should be set with the result
        processingComposition.setWebhookEventResult(webhookEventResult);
        assertNotNull(processingComposition.getWebhookEventResult());
        assertEquals(webhookEventResult, processingComposition.getWebhookEventResult());
    }

    @Test
    @DisplayName("Should maintain immutability of data through copies")
    void testDataConsistency() {
        WebhookEventComposition original = WebhookEventComposition.builder()
            .webhookEvent(webhookEvent)
            .webhookEventResult(webhookEventResult)
            .build();

        WebhookEventComposition copy = WebhookEventComposition.builder()
            .webhookEvent(original.getWebhookEvent())
            .webhookEventResult(original.getWebhookEventResult())
            .build();

        assertEquals(original, copy);
        assertEquals(original.getWebhookEvent(), copy.getWebhookEvent());
        assertEquals(original.getWebhookEventResult(), copy.getWebhookEventResult());
    }
}
