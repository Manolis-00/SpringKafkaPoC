package org.example.kafka.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.example.dto.OrderEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Kafka producer
 * Tests message sending without actually connecting to Kafka
 */
@ExtendWith(MockitoExtension.class)
public class OrderProducerTest {

    @Mock
    private KafkaTemplate<String, OrderEventDTO> kafkaTemplate;

    private OrderProducer orderProducer;

    private final String TEST_TOPIC = "test_order-events";

    @BeforeEach
    void setUp() {
        orderProducer = new OrderProducer(kafkaTemplate);

        // Using reflection to set the topic name
        ReflectionTestUtils.setField(orderProducer, "orderEventsTopic", TEST_TOPIC);
    }

    @Test
    void testSendOrderEventSuccess() {
        // Given
        OrderEventDTO orderEventDTO = createTestOrderEvent();
        CompletableFuture<SendResult<String, OrderEventDTO>> successFuture = createSuccessfulFuture(orderEventDTO);

        // Set up mock to return successful future
        when(kafkaTemplate.send(TEST_TOPIC, "123", orderEventDTO)).thenReturn(successFuture);

        // When
        orderProducer.sendOrderEvent(orderEventDTO);

        // Then
        verify(kafkaTemplate).send(TEST_TOPIC, "123", orderEventDTO);
    }

    @Test
    void testSendOrderEventFailure() {
        // Given
        OrderEventDTO orderEventDTO = createTestOrderEvent();
        CompletableFuture<SendResult<String, OrderEventDTO>> failureFuture = new CompletableFuture<>();
        failureFuture.completeExceptionally(new RuntimeException("Kafka connection failed"));

        when(kafkaTemplate.send(anyString(), anyString(), any(OrderEventDTO.class))).thenReturn(failureFuture);

        // When
        orderProducer.sendOrderEvent(orderEventDTO);

        // The
        verify(kafkaTemplate).send(TEST_TOPIC, "123", orderEventDTO);
    }

    @Test
    void testSendOrderEventWithNullOrderId() {
        // Given
        OrderEventDTO orderEventDTO = OrderEventDTO.builder()
                .orderId(null)
                .customerName("Test Customer")
                .eventType("CREATE")
                .build();

        when(kafkaTemplate.send(anyString(), eq("null"), any(OrderEventDTO.class))).thenReturn(new CompletableFuture<>());

        // When
        orderProducer.sendOrderEvent(orderEventDTO);

        // Then
        verify(kafkaTemplate).send(TEST_TOPIC, "null", orderEventDTO);
    }

    /**
     * Helpers to make the test more readable
     *
     * @return - Return the newly created {@link OrderEventDTO}
     */
    private OrderEventDTO createTestOrderEvent() {
        return OrderEventDTO.builder()
                .orderId(123L)
                .customerName("Test Customer")
                .eventType("CREATE")
                .build();
    }


    private CompletableFuture<SendResult<String, OrderEventDTO>> createSuccessfulFuture(OrderEventDTO eventDTO) {
        CompletableFuture<SendResult<String, OrderEventDTO>> future = new CompletableFuture<>();
        ProducerRecord<String, OrderEventDTO> record = new ProducerRecord<>(TEST_TOPIC, "123", eventDTO);
        RecordMetadata metadata = new RecordMetadata(new TopicPartition(TEST_TOPIC, 0), 0, 0,
                0, 0, 0);
        future.complete(new SendResult<>(record, metadata));
        return future;
    }
}
