package com.rinas.revenue.controller;

import com.rinas.revenue.common.web.PageResponse;
import com.rinas.revenue.domain.ProductStatus;
import com.rinas.revenue.domain.Role;
import com.rinas.revenue.dto.product.ProductRequest;
import com.rinas.revenue.dto.product.ProductResponse;
import com.rinas.revenue.security.AppUserPrincipal;
import com.rinas.revenue.security.SecurityUtils;
import com.rinas.revenue.service.ProductService;
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
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Sellable items owned by the caller's business")
public class ProductController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "List products for the caller's business")
    public PageResponse<ProductResponse> list(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.list(principal.getBusinessId(), status,
            PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), Sort.by("name")));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one product")
    public ProductResponse get(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        return productService.get(principal.getBusinessId(), id);
    }

    @PostMapping
    @Operation(summary = "Create a product")
    public ResponseEntity<ProductResponse> create(@AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody ProductRequest request) {
        SecurityUtils.requireRole(Role.OWNER, Role.ADMIN);
        ProductResponse created = productService.create(principal.getBusinessId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ProductResponse update(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request) {
        SecurityUtils.requireRole(Role.OWNER, Role.ADMIN);
        return productService.update(principal.getBusinessId(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a product (never a destructive delete)")
    public ResponseEntity<Void> deactivate(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        SecurityUtils.requireRole(Role.OWNER, Role.ADMIN);
        productService.deactivate(principal.getBusinessId(), id);
        return ResponseEntity.noContent().build();
    }
}
