package io.github.etr.playground.infra;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OutgoingKafkaMessages {
    private final Map<String, List<Map<String, Object>>> messages = new ConcurrentHashMap<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SneakyThrows
    @KafkaListener(topics = "order-created")
    public void listen(String json) {
        log.info("Received message on 'order-created' topic: {}", json);
        var msg = OBJECT_MAPPER.readValue(json, Map.class);
        var orderId = msg.get("orderId").toString();
        messages.computeIfAbsent(orderId, __ -> new ArrayList<>()).add(msg);
    }

    public Map<String, Object> awaitForOrderCreated(String orderId) {
        await().atMost(ofSeconds(10))
            .pollInterval(ofMillis(100))
            .until(() -> messages.containsKey(orderId));

        List<Map<String, Object>> orderMsgs = messages.get(orderId);
        assertThat(orderMsgs).hasSize(1);
        return orderMsgs.getFirst();
    }
}
