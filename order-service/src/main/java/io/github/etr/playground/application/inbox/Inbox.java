package io.github.etr.playground.application.inbox;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface Inbox extends JpaRepository<InboxMessage, Long> {

    @Query("""
            SELECT msg.id
            FROM InboxMessage msg
            WHERE msg.status IN (
                io.github.etr.playground.application.inbox.InboxMessage.Status.PENDING,
                io.github.etr.playground.application.inbox.InboxMessage.Status.RETRYABLE_ERROR
            )
            ORDER BY msg.observedAt ASC
        """)
    List<Long> findIdsOfUnprocessed();

    @Query("""
            SELECT msg
            FROM InboxMessage msg
            WHERE msg.id = :id
              AND msg.processedAt IS NULL
        """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InboxMessage> findByIdLocking(Long id);

    default void incomingMessage(String topic, String key, String json) {
        InboxMessage msg = new InboxMessage()
            .topic(topic)
            .key(key)
            .payload(json);
        this.save(msg);
    }

}