package io.github.etr.playground.inventory;

import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("itemOrderedListener")
class ItemOrderedListener implements Consumer<ItemOrderedListener.ItemOrderedEvent> {

    @Override
    public void accept(ItemOrderedEvent event) {
        log.info("📦 ITEM ORDERED - Processing item order! {}", event);
    }

    record ItemOrderedEvent(String orderId, String username, String productSku, int quantity) {
    }
}