package io.github.etr.playground.domain;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "orderId")
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
        this.status = Status.PENDING;
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
}
