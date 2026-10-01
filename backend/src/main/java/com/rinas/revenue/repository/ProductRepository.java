package com.rinas.revenue.repository;

import com.rinas.revenue.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByBusinessIdAndName(UUID businessId, String name);

    Optional<Product> findByBusinessId(UUID businessId);
}