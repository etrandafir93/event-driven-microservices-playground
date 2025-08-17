package io.github.etr.playground.application.outbox;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxKafkaPublisher {

    private final Outbox outbox;
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    @Transactional
    public void publishMsgAndUpdateStatus(Long outboxMsgId) {
        outbox.findByIdLocking(outboxMsgId)
            .ifPresentOrElse(
                this::publishAndUpdate,
                () -> log.info("couldn't fetch outboxMsgId={}, it'll be processed by other job", outboxMsgId)
            );
    }

    private void publishAndUpdate(OutboxMessage msg) {
        log.info("Preparing to publish and update outbox message: {}", msg);

        var kafkaMsg = new ProducerRecord<>(msg.topic(), msg.key(), msg.payload());
        kafkaMsg.headers()
            .add("outboxId", msg.id().toString().getBytes())
            .add("eventType", msg.eventType().getBytes())
            .add("observedAt", msg.observedAt().toString().getBytes());

        boolean sent = stringKafkaTemplate.send(kafkaMsg)
            .orTimeout(3, TimeUnit.SECONDS)
            .thenApply(res -> {
                log.info("outbox record {} was successfully published, will update the outbox table", msg.id());
                return true;
            })
            .exceptionally(__ -> {
                log.warn("couldn't publish record {}, will retry next time", msg.id());
                return false;
            })
            .join();

        if (sent) {
            log.info("updating the outbox table and marking {} as published", msg.id());
            msg.publishedAt(Instant.now());
            outbox.save(msg);
        }
    }

}
