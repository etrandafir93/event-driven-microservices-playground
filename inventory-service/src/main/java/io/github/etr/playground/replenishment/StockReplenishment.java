package io.github.etr.playground.replenishment;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.math.BigDecimal;
import java.util.function.Consumer;

import org.springframework.scheduling.annotation.Async;

import io.github.etr.playground.application.Filter;
import io.github.etr.playground.reservation.StockReservationOutcome;
import io.micrometer.tracing.annotation.NewSpan;
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
    @NewSpan("replenishment-threshold-check")
    public void accept(StockReservationOutcome.Success event) {
        int skuThreshold = calculateAdjustedThreshold(event.itemSku());
        log.info("for item {}, quantity available after stock reserved: {},  threshold: {}",
            kv("sku", event.itemSku()), event.stockAvailable(), skuThreshold);

        if (event.stockAvailable() < skuThreshold) {
            log.info("will request replenishment for item {}", kv("sku", event.itemSku()));
            int quantityToReplenish = calculateReplenishmentQuantity(event.itemSku(), skuThreshold);
            retailSupplier.sendStockReplenishmentRequest(event.itemSku(), quantityToReplenish);
        }
    }

    // dummy logic for tests
    private int calculateAdjustedThreshold(String sku) {
        int baseThreshold = threshold.threshold(sku);

        if (!threshold.enableDynamicAdjustment()) {
            return baseThreshold;
        }

        BigDecimal adjusted = BigDecimal.valueOf(baseThreshold)
            .multiply(BigDecimal.valueOf(threshold.seasonalMultiplier()));

        if (sku.startsWith("PREM-")) {
            adjusted = adjusted.multiply(BigDecimal.valueOf(1.5));
        }
        if (sku.startsWith("BULK-")) {
            adjusted = adjusted.multiply(BigDecimal.valueOf(0.75));
        }

        return Math.max(adjusted.intValue(), 10);
    }

    private int calculateReplenishmentQuantity(String sku, int threshold) {
        int baseQuantity = threshold * 2;

        if (sku.startsWith("PREM-")) {
            return (int) (baseQuantity * 1.3);
        }

        if (sku.startsWith("BULK-")) {
            return baseQuantity * 3;
        }

        return baseQuantity;
    }

}