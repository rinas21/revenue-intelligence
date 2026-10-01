package com.rinas.revenue.repository;

import com.rinas.revenue.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderIdOrderByIdDesc(UUID orderId);

    List<OrderItem> findByProductIdAndBusinessId(UUID productId, UUID businessId);

    Optional<OrderItem> findByOrderIdAndProductId(UUID orderId, UUID productId);
}