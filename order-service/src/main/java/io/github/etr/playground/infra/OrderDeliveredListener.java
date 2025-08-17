package io.github.etr.playground.infra;

import java.time.Instant;
import java.util.Objects;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.etr.playground.application.inbox.Inbox;
import io.github.etr.playground.application.inbox.InboxMessageAdapter;
import io.github.etr.playground.domain.OrderDeliveredEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
class OrderDeliveredListener implements InboxMessageAdapter<OrderDeliveredEvent> {

    private static final String TOPIC = "order-delivered";

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
    public OrderDeliveredEvent adapt(String jsonPayload) {
        var dto = mapper.readValue(jsonPayload, OrderDeliveredKafkaMessage.class);
        return new OrderDeliveredEvent(dto.orderId, dto.username);
    }

    record OrderDeliveredKafkaMessage(String orderId, String username, String trackingNumber, String carrier, Instant deliveredAt) {
        OrderDeliveredKafkaMessage {
            Objects.requireNonNull(orderId);
            Objects.requireNonNull(username);
        }
    }

}
