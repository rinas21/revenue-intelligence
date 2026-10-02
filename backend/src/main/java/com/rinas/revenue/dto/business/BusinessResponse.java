package com.rinas.revenue.dto.business;

import com.rinas.revenue.domain.Business;
import java.time.Instant;
import java.util.UUID;

public record BusinessResponse(
    UUID id,
    String name,
    Instant createdAt
) {

    public static BusinessResponse from(Business business) {
        return new BusinessResponse(business.getId(), business.getName(), business.getCreatedAt());
    }
}
