package io.github.etr.inbox;

import java.time.Instant;
import java.util.Optional;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class InboxProcessor {

    private final InboxRepo inbox;
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
            return;
        }
        var adapter = adapterOpt.get();

        try {
            Object domainEvent = adapter.domainEvent(msg.payload());
            eventPublisher.publishEvent(domainEvent);
            msg.status(InboxMessage.Status.PROCESSED_OK);
            msg.processedAt(Instant.now());
            log.info("processed the inbox message {} and updated the inbox table", msg.id());

        } catch (IllegalStateException e) {
            log.warn("an error occurred while processing the inbox message {}," +
                " will mark its status as 'RETRYABLE_ERROR'", msg.id(), e);
            msg.status(InboxMessage.Status.RETRYABLE_ERROR);

        } catch (Exception e) {
            log.error("a non-retryable error occurred while processing the inbox message {}," +
                " will mark its status as 'NON_RETRYABLE_ERROR'", msg.id(), e);
            msg.status(InboxMessage.Status.NON_RETRYABLE_ERROR);
        }
        inbox.save(msg);
    }

    @SuppressWarnings("rawtypes")
    private Optional<InboxMessageAdapter> findAdapterForTopic(String topic) {
        return context.getBeansOfType(InboxMessageAdapter.class)
            .values()
            .stream()
            .filter(it -> it.topic().equals(topic))
            .findFirst();
    }

}