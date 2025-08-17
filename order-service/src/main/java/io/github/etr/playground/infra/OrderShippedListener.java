package io.github.etr.playground.infra;

import java.time.Instant;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.etr.playground.application.inbox.Inbox;
import io.github.etr.playground.application.inbox.InboxMessageAdapter;
import io.github.etr.playground.domain.OrderShippedEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
class OrderShippedListener implements InboxMessageAdapter<OrderShippedEvent> {

    private static final String TOPIC = "order-shipped";

    private final Inbox inbox;
    private final ObjectMapper mapper;

    @KafkaListener(topics = TOPIC)
    void onOrderShipped(ConsumerRecord<String, String> message) {
        inbox.incomingMessage(TOPIC, message.key(), message.value());
    }

    @Override
    public String topic() {
        return TOPIC;
    }

    @SneakyThrows
    @Override
    public OrderShippedEvent adapt(String jsonPayload) {
        var dto = mapper.readValue(jsonPayload, OrderShippedKafkaMessage.class);
        return new OrderShippedEvent(dto.orderId, dto.username);
    }

    record OrderShippedKafkaMessage(String orderId, String username, String trackingNumber, String carrier, Instant shippedAt, Instant estimatedDelivery) {
    }

}
