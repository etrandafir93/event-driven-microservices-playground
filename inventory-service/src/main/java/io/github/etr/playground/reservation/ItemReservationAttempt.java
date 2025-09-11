package io.github.etr.playground.reservation;

import java.util.function.Function;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.github.etr.playground.inventory.Inventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("itemReservationAttempt")
@RequiredArgsConstructor
class ItemReservationAttempt implements Function<ItemReservationAttempt.ItemOrderedEvent, Outcome> {

    private final Inventory inventory;

    @Override
    @Transactional
    public Outcome apply(ItemOrderedEvent event) {
        log.info("received 'item-ordered' event - {}", event);

        var itemOpt = inventory.findByItemSku(event.itemSku());
        if (itemOpt.isEmpty()) {
            log.warn("item {} not found", event.itemSku());
            return new Outcome.UnknownItem(event.orderId, event.itemSku, event.quantity);
        }
        var item = itemOpt.get();

        if (!item.hasAvailableStock(event.quantity)) {
            log.warn("item {} is out of stock", event.itemSku());
            return new Outcome.OutOfStock(event.orderId, event.itemSku, event.quantity);
        }

        item.reserveQuantity(event.quantity);
        inventory.save(item);
        log.info("item {} reserved for order {}", event.itemSku(), event.orderId());
        return new Outcome.Success(event.orderId, event.itemSku, event.quantity);
    }

    record ItemOrderedEvent(String orderId, String username, String itemSku, int quantity) {

    }
}