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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import io.github.etr.playground.inventory.Inventory;
import io.github.etr.playground.inventory.InventoryItem;

@ActiveProfiles("test")
@Import({ IntegrationTest.Config.class })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

    @Autowired
    private KafkaOperations<String, String> stringKafkaTemplate;

    @Autowired
    protected OutgoingKafkaMessages outgoingKafkaMessages;

    @Autowired
    private Inventory inventory;

    protected static WireMockServer wiremock = new WireMockServer(
        WireMockConfiguration.options().dynamicPort());

    static {
        Awaitility.setDefaultPollInterval(ofMillis(100));
        Awaitility.setDefaultTimeout(ofSeconds(10));

        wiremock.start();
        wiremock.stubFor(post(urlEqualTo("/items"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{ \"status\": \"OK\" }")));
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("stock.replenishment.supplier.url", () -> wiremock.baseUrl());
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