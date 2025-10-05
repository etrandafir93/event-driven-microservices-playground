package io.github.etr.playground.domain.receipt;

import java.time.LocalDateTime;
import java.util.List;

public record Receipt(
    LocalDateTime createdAt,
    String orderId,
    String username,
    List<Item> items
) {

    public record Item(
        String sku,
        int quantity
    ) {
        public String format() {
            if (quantity == 1)
                return sku;
            return "%sx %s".formatted(quantity, sku);
        }
    }

}