package com.rinas.revenue.repository;

import com.rinas.revenue.domain.ConsumedEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumedEventRepository extends JpaRepository<ConsumedEvent, UUID> {

    boolean existsByEventIdAndConsumer(UUID eventId, String consumer);
}
