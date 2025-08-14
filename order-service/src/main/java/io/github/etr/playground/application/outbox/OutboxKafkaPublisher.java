package io.github.etr.playground.application.outbox;

import static java.util.stream.Collectors.toMap;

import java.time.Instant;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import io.github.etr.playground.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class OutboxKafkaPublisher {

    private final Outbox outbox;
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publish(OutboxMessageReadyToPublish outboxMsg) {
        var msg = outbox.findByIdLocking(outboxMsg.id()).orElseThrow();
        log.info("Preparing to publish and update outbox message: {}", msg);

        var kafkaMsg = new ProducerRecord<>(msg.topic(), msg.key(), msg.payload());
        kafkaMsg.headers()
            .add("outboxId", msg.id().toString().getBytes())
            .add("eventType", msg.eventType().getBytes())
            .add("observedAt", msg.observedAt().toString().getBytes());

        stringKafkaTemplate.send(kafkaMsg).join();
        msg.publishedAt(Instant.now());

        log.info("Published outbox message with id: {}, topic: {}, key: {}; Updating the outbox db...",
            msg.id(), msg.topic(), msg.key());
        outbox.save(msg);
    }

    record OutboxMessageReadyToPublish(long id) {}


}
