package io.github.etr.playground;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.stereotype.Component;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OutgoingKafkaMessages {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Getter
    private final Map<String, List<Map<String, Object>>> messages = new ConcurrentHashMap<>();

    @SneakyThrows
    @KafkaListener(topics = "item-ordered", containerFactory = "stringDeserKafkaListenerContainerFactory")
    public void listen(String json) {
        log.info("Received message on 'item-ordered' topic: {}", json);
        var msg = OBJECT_MAPPER.readValue(json, Map.class);
        var productSku = msg.get("productSku")
            .toString();
        messages.computeIfAbsent(productSku, __ -> new ArrayList<>())
            .add(msg);
    }

    public List<Map<String, Object>> messagesForProduct(String sku) {
        return messages.getOrDefault(sku, emptyList());
    }

    @TestConfiguration
    static class Config {

        @Bean
        ConcurrentKafkaListenerContainerFactory<String, String> stringDeserKafkaListenerContainerFactory(KafkaProperties kp, ConfluentKafkaContainer kafka) {
            Map<String, Object> props = kp.buildConsumerProperties();
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-service-test-group");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());

            DefaultKafkaConsumerFactory<String, String> kcf = new DefaultKafkaConsumerFactory<>(props);
            ConcurrentKafkaListenerContainerFactory<String, String> cf = new ConcurrentKafkaListenerContainerFactory<>();
            cf.setConsumerFactory(kcf);
            return cf;
        }
    }

}
