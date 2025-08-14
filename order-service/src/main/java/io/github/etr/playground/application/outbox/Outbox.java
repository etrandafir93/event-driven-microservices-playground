package io.github.etr.playground.application.outbox;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface Outbox extends JpaRepository<OutboxMessage, Long> {

    @Query("""
        SELECT msg.id
        FROM OutboxMessage msg
        WHERE msg.publishedAt IS NULL
          AND msg.observedAt < :cutoff
        ORDER BY msg.observedAt ASC
    """)
    List<Long> findIdsOfUnpublished(@Param("cutoff") Instant cutoff);

    @Query("""
        SELECT msg
        FROM OutboxMessage msg
        WHERE msg.id = :id
    """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OutboxMessage> findByIdLocking(Long id);

}

