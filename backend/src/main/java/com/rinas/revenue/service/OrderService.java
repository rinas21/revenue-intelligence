package com.rinas.revenue.service;

import com.rinas.revenue.common.exception.BusinessRuleException;
import com.rinas.revenue.common.exception.ResourceNotFoundException;
import com.rinas.revenue.common.web.PageResponse;
import com.rinas.revenue.domain.Customer;
import com.rinas.revenue.domain.Order;
import com.rinas.revenue.domain.OrderStatus;
import com.rinas.revenue.domain.Product;
import com.rinas.revenue.domain.ProductStatus;
import com.rinas.revenue.dto.order.OrderRequest;
import com.rinas.revenue.dto.order.OrderResponse;
import com.rinas.revenue.dto.order.OrderSummaryResponse;
import com.rinas.revenue.event.OrderCancelledEvent;
import com.rinas.revenue.event.OrderCreatedEvent;
import com.rinas.revenue.event.OutboxService;
import com.rinas.revenue.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recording and reading sales.
 *
 * <p>The whole creation flow is one database transaction: the order, its items
 * and the outbox row commit together. Nothing is published to Kafka here — the
 * outbox publisher does that after the commit, so a broker outage can never
 * leave an order recorded with no event, or an event with no order.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final CustomerService customerService;
    private final BusinessService businessService;
    private final OutboxService outboxService;

    public OrderService(OrderRepository orderRepository, ProductService productService,
            CustomerService customerService, BusinessService businessService, OutboxService outboxService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.customerService = customerService;
        this.businessService = businessService;
        this.outboxService = outboxService;
    }

    @Transactional
    public OrderResponse create(UUID businessId, OrderRequest request) {
        // Idempotency: a retried request carrying the same externalRef returns
        // the original order instead of recording a second sale.
        if (request.externalRef() != null && !request.externalRef().isBlank()) {
            var existing = orderRepository.findByBusinessIdAndExternalRef(businessId, request.externalRef());
            if (existing.isPresent()) {
                return OrderResponse.from(existing.get());
            }
        }

        Customer customer = request.customerId() == null
            ? null
            : customerService.requireCustomer(businessId, request.customerId());

        Order order = new Order(businessService.requireBusiness(businessId), customer, request.orderDate());
        order.setSource(request.source() == null ? "MANUAL" : request.source());
        order.setExternalRef(request.externalRef());
        if (request.discountAmount() != null) {
            order.setDiscountAmount(request.discountAmount());
        }

        for (OrderRequest.OrderItemRequest line : request.items()) {
            Product product = productService.requireProduct(businessId, line.productId());
            if (product.getStatus() == ProductStatus.ARCHIVED) {
                throw new BusinessRuleException("Product '" + product.getName() + "' is archived and cannot be sold");
            }
            BigDecimal unitPrice = line.unitPrice() == null ? product.getPrice() : line.unitPrice();
            order.addItem(product, line.quantity(), unitPrice, line.discountAmount());
        }

        order.recalculateTotals();
        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Order total cannot be negative after discounts");
        }

        Order saved = orderRepository.save(order);

        List<OrderCreatedEvent.Item> items = saved.getItems().stream()
            .map(item -> new OrderCreatedEvent.Item(
                item.getProductId(), item.getQuantity(), item.getUnitPrice(),
                item.getDiscountAmount(), item.getLineTotal()))
            .toList();

        outboxService.append(OutboxService.AGGREGATE_ORDER, saved.getId(), "OrderCreated",
            OutboxService.orderCreated(saved.getId(), businessId,
                customer == null ? null : customer.getId(), saved.getOrderDate(), saved.getStatus().name(),
                saved.getSubtotal(), saved.getDiscountAmount(), saved.getTotalAmount(), items));

        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> list(UUID businessId, OrderStatus status, Pageable pageable) {
        var page = status == null
            ? orderRepository.findAllByBusinessId(businessId, pageable)
            : orderRepository.findAllByBusinessIdAndStatus(businessId, status, pageable);
        return PageResponse.from(page, OrderSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID businessId, UUID orderId) {
        return OrderResponse.from(requireOrder(businessId, orderId));
    }

    @Transactional
    public void cancel(UUID businessId, UUID orderId, String reason) {
        Order order = requireOrder(businessId, orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        outboxService.append(OutboxService.AGGREGATE_ORDER, order.getId(), "OrderCancelled",
            new OrderCancelledEvent(UUID.randomUUID(), order.getId(), businessId, Instant.now(), reason));
    }

    @Transactional(readOnly = true)
    public Order requireOrder(UUID businessId, UUID orderId) {
        return orderRepository.findByIdAndBusinessId(orderId, businessId)
            .orElseThrow(() -> ResourceNotFoundException.of("Order", orderId));
    }
}
