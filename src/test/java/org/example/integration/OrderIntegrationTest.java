package org.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.dto.OrderRequestDTO;
import org.example.entity.Order;
import org.example.enums.OrderStatus;
import org.example.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the complete order flow.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
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
        "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer"
})
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void testCreateOrderEndToEnd() throws Exception {
        // Given
        OrderRequestDTO requestDTO = new OrderRequestDTO(
                "Integration Test Customer",
                "Test Product",
                3,
                new BigDecimal("299.99")
        );

        // When - Create order via REST api
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))) // Important step for SERIALIZATION
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("Integration Test Customer"))
                .andExpect(jsonPath("$.orderStatus").value("PENDING"));

        // Then
        assertThat(orderRepository.count()).isEqualTo(1);

        Order savedOrder = orderRepository.findAll().get(0);
        assertThat(savedOrder.getCustomerName()).isEqualTo("Integration Test Customer");
        assertThat(savedOrder.getQuantity()).isEqualTo(3);
        assertThat(savedOrder.getPrice()).isEqualTo(new BigDecimal("299.99"));
        assertThat(savedOrder.getCreatedAt()).isNotNull();
    }

    @Test
    void testGetOrdersByStatus() throws Exception {
        // Given
        createAndSaveOrder("Customer1", OrderStatus.PENDING);
        createAndSaveOrder("Customer2", OrderStatus.PENDING);
        createAndSaveOrder("Customer3", OrderStatus.COMPLETED);

        // When
        mockMvc.perform(get("/api/orders/status/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderStatus").value("PENDING"))
                .andExpect(jsonPath("$[1].orderStatus").value("PENDING"));

        // When
        mockMvc.perform(get("/api/orders/status/completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderStatus").value("COMPLETED"));
    }

    private void createAndSaveOrder(String customerName, OrderStatus status) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setProductName("Test Product");
        order.setQuantity(1);
        order.setPrice(new BigDecimal("100.0"));
        order.setOrderStatus(status);
        orderRepository.save(order);
    }
}
