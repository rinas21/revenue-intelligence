package com.rinas.revenue.service;

import com.rinas.revenue.domain.Order;
import com.rinas.revenue.domain.OrderItem;
import com.rinas.revenue.dto.SaleRecordDTO;
import com.rinas.revenue.repository.OrderItemRepository;
import com.rinas.revenue.repository.OrderRepository;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        KafkaTemplate<String, String> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Order createOrder(UUID businessId, UUID customerId) {
        Order order = new Order(businessId, customerId);
        return orderRepository.save(order);
    }

    @Transactional
    public OrderItem addOrderItem(UUID orderId, UUID productId, Integer quantity, java.math.BigDecimal unitPrice, java.math.BigDecimal discountAmount) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be >= zero");
        }
        if (discountAmount == null || discountAmount.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount must be >= zero");
        }

        OrderItem item = new OrderItem(orderId, productId);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setDiscountAmount(discountAmount);

        BigDecimal lineTotal = unitPrice.multiply(java.math.BigDecimal.valueOf(quantity)).subtract(discountAmount);
        item.setLineTotal(lineTotal);

        OrderItem savedItem = orderItemRepository.save(item);
        
        // Publish OrderCreated event after the order item is saved
        publishOrderCreatedEvent(orderId, item.getProductId(), quantity, unitPrice, discountAmount);

        return savedItem;
    }

    public List<Order> listByBusinessId(UUID businessId) {
        return orderRepository.findByBusinessIdOrderByOrderDateDesc(businessId);
    }

    public Optional<Order> findById(UUID id) {
        return orderRepository.findByIdAndBusinessId(id, null);
    }

    @Transactional
    public void deleteOrder(UUID id) {
        orderRepository.deleteById(id);
    }

    private void publishOrderCreatedEvent(UUID orderId, UUID productId, Integer quantity, java.math.BigDecimal unitPrice, java.math.BigDecimal discountAmount) {
        if (kafkaTemplate == null) {
            return;
        }
        
        String topic = "revenue.order-created";
        String key = orderId.toString();
        
        String value = String.format(
            "{\"orderId\":\"%s\",\"productId\":\"%s\",\"quantity\":%d,\"unitPrice\":%d,\"discountAmount\":%d}",
            orderId, productId, quantity, unitPrice, discountAmount
        );
        
        // Use ProducerRecord for explicit control
        org.apache.kafka.clients.producer.ProducerRecord<String, String> record =
            new org.apache.kafka.clients.producer.ProducerRecord<>(topic, key, value);
        
        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.println("Failed to send Kafka event: " + ex.getMessage());
            }
        });
    }
}