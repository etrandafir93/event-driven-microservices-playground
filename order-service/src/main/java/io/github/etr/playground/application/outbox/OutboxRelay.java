package io.github.etr.playground.application.outbox;

import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
class OutboxRelay {

    private final OutboxRepo outbox;
    private final OutboxKafkaPublisher outboxPublisher;

    @Scheduled(fixedDelayString = "${outbox.relay.delay.ms}")
    public void relay() {
        try {
            log.info("fetching records to publish from the outbox table..");
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

}
