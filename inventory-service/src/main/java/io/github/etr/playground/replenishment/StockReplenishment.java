package io.github.etr.playground.replenishment;

import static net.logstash.logback.argument.StructuredArguments.kv;

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
        int skuThreshold = threshold.threshold(event.itemSku());
        log.info("for item {}, quantity available after stock reserved: {},  threshold: {}",
            kv("sku", event.itemSku()), event.stockAvailable(), skuThreshold);

        if (event.stockAvailable() < skuThreshold) {
            log.info("will request replenishment for item {}", kv("sku", event.itemSku()));
            int quantityToReplenish = skuThreshold * 2;
            retailSupplier.sendStockReplenishmentRequest(event.itemSku(), quantityToReplenish);
        }
    }

}