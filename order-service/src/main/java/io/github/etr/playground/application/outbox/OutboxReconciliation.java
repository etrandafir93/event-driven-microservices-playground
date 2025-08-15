package io.github.etr.playground.application.outbox;

import static java.util.concurrent.CompletableFuture.runAsync;

import java.time.Instant;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.etr.playground.application.outbox.OutboxKafkaPublisher.OutboxMessageReadyToPublish;
import io.github.etr.playground.domain.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class OutboxReconciliation {

    private final Outbox outbox;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEvents;

    @Scheduled(fixedDelay = 1000L)
    public void reconcile() {
        outbox.findIdsOfUnpublished(Instant.now()
                .minusSeconds(10))
            .stream()
            .peek(msg -> log.info("Will trigger reconciliation job of outbox message with id: {}", msg))
            .map(OutboxMessageReadyToPublish::new)
            .forEach(applicationEvents::publishEvent);
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

        log.info("Received OrderCreatedEvent for orderId: {}, persisting it to the outbox table: {}",
            event.key(), outboxMsg);

        outboxMsg = outbox.save(outboxMsg);
        long outboxId = outboxMsg.id();

        runAsync(() -> applicationEvents.publishEvent(
            new OutboxMessageReadyToPublish(outboxId)));
    }

    @SneakyThrows
    private String messagePayload(OrderCreatedEvent event) {
        return objectMapper.writeValueAsString(event);
    }

    record OrderCreatedKafkaMessage(String orderId, String customerUsername, Map<String, Integer> order) {
    }

}
