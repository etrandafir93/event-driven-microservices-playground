package io.github.etr.playground;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;

import java.time.Duration;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

@ActiveProfiles("test")
@Import({ IntegrationTest.Config.class })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

    @Autowired
    private KafkaOperations<String, String> stringKafkaTemplate;

    @Autowired
    protected OutgoingKafkaMessages outgoingKafkaMessages;

    static {
        Awaitility.setDefaultPollInterval(ofMillis(100));
        Awaitility.setDefaultTimeout(ofSeconds(10));
    }

    protected void sendKafkaMessage(String topic, String key, String payload) {
        stringKafkaTemplate.send(topic, key, payload)
            .join();
    }

    protected void sendKafkaMessageWithTypeHeader(String topic, String key, String payload) {
        Message<String> message = MessageBuilder
            .withPayload(payload)
            .setHeader(KafkaHeaders.TOPIC, topic)
            .setHeader(KafkaHeaders.KEY, key)
            .build();

        stringKafkaTemplate.send(message)
            .join();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    private String json(Map<String, Integer> map) {
        return objectMapper.writeValueAsString(map);
    }

    protected void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread()
                .interrupt();
            throw new RuntimeException(e);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class Config {

        @Bean
        @ServiceConnection
        ConfluentKafkaContainer kafka() {
            return new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0");
        }

        @Bean
        KafkaOperations<String, String> stringKafkaTemplate(ProducerFactory<?, ?> producerFactory) {
            return (KafkaTemplate<String, String>) new KafkaTemplate<>(producerFactory,
                Map.of(
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName(),
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()
                ));
        }
    }
}