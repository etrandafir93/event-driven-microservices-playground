package io.github.etr.playground.infra;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import lombok.experimental.UtilityClass;

@UtilityClass
class KafkaHeaderUtils {

    public static final String IDEMPOTENCY_KEY_HEADER = "idempotency_key";
    public static final String OBSERVED_AT_HEADER = "observed_at";

    static String idempotencyKey(ConsumerRecord<String, String> message) {
        return Optional.ofNullable(message.headers()
                .lastHeader(IDEMPOTENCY_KEY_HEADER))
            .map(Header::value)
            .map(String::new)
            .orElseGet(() -> UUID.randomUUID()
                .toString());
    }

    static Instant observedAt(ConsumerRecord<String, String> message) {
        return Optional.ofNullable(message.headers()
                .lastHeader(OBSERVED_AT_HEADER))
            .map(Header::value)
            .map(String::new)
            .map(Instant::parse)
            .orElseGet(Instant::now);
    }
}
