package com.rinas.revenue.repository;

import com.rinas.revenue.domain.OrderItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @EntityGraph(attributePaths = {"product"})
    List<OrderItem> findAllByOrderId(UUID orderId);
}
