package org.example.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Kafka producer service for sending order events.
 * Uses KafkaTemplate for sending messages asynchronously
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {
}
