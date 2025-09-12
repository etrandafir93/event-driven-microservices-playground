package io.github.etr.playground.replenishment;

import java.util.function.Consumer;

import org.springframework.scheduling.annotation.Async;

import io.github.etr.playground.application.Filter;
import io.github.etr.playground.reservation.StockReservationOutcome;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Filter("stockReplenishment")
class StockReplenishment implements Consumer<StockReservationOutcome.Success> {

    private final ReplenishmentThreshold threshold;
    private final RetailSupplierClient retailSupplier;

    @Async
    @Override
    public void accept(StockReservationOutcome.Success event) {
        log.info("Quantity Reserved: {}", event.stockRequested());
        log.info("After Reserved: {}", event.stockAvailable());
        log.info("=== END REPLENISHMENT LOG ===");

        boolean replenishmentNeeded = event.stockAvailable() <= threshold.threshold(event.itemSku());
        if (!replenishmentNeeded) {
            return; // filter out
        }

        log.info("replenishment needed for sku={}", event.itemSku());
        retailSupplier.sendStockReplenishmentRequest(event.itemSku(), event.stockRequested());
    }

}