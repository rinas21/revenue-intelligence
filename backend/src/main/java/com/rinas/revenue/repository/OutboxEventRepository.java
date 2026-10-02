package com.rinas.revenue.repository;

import com.rinas.revenue.domain.OutboxEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /** Oldest PENDING events first; the publisher drains these in order. */
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxEvent.Status status, Pageable pageable);

    long countByStatus(OutboxEvent.Status status);
}
