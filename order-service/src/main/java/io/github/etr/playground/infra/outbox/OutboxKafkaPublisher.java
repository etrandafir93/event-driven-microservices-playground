package io.github.etr.playground.infra.outbox;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class OutboxKafkaPublisher {

    private final Tracer tracer;
    private final Outbox outbox;
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    @Transactional
    public void publishMsgAndUpdateStatus(Long outboxMsgId) {
        var msg = outbox.findByIdLocking(outboxMsgId)
            .orElseThrow(() -> new NoSuchElementException("couldn't fetch outboxMsgId=%s, it'll be processed by other job".formatted(outboxMsgId)));

        try (var __ = overrideTraceId(msg.originalTraceId())) {
            publishAndUpdate(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private AutoCloseable overrideTraceId(String traceId) {
        if (traceId.isEmpty()) {
            return () -> {};
        }

        TraceContext newTraceContext = tracer.traceContextBuilder()
            .traceId(traceId)
            .parentId(tracer.currentTraceContext()
                .context()
                .traceId())
            .spanId(randomSpanId())
            .sampled(true)
            .build();

        return tracer.currentTraceContext()
            .newScope(newTraceContext);
    }

    private static String randomSpanId() {
        return String.valueOf(ThreadLocalRandom.current()
            .nextLong(1, Long.MAX_VALUE));
    }

}
