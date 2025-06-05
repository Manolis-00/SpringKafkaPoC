package org.example.integration;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.dto.OrderEventDTO;
import org.example.dto.OrderRequestDTO;
import org.example.entity.Order;
import org.example.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kafka integration tests using Kafka
 * Tests the complete message flow from producer to consumer
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(
        partitions = 3,
        topics = "${app.kafka.topic.order-events}",
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092",
                "port=9092"
        }
)
@TestPropertySource(properties = {
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
        "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
        "spring.kafka.consumer.properties.spring.json.trusted.packages=*"
})
public class KafkaIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Value("${app.kafka.topic.order-events}")
    private String orderEventsTopic;

    @Test
    void testKafkaMessageFlow() throws Exception {
        // Given
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "test-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.TYPE_MAPPINGS, "orderEventDTO:org.example.dto.OrderEventDTO");

        DefaultKafkaConsumerFactory<String, OrderEventDTO> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(orderEventsTopic);
        KafkaMessageListenerContainer<String, OrderEventDTO> container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        BlockingDeque<ConsumerRecord<String, OrderEventDTO>> records = new LinkedBlockingDeque<>();
        container.setupMessageListener((MessageListener<String, OrderEventDTO>) records::add);
        container.start();

        // Wait for container to be ready
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        // When - create an order
        OrderRequestDTO requestDTO = new OrderRequestDTO(
                "Kafka Test Customer",
                "Test Product",
                1,
                new BigDecimal("99.99")
        );
        Order createOrder = orderService.createOrder(requestDTO);

        // Then - verify that the event was published and consumed
        ConsumerRecord<String, OrderEventDTO> received = records.poll(10, TimeUnit.SECONDS);

        assertThat(received).isNotNull();
        assertThat(received.key()).isEqualTo(createOrder.getId().toString());

        OrderEventDTO receivedEvent = received.value();
        assertThat(receivedEvent.getOrderId()).isEqualTo(createOrder.getId());
        assertThat(receivedEvent.getCustomerName()).isEqualTo("Kafka Test Customer");
        assertThat(receivedEvent.getEventType()).isEqualTo("CREATE");

        // Cleanup
        container.stop();
    }
}
