package io.github.etr.playground;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import io.github.etr.playground.inventory.Inventory;
import io.github.etr.playground.inventory.InventoryItem;

@EnableWireMock(
    @ConfigureWireMock(name = "stock-supplier", port = 9999, filesUnderDirectory = "src/test/resources/wiremock"))
// TODO: use dynamic port instead!
@ActiveProfiles("test")
@Import({ IntegrationTest.Config.class })
@SpringBootTest(classes = InventoryServiceApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

    @Autowired
    private KafkaOperations<String, String> stringKafkaTemplate;

    @Autowired
    protected OutgoingKafkaMessages outgoingKafkaMessages;

    @Autowired
    private Inventory inventory;

    static {
        Awaitility.setDefaultPollInterval(ofMillis(100));
        Awaitility.setDefaultTimeout(ofSeconds(10));
    }

    @BeforeEach
    void reset() {
        outgoingKafkaMessages.reset();
        inventory.deleteAll();
        inventory.save(new InventoryItem("DUMMY-SKU-10", 10));
        inventory.save(new InventoryItem("DUMMY-SKU-10k", 10_000));
    }

    protected void sendKafkaMessage(String topic, String key, String payload) {
        stringKafkaTemplate.send(topic, key, payload)
            .join();
    }

    protected static Predicate<List<Map<String, Object>>> hasCount(int count) {
        return it -> it.size() == count;
    }

    @Configuration(proxyBeanMethods = false)
    static class Config {

        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgres() {
            return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine")).withDatabaseName("order_db");
        }

        @Bean
        @ServiceConnection
        ConfluentKafkaContainer kafka() {
            return new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0");
        }

        @Bean
        KafkaOperations<String, String> stringKafkaTemplate(ProducerFactory<?, ?> producerFactory) {
            return (KafkaTemplate<String, String>) new KafkaTemplate<>(producerFactory,
                Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName(), ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    StringSerializer.class.getName()));
        }
    }
}