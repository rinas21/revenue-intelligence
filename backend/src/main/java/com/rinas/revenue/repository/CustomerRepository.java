package com.rinas.revenue.repository;

import com.rinas.revenue.domain.Customer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByIdAndBusinessId(UUID id, UUID businessId);

    Page<Customer> findAllByBusinessId(UUID businessId, Pageable pageable);

    Optional<Customer> findByBusinessIdAndEmailIgnoreCase(UUID businessId, String email);

    boolean existsByBusinessIdAndEmailIgnoreCase(UUID businessId, String email);
}
