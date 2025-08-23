package io.github.etr.playground.application.inbox;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Repository
public interface Inbox extends JpaRepository<InboxMessage, Long> {

    static final Logger log = LoggerFactory.getLogger(Inbox.class);

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
            .payload(json)
            .idempotencyKey(UUID.randomUUID().toString());
        this.save(msg);
    }

    default void uniqueIncomingMessage(String topic, String key, String json, String idempotencyKey) {
        InboxMessage msg = new InboxMessage()
            .topic(topic)
            .key(key)
            .payload(json)
            .idempotencyKey(idempotencyKey);

        try {
            this.save(msg);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            log.info("ignoring duplicated inbox message for topic={} and key={}, with idempotencyKey={}",
                msg.topic(), msg.key(), msg.idempotencyKey(), e);
        }
    }

}