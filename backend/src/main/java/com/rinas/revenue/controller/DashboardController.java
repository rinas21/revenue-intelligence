package com.rinas.revenue.controller;

import com.rinas.revenue.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/analytics")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, BigDecimal>> totalRevenue(@RequestParam(required = false) UUID businessId) {
        BigDecimal total = dashboardService.totalRevenue(businessId);
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        map.put("totalRevenue", total);
        return ResponseEntity.ok(map);
    }

    @GetMapping("/revenue/daily")
    public ResponseEntity<Map<LocalDate, BigDecimal>> dailyRevenue(
            @RequestParam(required = false) UUID businessId,
            @RequestParam(defaultValue = "30") int days) {
        Map<LocalDate, BigDecimal> daily = dashboardService.dailyRevenue(businessId, days);
        return ResponseEntity.ok(daily);
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity<Map<String, BigDecimal>> monthlyRevenue(@RequestParam(required = false) UUID businessId) {
        Map<String, BigDecimal> monthly = dashboardService.monthlyRevenue(businessId);
        return ResponseEntity.ok(monthly);
    }

    @GetMapping("/products/top")
    public ResponseEntity<List<Map<String, String>>> topProducts(
            @RequestParam(required = false) UUID businessId,
            @RequestParam(defaultValue = "5") int limit) {
        List<Map<String, String>> top = dashboardService.topProducts(businessId, limit);
        return ResponseEntity.ok(top);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<Map<String, String>>> customers(@RequestParam(required = false) UUID businessId) {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/growth")
    public ResponseEntity<Map<String, BigDecimal>> growth(@RequestParam(required = false) UUID businessId) {
        Map<String, BigDecimal> growth = new LinkedHashMap<>();
        Map<String, BigDecimal> monthly = dashboardService.monthlyRevenue(businessId);
        List<BigDecimal> values = new ArrayList<>(monthly.values());
        if (values.size() >= 2) {
            BigDecimal previous = values.get(0);
            BigDecimal current = values.get(1);
            if (previous.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal change = current.subtract(previous).multiply(BigDecimal.valueOf(100))
                        .divide(previous, 2, BigDecimal.ROUND_HALF_UP);
                growth.put("monthOverMonthChange", change);
            }
        }
        growth.put("monthlyRevenueCount", BigDecimal.valueOf(values.size()));
        return ResponseEntity.ok(growth);
    }

    @GetMapping("/insights")
    public ResponseEntity<List<Map<String, String>>> insights(@RequestParam(required = false) UUID businessId) {
        List<Map<String, String>> ins = dashboardService.recentInsights(businessId);
        return ResponseEntity.ok(ins);
    }
}
