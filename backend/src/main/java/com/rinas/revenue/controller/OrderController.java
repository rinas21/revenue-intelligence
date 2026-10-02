package com.rinas.revenue.controller;

import com.rinas.revenue.common.web.PageResponse;
import com.rinas.revenue.domain.OrderStatus;
import com.rinas.revenue.domain.Role;
import com.rinas.revenue.dto.order.OrderRequest;
import com.rinas.revenue.dto.order.OrderResponse;
import com.rinas.revenue.dto.order.OrderSummaryResponse;
import com.rinas.revenue.security.AppUserPrincipal;
import com.rinas.revenue.security.SecurityUtils;
import com.rinas.revenue.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Sales recorded for the caller's business")
public class OrderController {

    private static final int MAX_PAGE_SIZE = 100;

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "List orders for the caller's business")
    public PageResponse<OrderSummaryResponse> list(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return orderService.list(principal.getBusinessId(), status,
            PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "orderDate")));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one order with its items")
    public OrderResponse get(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        return orderService.get(principal.getBusinessId(), id);
    }

    @PostMapping
    @Operation(summary = "Record a sale")
    public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse created = orderService.create(principal.getBusinessId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id,
            @RequestParam(required = false) String reason) {
        SecurityUtils.requireRole(Role.OWNER, Role.ADMIN);
        orderService.cancel(principal.getBusinessId(), id, reason);
        return ResponseEntity.noContent().build();
    }
}
