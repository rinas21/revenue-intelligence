package com.rinas.revenue.controller;

import com.rinas.revenue.domain.Role;
import com.rinas.revenue.dto.business.BusinessResponse;
import com.rinas.revenue.dto.business.BusinessUpdateRequest;
import com.rinas.revenue.security.AppUserPrincipal;
import com.rinas.revenue.security.SecurityUtils;
import com.rinas.revenue.service.BusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/business")
@Tag(name = "Business", description = "The caller's own business profile")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping
    @Operation(summary = "Get the caller's business")
    public BusinessResponse get(@AuthenticationPrincipal AppUserPrincipal principal) {
        return businessService.get(principal.getBusinessId());
    }

    @PutMapping
    @Operation(summary = "Rename the caller's business")
    public BusinessResponse update(@AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody BusinessUpdateRequest request) {
        SecurityUtils.requireRole(Role.OWNER, Role.ADMIN);
        return businessService.rename(principal.getBusinessId(), request.name());
    }
}
