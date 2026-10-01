package com.rinas.revenue.repository;

import com.rinas.revenue.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByBusinessIdOrderByOrderDateDesc(UUID businessId);

    List<Order> findByBusinessIdAndStatus(UUID businessId, String status);

    List<Order> findByCustomerIdOrderByOrderDateDesc(UUID customerId);

    Optional<Order> findByBusinessIdAndId(UUID businessId, UUID orderId);

    Optional<Order> findByIdAndBusinessId(UUID orderId, UUID businessId);
}