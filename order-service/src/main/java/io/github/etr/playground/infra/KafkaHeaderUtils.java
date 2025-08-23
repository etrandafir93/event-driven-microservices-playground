package io.github.etr.playground.infra;

import java.util.Optional;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import lombok.experimental.UtilityClass;

@UtilityClass
class KafkaHeaderUtils {

    static String idempotencyKey(ConsumerRecord<String, String> message) {
        return Optional.ofNullable(message.headers()
                .lastHeader("idempotency-key"))
            .map(Header::value)
            .map(String::new)
            .orElseGet(() -> UUID.randomUUID().toString());
    }
}
