package com.rinas.revenue.repository;

import com.rinas.revenue.domain.ImportJob;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportJobRepository extends JpaRepository<ImportJob, UUID> {

    Optional<ImportJob> findByIdAndBusinessId(UUID id, UUID businessId);

    Page<ImportJob> findAllByBusinessId(UUID businessId, Pageable pageable);
}
