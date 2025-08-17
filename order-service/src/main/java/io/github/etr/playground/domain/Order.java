package io.github.etr.playground.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", unique = true, nullable = false)
    private String orderId;

    @Embedded
    private Customer customer;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ToString.Exclude
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    public Order(Customer customer, Map<Product, Integer> products) {
        this.customer = customer;
        this.orderId = UUID.randomUUID()
            .toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        this.orderItems = products.entrySet()
            .stream()
            .map(entry -> new OrderItem(this, entry.getKey(), entry.getValue()))
            .toList();
    }

    public String customerUsername() {
        return customer.username();
    }

    public enum Status {
        PENDING("Order received and pending processing"),
        PROCESSING("Order is being processed"),
        STOCK_RESERVED("Stock has been reserved for the order"),
        STOCK_UNAVAILABLE("Required stock is not available"),
        SHIPPED("Order has been shipped"),
        DELIVERED("Order has been delivered"),
        CANCELLED("Order has been cancelled");

        @Getter
        private final String description;
        Status(String description) {
            this.description = description;
        }
    }

    public BigDecimal totalValue() {
        return orderItems.stream()
            .map(OrderItem::totalValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void shipped() {
        boolean illegalState = status == Status.STOCK_UNAVAILABLE
            || status == Status.CANCELLED || status == Status.DELIVERED;
        if (illegalState)
            throw new IllegalStateException(("the order %s has status='%s' " +
                "and cannot transition to status='SHIPPED'!").formatted(orderId, status));

        if (status == Status.SHIPPED) {
            log.info("the order %s was already it is in 'SHIPPED' status already.");
            return;
        }

        status = Status.SHIPPED;
        updatedAt = LocalDateTime.now();
    }

    public void delivered() {
        if (status == Status.DELIVERED) {
            log.info("the order %s was already it is in 'DELIVERED' status already.");
            return;
        }

        if (status != Status.SHIPPED)
            throw new IllegalStateException(("the order %s has status='%s' " +
                "and cannot transition to status='SHIPPED'!").formatted(orderId, status));

        status = Status.DELIVERED;
        updatedAt = LocalDateTime.now();
    }
}
