package io.github.etr.playground.domain;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("Order received and pending processing"),
    PROCESSING("Order is being processed"),
    STOCK_RESERVED("Stock has been reserved for the order"),
    STOCK_UNAVAILABLE("Required stock is not available"),
    SHIPPED("Order has been shipped"),
    DELIVERED("Order has been delivered"),
    CANCELLED("Order has been cancelled");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name() + ": " + description;
    }
}
