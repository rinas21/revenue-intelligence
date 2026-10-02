package com.rinas.revenue.controller;

import com.rinas.revenue.domain.Role;
import com.rinas.revenue.dto.user.UserResponse;
import com.rinas.revenue.security.AppUserPrincipal;
import com.rinas.revenue.security.SecurityUtils;
import com.rinas.revenue.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Members of the caller's business")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List users in the caller's business")
    public List<UserResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        SecurityUtils.requireRole(Role.OWNER, Role.ADMIN);
        return userService.list(principal.getBusinessId());
    }

    @PostMapping
    @Operation(summary = "Add a user to the caller's business")
    public ResponseEntity<UserResponse> create(@AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody UserResponse.CreateRequest request) {
        SecurityUtils.requireRole(Role.OWNER, Role.ADMIN);
        UserResponse created = userService.create(principal.getBusinessId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a user from the caller's business")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        SecurityUtils.requireRole(Role.OWNER, Role.ADMIN);
        userService.remove(principal.getBusinessId(), id);
        return ResponseEntity.noContent().build();
    }
}
