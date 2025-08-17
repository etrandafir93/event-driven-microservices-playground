package io.github.etr.playground.application.inbox;

import java.time.Instant;
import java.util.Optional;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class InboxProcessor {

    private final Inbox inbox;
    private final ObjectMapper mapper;
    private final ApplicationContext context;
    private final ApplicationEventPublisher eventPublisher;

    @SneakyThrows
    @Transactional
    public void process(Long inboxMsgId) {
        InboxMessage msg = inbox.findByIdLocking(inboxMsgId)
            .orElseThrow();

        var adapterOpt = findAdapterForTopic(msg.topic());
        if (adapterOpt.isEmpty()) {
            log.error("the topic {} is not configured properly for using the inbox table. No InboxMessageAdapter<> bean was found", msg.topic());
            msg.status(InboxMessage.Status.RETRYABLE_ERROR);
            inbox.save(msg);
        }
        var adapter = adapterOpt.get();

        try {
            eventPublisher.publishEvent(adapter.adapt(msg.payload()));
            msg.status(InboxMessage.Status.PROCESSED_OK);
            msg.processedAt(Instant.now());
            log.info("processed the inbox message {} and update updated the inbox table", msg.id());
        } catch (Exception e) {
            msg.status(InboxMessage.Status.NON_RETRYABLE_ERROR);
        }
        inbox.save(msg);
    }

    private Optional<InboxMessageAdapter> findAdapterForTopic(String topic) {
        return context.getBeansOfType(InboxMessageAdapter.class)
            .values()
            .stream()
            .filter(it -> it.topic().equals(topic))
            .findFirst();
    }

}
