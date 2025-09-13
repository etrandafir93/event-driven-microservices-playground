package io.github.etr.playground.infra.inbox;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class InboxRelay {

    private final InboxRepo inboxRepo;
    private final InboxProcessor processor;

    @NewSpan("inbox")
    @Scheduled(fixedDelayString = "${inbox.relay.delay.ms}")
    public void relay() {
        log.debug("fetching records to publish from the inbox table..");
        List<Long> unprocessed = inboxRepo.findIdsOfUnprocessed();

        if (unprocessed.isEmpty()) {
            return;
        }
        log.info("found {} inbox records to be processed", unprocessed.size());
        unprocessed.forEach(this::tryProcessingMsg);
    }

    private void tryProcessingMsg(Long inboxMsgId) {
        try {
            processor.process(inboxMsgId);
        } catch (Exception e) {
            log.error("error processing inbox records, {}", e.getMessage(), e);
        }
    }

}