package com.rinas.revenue.dto.customer;

import com.rinas.revenue.domain.Customer;
import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
    UUID id,
    String name,
    String email,
    String phone,
    Instant createdAt,
    Instant updatedAt
) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
            customer.getId(),
            customer.getName(),
            customer.getEmail(),
            customer.getPhone(),
            customer.getCreatedAt(),
            customer.getUpdatedAt());
    }
}
