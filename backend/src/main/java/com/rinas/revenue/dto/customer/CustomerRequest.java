package com.rinas.revenue.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
    @NotBlank @Size(max = 255) String name,
    // Optional: anonymous / walk-in customers have no email.
    @Email @Size(max = 255) String email,
    @Size(max = 50) String phone
) {
}
