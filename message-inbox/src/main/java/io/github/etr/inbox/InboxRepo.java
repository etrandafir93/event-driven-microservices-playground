package io.github.etr.inbox;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
interface InboxRepo extends JpaRepository<InboxMessage, Long>, Inbox {

    Logger log = LoggerFactory.getLogger(InboxRepo.class);

    @Query("""
            SELECT msg.id
            FROM InboxMessage msg
            WHERE msg.status IN (
                io.github.etr.inbox.InboxMessage.Status.PENDING,
                io.github.etr.inbox.InboxMessage.Status.RETRYABLE_ERROR
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

    @Override
    default void uniqueIncomingMessage(String topic, String key, String payload, String idempotencyKey) {
        InboxMessage msg = new InboxMessage()
            .topic(topic)
            .key(key)
            .payload(payload)
            .idempotencyKey(idempotencyKey);

        try {
            this.save(msg);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            log.info("ignoring duplicated inbox message for topic={} and key={}, with idempotencyKey={}",
                msg.topic(), msg.key(), msg.idempotencyKey(), e);
        }
    }

}