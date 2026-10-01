package com.rinas.revenue.service;

import com.rinas.revenue.domain.Order;
import com.rinas.revenue.domain.OrderItem;
import com.rinas.revenue.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public DashboardService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /** Total revenue across all businesses (or filtered by businessId). */
    @Transactional(readOnly = true)
    public BigDecimal totalRevenue(UUID businessId) {
        return orderRepository.findByBusinessIdOrderByOrderDateDesc(businessId).stream()
            .map(o -> o.getTotalAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Total order count across all businesses (or filtered by businessId). */
    @Transactional(readOnly = true)
    public long orderCount(UUID businessId) {
        return orderRepository.count();
    }

    /** Average order value. */
    @Transactional(readOnly = true)
    public BigDecimal averageOrderValue(UUID businessId) {
        long count = orderCount(businessId);
        if (count == 0) return BigDecimal.ZERO;
        return totalRevenue(businessId).divide(BigDecimal.valueOf(count), 2, BigDecimal.ROUND_HALF_UP);
    }

    /** Daily revenue for the last n days. */
    @Transactional(readOnly = true)
    public Map<LocalDate, BigDecimal> dailyRevenue(UUID businessId, int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);
        Map<LocalDate, BigDecimal> map = new LinkedHashMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            map.put(d, BigDecimal.ZERO);
        }
        // Get orders within the date range
        Instant startInstant = start.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = end.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        List<Order> orders = orderRepository.findByBusinessIdOrderByOrderDateDesc(businessId).stream()
            .filter(o -> !o.getOrderDate().isBefore(startInstant) && !o.getOrderDate().isAfter(endInstant))
            .collect(Collectors.toList());
        for (Order o : orders) {
            LocalDate day = o.getOrderDate().atZone(ZoneId.systemDefault()).toLocalDate();
            map.merge(day, o.getTotalAmount(), BigDecimal::add);
        }
        return map;
    }

    /** Monthly revenue. */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> monthlyRevenue(UUID businessId) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        List<Order> orders = orderRepository.findByBusinessIdOrderByOrderDateDesc(businessId);
        for (Order o : orders) {
            String month = o.getOrderDate().atZone(ZoneId.systemDefault()).toLocalDate()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
            map.merge(month, o.getTotalAmount(), BigDecimal::add);
        }
        return map;
    }

    /** Top products by revenue. */
    @Transactional(readOnly = true)
    public List<Map<String, String>> topProducts(UUID businessId, int limit) {
        List<OrderItem> allItems = orderItemRepository.findByProductIdAndBusinessId(businessId, businessId);
        allItems.sort((a, b) -> b.getLineTotal().compareTo(a.getLineTotal()));
        if (limit > 0 && limit < allItems.size()) {
            allItems = allItems.subList(0, limit);
        }
        return allItems.stream()
            .map(item -> {
                Map<String, String> m = new LinkedHashMap<>();
                m.put("productName", item.getProduct().getName());
                m.put("revenue", item.getLineTotal().toString());
                return m;
            })
            .collect(Collectors.toList());
    }

    /** Recent insights (simple deterministic rules). */
    @Transactional(readOnly = true)
    public List<Map<String, String>> recentInsights(UUID businessId) {
        List<Map<String, String>> insights = new ArrayList<>();
        BigDecimal revenue = totalRevenue(businessId);
        long count = orderCount(businessId);
        BigDecimal aov = averageOrderValue(businessId);

        if (count > 0) {
            insights.add(Map.of(
                "type", "revenue",
                "title", "Total Revenue",
                "description", String.format("Current: LKR %,.0f", revenue),
                "metric", "revenue",
                "metric_value", revenue.toString(),
                "baseline_value", "100000",
                "period_start", "Today",
                "period_end", "Today"
            ));
        }

        if (aov.compareTo(BigDecimal.valueOf(5000)) > 0) {
            insights.add(Map.of(
                "type", "aov_change",
                "title", "Average Order Value Change",
                "description", String.format("AOV: LKR %,.0f", aov),
                "metric", "average_order_value",
                "metric_value", aov.toString(),
                "baseline_value", "5000",
                "period_start", "Today",
                "period_end", "Today"
            ));
        }

        return insights;
    }
}