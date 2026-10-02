package com.rinas.revenue.controller;

import com.rinas.revenue.common.web.PageResponse;
import com.rinas.revenue.domain.Role;
import com.rinas.revenue.dto.customer.CustomerRequest;
import com.rinas.revenue.dto.customer.CustomerResponse;
import com.rinas.revenue.security.AppUserPrincipal;
import com.rinas.revenue.security.SecurityUtils;
import com.rinas.revenue.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Customers owned by the caller's business")
public class CustomerController {

    private static final int MAX_PAGE_SIZE = 100;

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @Operation(summary = "List customers for the caller's business")
    public PageResponse<CustomerResponse> list(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return customerService.list(principal.getBusinessId(),
            PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), Sort.by("name")));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one customer")
    public CustomerResponse get(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        return customerService.get(principal.getBusinessId(), id);
    }

    @PostMapping
    @Operation(summary = "Create a customer")
    public ResponseEntity<CustomerResponse> create(@AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse created = customerService.create(principal.getBusinessId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a customer")
    public CustomerResponse update(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id,
            @Valid @RequestBody CustomerRequest request) {
        return customerService.update(principal.getBusinessId(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a customer")
    public ResponseEntity<Void> deactivate(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        SecurityUtils.requireRole(Role.OWNER, Role.ADMIN);
        customerService.deactivate(principal.getBusinessId(), id);
        return ResponseEntity.noContent().build();
    }
}
