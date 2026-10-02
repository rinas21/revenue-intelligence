package com.rinas.revenue.repository;

import com.rinas.revenue.domain.Order;
import com.rinas.revenue.domain.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = {"customer", "items", "items.product"})
    Optional<Order> findByIdAndBusinessId(UUID id, UUID businessId);

    @EntityGraph(attributePaths = {"customer"})
    Page<Order> findAllByBusinessId(UUID businessId, Pageable pageable);

    @EntityGraph(attributePaths = {"customer"})
    Page<Order> findAllByBusinessIdAndStatus(UUID businessId, OrderStatus status, Pageable pageable);

    Optional<Order> findByBusinessIdAndExternalRef(UUID businessId, String externalRef);

    @EntityGraph(attributePaths = {"customer", "items", "items.product"})
    List<Order> findAllByBusinessIdAndOrderDateBetween(UUID businessId, Instant from, Instant to);

    long countByBusinessIdAndStatus(UUID businessId, OrderStatus status);

    List<Order> findByBusinessIdOrderByOrderDateDesc(UUID businessId);

    long countByBusinessId(UUID businessId);
}