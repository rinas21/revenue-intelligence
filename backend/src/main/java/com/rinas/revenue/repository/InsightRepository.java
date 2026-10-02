package com.rinas.revenue.repository;

import com.rinas.revenue.domain.Insight;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsightRepository extends JpaRepository<Insight, UUID> {

    Optional<Insight> findByIdAndBusinessId(UUID id, UUID businessId);

    Page<Insight> findAllByBusinessId(UUID businessId, Pageable pageable);

    boolean existsByBusinessIdAndDedupeKey(UUID businessId, String dedupeKey);
}
