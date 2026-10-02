package com.rinas.revenue.dto.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BusinessUpdateRequest(
    @NotBlank @Size(max = 255) String name
) {
}
