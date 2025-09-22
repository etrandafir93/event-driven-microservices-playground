package io.github.etr.playground;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.messaging.Message;
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
    private final Map<String, List<Map<String, Object>>> messagesByTopic = new ConcurrentHashMap<>();

    @SneakyThrows
    @KafkaListener(
        containerFactory = "stringDeserKafkaListenerContainerFactory",
        topics = { "order-shipped", "order-packed", "order-delivered" }
    )
    public void listen(Message<String> message) {
        var topic = message.getHeaders().get("kafka_receivedTopic").toString();
        var msg = OBJECT_MAPPER.readValue(message.getPayload(), Map.class);

        log.info("Received message on '{}' topic: {}", topic, msg);
        messagesByTopic.computeIfAbsent(topic, __ -> new ArrayList<>())
            .add(msg);
    }

    public List<Map<String, Object>> messagesFor(String topic) {
        return messagesByTopic.getOrDefault(topic, emptyList());
    }

    public void reset() {
        messagesByTopic.clear();
    }

    @TestConfiguration
    static class Config {

        @Bean
        ConcurrentKafkaListenerContainerFactory<String, String> stringDeserKafkaListenerContainerFactory(KafkaProperties kp, ConfluentKafkaContainer kafka) {
            Map<String, Object> props = kp.buildConsumerProperties();
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "shipping-service-test-group");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());

            DefaultKafkaConsumerFactory<String, String> kcf = new DefaultKafkaConsumerFactory<>(props);
            ConcurrentKafkaListenerContainerFactory<String, String> cf = new ConcurrentKafkaListenerContainerFactory<>();
            cf.setConsumerFactory(kcf);
            return cf;
        }
    }

}