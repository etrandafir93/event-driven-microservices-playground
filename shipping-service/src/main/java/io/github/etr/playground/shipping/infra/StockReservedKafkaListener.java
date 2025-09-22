package io.github.etr.playground.shipping.infra;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import io.github.etr.playground.shipping.domain.OrderShipmentsCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class StockReservedKafkaListener {

    private final OrderShipmentsCommandHandler commandHandler;

    @KafkaListener(topics = "stock-reserved", containerFactory = "customKafkaListenerContainerFactory")
    public void handleStockReserved(StockReserved message) {
        log.info("Received stock reserved event: {}", message);
        commandHandler.createShipment(message.orderId(), message.username());
    }

    record StockReserved(String orderId, String username, String itemSku) {

    }
}
