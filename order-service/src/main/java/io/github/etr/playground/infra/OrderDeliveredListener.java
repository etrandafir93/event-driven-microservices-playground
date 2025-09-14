package io.github.etr.playground.infra;

import static io.github.etr.playground.infra.KafkaHeaderUtils.idempotencyKey;
import static io.github.etr.playground.infra.KafkaHeaderUtils.observedAt;
import static java.util.Objects.requireNonNull;

import java.time.Instant;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.etr.playground.application.annotations.Adapter;
import io.github.etr.playground.domain.order.OrderDeliveredEvent;
import io.github.etr.playground.infra.inbox.Inbox;
import io.github.etr.playground.infra.inbox.InboxMessageAdapter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter
@RequiredArgsConstructor
class OrderDeliveredListener implements InboxMessageAdapter<OrderDeliveredEvent> {

    private static final String TOPIC = "order-delivered";

    private final Inbox inbox;
    private final ObjectMapper mapper;

    @KafkaListener(topics = TOPIC)
    void onOrderDelivered(ConsumerRecord<String, String> message) {
        log.info("received message on topic {}, key {}", TOPIC, message.key());
        inbox.incomingMessage(TOPIC, message.key(), message.value(), idempotencyKey(message), observedAt(message));
    }

    @Override
    public String topic() {
        return TOPIC;
    }

    @SneakyThrows
    @Override
    public OrderDeliveredEvent domainEvent(String msgPayload) {
        var dto = mapper.readValue(msgPayload, OrderDeliveredKafkaMessage.class);
        return new OrderDeliveredEvent(dto.orderId, dto.username);
    }

    record OrderDeliveredKafkaMessage(String orderId, String username, String trackingNumber, String carrier, Instant deliveredAt) {

        OrderDeliveredKafkaMessage {
            requireNonNull(orderId);
            requireNonNull(username);
        }
    }

}
