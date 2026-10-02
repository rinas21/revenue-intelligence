package com.rinas.revenue.repository;

import com.rinas.revenue.domain.Product;
import com.rinas.revenue.domain.ProductStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * The safe read for a business-owned product. {@code findById} alone is
     * deliberately not exposed here: it would let one business read another's
     * row if a caller ever passed an id from a request.
     */
    Optional<Product> findByIdAndBusinessId(UUID id, UUID businessId);

    Page<Product> findAllByBusinessId(UUID businessId, Pageable pageable);

    Page<Product> findAllByBusinessIdAndStatus(UUID businessId, ProductStatus status, Pageable pageable);

    Optional<Product> findByBusinessIdAndNameIgnoreCase(UUID businessId, String name);

    boolean existsByBusinessIdAndNameIgnoreCase(UUID businessId, String name);
}
