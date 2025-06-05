package org.example.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka configuration class.
 * Creates topics programmatically if they don't exist.
 */
@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topic.order-events}")
    private String orderEventsTopic;

    /**
     * Creates the order events topic with 3 partitions and replication factor of 1.
     *
     * @return Returns a {@link NewTopic}
     */
    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(orderEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
