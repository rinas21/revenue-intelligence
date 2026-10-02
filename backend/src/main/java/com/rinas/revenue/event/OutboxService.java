package com.rinas.revenue.event;

import com.rinas.revenue.domain.OutboxEvent;
import com.rinas.revenue.repository.OutboxEventRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * Writes domain events to the transactional outbox.
 *
 * <p>This is always called inside the caller's transaction
 * ({@link Propagation#MANDATORY}), so the event row and the aggregate change
 * commit or roll back together. A separate publisher moves rows to Kafka. This
 * is what removes the "saved the order, crashed before publishing" gap.
 */
@Service
public class OutboxService {

    public static final String AGGREGATE_ORDER = "Order";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void append(String aggregateType, UUID aggregateId, String eventType, Object event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxEventRepository.save(new OutboxEvent(aggregateType, aggregateId, eventType, payload));
    }

    public static OrderCreatedEvent orderCreated(UUID orderId, UUID businessId, UUID customerId, Instant orderDate,
            String status, BigDecimal subtotal, BigDecimal discountAmount, BigDecimal totalAmount,
            List<OrderCreatedEvent.Item> items) {
        return new OrderCreatedEvent(
            UUID.randomUUID(), orderId, businessId, customerId, orderDate, status,
            subtotal, discountAmount, totalAmount, items, Instant.now());
    }
}
