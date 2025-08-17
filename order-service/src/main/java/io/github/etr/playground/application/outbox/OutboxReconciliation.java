package io.github.etr.playground.application.outbox;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
class OutboxReconciliation {

    private final Outbox outbox;
    private final ObjectMapper objectMapper;
    private final OutboxKafkaPublisher outboxPublisher;

    @Scheduled(fixedDelay = 100)
    public void reconcile() {
        try {
            log.debug("fetching records to publish from the outbox table..");
            List<Long> unpublished = outbox.findIdsOfUnpublished();

            if (unpublished.isEmpty()) {
                return;
            }
            log.info("found {} outbox records to be published", unpublished.size());
            unpublished.forEach(outboxPublisher::publishMsgAndUpdateStatus);

        } catch (Exception e) {
            log.error("error publishing outbox records to kafka, {}", e.getMessage(), e);
        }
    }

    @SneakyThrows
    @Transactional(propagation = Propagation.REQUIRED)
    @EventListener(OutboxEvent.class)
    public void onOutboxEvent(OutboxEvent event) {
        var outboxMsg = new OutboxMessage()
            .topic(event.topic())
            .key(event.key())
            .payload(objectMapper.writeValueAsString(event))
            .eventType(event.getClass().getName())
            .observedAt(Instant.now());

        outboxMsg = outbox.save(outboxMsg);
        log.info("Received OrderCreatedEvent for orderId: {}, and persisted it to the outbox table: {}",
            event.key(), outboxMsg);
    }

}
