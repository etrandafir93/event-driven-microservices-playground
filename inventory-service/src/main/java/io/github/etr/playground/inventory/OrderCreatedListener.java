package io.github.etr.playground.inventory;

import static org.springframework.messaging.support.MessageBuilder.createMessage;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import io.github.etr.playground.inventory.ItemOrderedListener.ItemOrderedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("orderCreatedListener")
class OrderCreatedListener implements Function<OrderCreatedListener.OrderCreatedEvent, List<Message<ItemOrderedEvent>>> {

    @Override
    public List<Message<ItemOrderedEvent>> apply(OrderCreatedEvent orderEvent) {
        log.info("order-created message received! {}", orderEvent);

        var orderedItems = orderEvent.order.entrySet()
            .stream()
            .map(it -> new ItemOrderedEvent(
                orderEvent.orderId,
                orderEvent.username,
                it.getKey(),
                it.getValue()
            ))
            .toList();

        return orderedItems.stream()
            .map(evt -> createMessage(evt, msgKey(evt.productSku())))
            .toList();
    }

    private static MessageHeaders msgKey(String key) {
        return new MessageHeaders(Map.of(KafkaHeaders.KEY, key.getBytes()));
    }

    record OrderCreatedEvent(String orderId, String username, Map<String, Integer> order) {

    }
}