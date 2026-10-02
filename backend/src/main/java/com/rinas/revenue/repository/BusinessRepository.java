package com.rinas.revenue.repository;

import com.rinas.revenue.domain.Business;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    Optional<Business> findByName(String name);

    boolean existsByName(String name);
}
