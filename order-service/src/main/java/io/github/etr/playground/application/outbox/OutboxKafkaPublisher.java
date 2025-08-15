package io.github.etr.playground.application.outbox;

import java.time.Instant;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class OutboxKafkaPublisher {

    private final Outbox outbox;
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    @EventListener
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
