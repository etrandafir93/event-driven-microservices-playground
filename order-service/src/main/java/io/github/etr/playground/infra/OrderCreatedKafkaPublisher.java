package io.github.etr.playground.infra;

import static java.util.stream.Collectors.toMap;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import io.github.etr.playground.application.Port;
import io.github.etr.playground.domain.Order;
import io.github.etr.playground.domain.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class OrderCreatedKafkaPublisher {

    private static final String TOPIC = "order-created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    @TransactionalEventListener(OrderCreatedEvent.class)
    public void publish(OrderCreatedEvent event) {
        OrderCreatedKafkaMessage msg = OrderCreatedKafkaMessage.from(event.order());
        kafkaTemplate.send(TOPIC, msg.customerUsername, msg);
    }

    public record OrderCreatedKafkaMessage(String orderId, String customerUsername, Map<String, Integer> order) {
        static OrderCreatedKafkaMessage from(Order order) {
            return new OrderCreatedKafkaMessage(
                order.orderId(),
                order.customer().username(),
                order.orderItems()
                    .stream()
                    .collect(toMap(
                        item -> item.product().sku(),
                        item -> item.quantity()))
            );
        }
    }

}
