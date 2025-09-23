package io.github.etr.playground;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;

import java.net.URI;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

@ActiveProfiles("test")
@Import({IntegrationTest.Config.class, OutgoingKafkaMessages.Config.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    protected OutgoingKafkaMessages outgoingKafkaMessages;

    static {
        Awaitility.setDefaultPollInterval(ofMillis(100));
        Awaitility.setDefaultTimeout(ofSeconds(10));
    }

    @BeforeEach
    void reset() {
        outgoingKafkaMessages.reset();
    }

    protected void sendKafkaMessage(String topic, String key, String payload) {
        stringKafkaTemplate().send(topic, key, payload)
            .join();
    }

    protected void sendPutRequest(String path, Map<String, String> query) {
        restClient(port)
            .put()
            .uri(uriBuilder -> requestUri(uriBuilder, path, query))
            .retrieve()
            .toBodilessEntity();
    }

    protected Map<String, Object> sendGetRequest(String path) {
        return restClient(port).get()
            .uri(path)
            .exchange((req, resp) -> resp.bodyTo(new ParameterizedTypeReference<Map<String, Object>>() {
            }));
    }

    @SneakyThrows
    protected JsonNode httpRequest(String method, String path) {
        String resp = restClient(port)
            .method(HttpMethod.valueOf(method))
            .uri(path)
            .retrieve()
            .body(String.class);

        return new ObjectMapper().readTree(resp);
    }

    @SneakyThrows
    protected JsonNode getJson(String path) {
        String resp = restClient(port).get()
            .uri(path)
            .retrieve()
            .body(String.class);

        return new ObjectMapper().readTree(resp);
    }

    private static RestClient restClient(int port) {
        return RestClient.builder()
            .defaultHeader("Content-Type", "application/json")
            .baseUrl("http://localhost:%s/api/v1/".formatted(port))
            .build();
    }

    @Autowired
    private ProducerFactory<?, ?> producerFactory;

    private KafkaOperations<String, String> stringKafkaTemplate() {
        return (KafkaTemplate<String, String>) new KafkaTemplate<>(producerFactory,
            Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()));
    }

    private static URI requestUri(UriBuilder uriBuilder, String path, Map<String, String> query) {
        return query.entrySet()
            .stream()
            .reduce(
                uriBuilder.path(path),
                (ub, entry) -> ub.queryParam(entry.getKey(), entry.getValue()),
                (b1, b2) -> b1
            )
            .build();
    }

    @Configuration(proxyBeanMethods = false)
    static class Config {

        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgres() {
            return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine")).withDatabaseName("shipping_db");
        }

        @Bean
        @ServiceConnection
        ConfluentKafkaContainer kafka() {
            return new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0");
        }

    }
}