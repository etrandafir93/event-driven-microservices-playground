package io.github.etr.playground.application.outbox;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
class OutboxRelay {

    private final Tracer tracer;
    private final Outbox outbox;
    private final OutboxKafkaPublisher outboxPublisher;
    private final ObjectMapper mapper;

    @NewSpan("outbox")
    @Scheduled(fixedDelayString = "${outbox.relay.delay.ms}")
    public void relay() {
        try {
            log.debug("fetching records to publish from the outbox table..");
            List<Long> unpublished = outbox.findIdsOfUnpublished();

            if (unpublished.isEmpty()) {
                return;
            }
            log.info("found {} outbox records to be published", unpublished.size());
            unpublished.forEach(outboxPublisher::publishMsgAndUpdateStatus);

        } catch (Exception e) {
            log.error("error processing inbox records to kafka, {}", e.getMessage(), e);
        }
    }

    @SneakyThrows
    @Transactional(propagation = Propagation.REQUIRED)
    @EventListener(OutboxEvent.class)
    public void onOutboxEvent(OutboxEvent event) {
        var outboxMsg = new OutboxMessage()
            .topic(event.topic())
            .key(event.key())
            .payload(mapper.writeValueAsString(event))
            .eventType(event.getClass().getName())
            .observedAt(Instant.now())
            .originalTraceId(currentTraceId());

        outboxMsg = outbox.save(outboxMsg);
        log.info("Received OrderCreatedEvent for orderId: {}, and persisted it to the outbox table: {}",
            event.key(), outboxMsg);
    }

    private String currentTraceId() {
        return Optional.ofNullable(tracer.currentTraceContext()
                .context())
            .map(TraceContext::traceId)
            .orElse("");
    }
}
