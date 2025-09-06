package io.github.etr.playground.infra.outbox;

import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class OutboxKafkaPublisher {

    private final Outbox outbox;
    private final KafkaOperations<String, String> stringKafkaTemplate;

    @Transactional
    public void publishMsgAndUpdateStatus(Long outboxMsgId) {
        outbox.findByIdLocking(outboxMsgId)
            .ifPresentOrElse(this::publishAndUpdate, () -> {
                throw new NoSuchElementException("couldn't fetch outboxMsgId=%s, it'll be processed by other job".formatted(outboxMsgId));
            });
    }

    private void publishAndUpdate(OutboxMessage msg) {
        log.info("Preparing to publish and update outbox message: {}", msg);

        var kafkaMsg = new ProducerRecord<>(msg.topic(), msg.key(), msg.payload());
        kafkaMsg.headers()
            .add("outboxId", msg.id()
                .toString()
                .getBytes())
            .add("eventType", msg.eventType()
                .getBytes())
            .add("observedAt", msg.observedAt()
                .toString()
                .getBytes());

        msg.headers()
            .forEach(it -> kafkaMsg.headers()
                .add(it.key(), it.value()
                    .getBytes()));

        boolean sent = stringKafkaTemplate.send(kafkaMsg)
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

    private static KafkaTemplate<String, String> stringKafkaTempalte(ProducerFactory<?, ?> producerFactory) {
        return (KafkaTemplate<String, String>) new KafkaTemplate<>(producerFactory,
            Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()));
    }

}
