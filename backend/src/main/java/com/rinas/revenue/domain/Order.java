package com.rinas.revenue.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * A sale recorded against one business. Monetary totals are always computed
 * server-side from the order's items; a client that posts totals has them
 * ignored rather than trusted.
 *
 * <p>{@code customer} is nullable: anonymous / walk-in sales are first-class.
 * {@code source} and {@code externalRef} make imported orders idempotent — an
 * import retried with the same {@code externalRef} resolves to the same order
 * instead of creating a duplicate sale.
 */
@Entity
@Table(schema = "app", name = "orders")
public class Order {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "order_date", nullable = false)
    private Instant orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status = OrderStatus.PAID;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 20)
    private String source;

    @Column(name = "external_ref", length = 255)
    private String externalRef;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Order() {
    }

    public Order(Business business, Customer customer, Instant orderDate) {
        this.id = UUID.randomUUID();
        this.business = business;
        this.customer = customer;
        this.orderDate = orderDate == null ? Instant.now() : orderDate;
    }

    /** Adds a line to the order. The line total is computed, never supplied by the caller. */
    public OrderItem addItem(Product product, int quantity, BigDecimal unitPrice, BigDecimal lineDiscount) {
        BigDecimal price = unitPrice == null ? product.getPrice() : unitPrice;
        BigDecimal discount = lineDiscount == null ? BigDecimal.ZERO : lineDiscount;
        OrderItem item = new OrderItem(this, product, quantity, price, discount);
        items.add(item);
        return item;
    }

    /** Recomputes subtotal and total from the current lines and order-level discount. */
    public void recalculateTotals() {
        this.subtotal = items.stream()
            .map(OrderItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = this.subtotal.subtract(this.discountAmount == null ? BigDecimal.ZERO : this.discountAmount);
    }

    public UUID getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public UUID getBusinessId() {
        return business == null ? null : business.getId();
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Instant orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(String externalRef) {
        this.externalRef = externalRef;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
