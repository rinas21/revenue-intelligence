package com.rinas.revenue.controller;

import com.rinas.revenue.domain.Order;
import com.rinas.revenue.dto.SaleRecordDTO;
import com.rinas.revenue.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

import com.rinas.revenue.domain.OrderItem;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrdersController {

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody SaleRecordDTO saleRecord) {
        if (saleRecord.getCustomerId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (saleRecord.getProductId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (saleRecord.getQuantity() == null || saleRecord.getQuantity() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Order order = orderService.createOrder(
                // In a real system, businessId would come from authentication context
                // For now, use a default/demo business ID
                UUID.randomUUID(),
                saleRecord.getCustomerId()
        );

        OrderItem addedItem = orderService.addOrderItem(
                order.getId(),
                saleRecord.getProductId(),
                saleRecord.getQuantity(),
                saleRecord.getPrice(),
                saleRecord.getDiscount()
        );

        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Order> listByBusinessId(@RequestParam(required = false) UUID businessId) {
        if (businessId == null) {
            return java.util.Collections.emptyList();
        }
        return orderService.listByBusinessId(businessId);
    }
}