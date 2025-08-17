package io.github.etr.playground.application.inbox;

import java.util.List;

import org.apache.catalina.core.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
class InboxRelay {

    private final Inbox inboxRepo;
    private final InboxProcessor processor;

    @Scheduled(fixedDelayString = "${inbox.relay.delay.ms}")
    public void relay() {
        try {
            log.debug("fetching records to publish from the inbox table..");
            List<Long> unprocessed = inboxRepo.findIdsOfUnprocessed();

            if (unprocessed.isEmpty()) {
                return;
            }
            log.info("found {} inbox records to be processed", unprocessed.size());
            unprocessed.forEach(processor::process);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("error processing inbox records, {}", e.getMessage(), e);
        }
    }

}
