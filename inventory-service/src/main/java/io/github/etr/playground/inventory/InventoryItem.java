package io.github.etr.playground.inventory;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "inventory_items")
@NoArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String itemSku;
    private Integer quantity;
    private Integer reservedQuantity = 0;

    public InventoryItem(String itemSku, int quantity) {
        this.itemSku = itemSku;
        this.quantity = quantity;
    }

    public boolean hasAvailableStock(Integer requestedQuantity) {
        return (quantity - reservedQuantity) >= requestedQuantity;
    }

    public void reserveQuantity(Integer quantityToReserve) {
        if (hasAvailableStock(quantityToReserve)) {
            this.reservedQuantity += quantityToReserve;
        } else {
            throw new IllegalStateException("Insufficient stock available");
        }
    }

    public void reduceQuantity(Integer quantityToReduce) {
        this.quantity -= quantityToReduce;
        this.reservedQuantity -= quantityToReduce;
    }
}