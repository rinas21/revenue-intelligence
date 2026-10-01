package com.rinas.revenue.repository;

import com.rinas.revenue.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByBusinessIdAndEmail(UUID businessId, String email);

    Optional<Customer> findByBusinessId(UUID businessId);
}