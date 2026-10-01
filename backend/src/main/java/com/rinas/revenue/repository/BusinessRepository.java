package com.rinas.revenue.repository;

import com.rinas.revenue.domain.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {

    Optional<Business> findByBusinessId(UUID businessId);

    Optional<Business> findByName(String name);
}