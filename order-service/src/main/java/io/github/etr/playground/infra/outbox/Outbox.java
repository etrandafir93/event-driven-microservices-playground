package io.github.etr.playground.infra.outbox;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
interface Outbox extends JpaRepository<OutboxMessage, Long> {

    @Query("""
        SELECT msg.id
        FROM OutboxMessage msg
        WHERE msg.publishedAt IS NULL
        ORDER BY msg.observedAt ASC
    """)
    List<Long> findIdsOfUnpublished();

    @Query("""
        SELECT msg
        FROM OutboxMessage msg
        WHERE msg.id = :id
          AND msg.publishedAt IS NULL
    """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OutboxMessage> findByIdLocking(Long id);

}

