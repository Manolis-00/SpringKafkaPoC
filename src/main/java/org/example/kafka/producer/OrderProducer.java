package org.example.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.OrderEventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer service for sending order events.
 * Uses KafkaTemplate for sending messages asynchronously
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<String, OrderEventDTO> kafkaTemplate;

    @Value("${app.kafka.topic.order-events")
    private String orderEventsTopic;

    public void sendOrderEvent(OrderEventDTO orderEventDTO) {
        log.info("Sending order event: {}", orderEventDTO);

        // When using orderId as key, it ensures that all events for an order go to the same partition
        String key = String.valueOf(orderEventDTO.getOrderId());

        CompletableFuture<SendResult<String, OrderEventDTO>> future = kafkaTemplate.send(orderEventsTopic, key, orderEventDTO);

        // Handle success and failure callbacks
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent order event with key: {} to partition: {} at offset: {}",
                        key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send order event with key: {}", key, ex);
            }
        });
    }
}
