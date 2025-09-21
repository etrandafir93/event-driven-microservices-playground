package io.github.etr.playground.reservation;

import java.util.function.Function;

import org.springframework.transaction.annotation.Transactional;

import io.github.etr.playground.application.Filter;
import io.github.etr.playground.inventory.Inventory;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Filter("itemReservationAttempt")
@RequiredArgsConstructor
class StockReservation implements Function<StockReservation.ItemOrderedEvent, StockReservationOutcome> {

    private final Inventory inventory;

    @Override
    @Transactional
    @NewSpan("reservation-stock-check")
    public StockReservationOutcome apply(ItemOrderedEvent event) {
        log.info("received 'item-ordered' event - {}", event);

        var stockOpt = inventory.findByItemSku(event.itemSku());
        if (stockOpt.isEmpty()) {
            log.warn("item {} not found", event.itemSku());
            return new StockReservationOutcome.UnknownItem(event.orderId, event.username, event.itemSku, event.quantity);
        }
        var stock = stockOpt.get();

        if (!stock.hasAvailableStock(event.quantity)) {
            log.warn("item {} is out of stock", event.itemSku());
            return new StockReservationOutcome.OutOfStock(event.orderId, event.username, event.itemSku, event.quantity);
        }
        stock.reserveQuantity(event.quantity);
        inventory.save(stock);

        log.info("item {} reserved for order {}", event.itemSku(), event.orderId());
        return new StockReservationOutcome.Success(event.orderId, event.username, event.itemSku, event.quantity, stock.quantity());
    }

    record ItemOrderedEvent(String orderId, String username, String itemSku, int quantity) {

    }
}