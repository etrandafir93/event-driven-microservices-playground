package io.github.etr.playground.shipping.infra;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.etr.playground.shipping.domain.OrderShipmentsCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReservedKafkaListener {

    private final OrderShipmentsCommandHandler commandHandler;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "stock-reserved")
    public void handleStockReserved(String message) {
        try {
            log.info("Received stock reserved event: {}", message);
            StockReservedEvent event = objectMapper.readValue(message, StockReservedEvent.class);
            commandHandler.createShipment(event.orderId(), event.username());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse stock reserved event: {}", message, e);
        }
    }

    public record StockReservedEvent(String orderId, String username) {
    }
}
