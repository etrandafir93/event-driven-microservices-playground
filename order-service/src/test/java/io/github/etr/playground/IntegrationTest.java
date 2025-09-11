package io.github.etr.playground;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.mockito.ArgumentMatchers.any;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import io.github.etr.playground.application.SystemTime;
import io.github.etr.playground.spy.SystemTimeSpy;
import io.github.etr.playground.infra.OutgoingKafkaMessages;
import io.github.etr.playground.spy.StringKafkaTemplateSpy;

@ActiveProfiles("test")
@Import(IntegrationTest.Config.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    protected OutgoingKafkaMessages outgoingKafkaMessages;

    @Autowired
    protected StringKafkaTemplateSpy stringKafkaTemplateSpy;

    @Autowired
    protected SystemTimeSpy systemTimeSpy;

    static {
        Awaitility.setDefaultPollInterval(ofMillis(100));
        Awaitility.setDefaultTimeout(ofSeconds(10));
    }

    protected void sendKafkaMessage(String topic, String key, String payload) {
        sendKafkaMessage(topic, key, payload, UUID.randomUUID()
            .toString());
    }

    protected void sendKafkaMessage(String topic, String key, String payload, String idempotencyKey) {
        ProducerRecord<String, String> msg = new ProducerRecord<>(topic, key, payload);
        msg.headers()
            .add("idempotency-key", idempotencyKey.getBytes());

        stringKafkaTemplateSpy.send(msg)
            .join();
    }

    protected Map<String, Object> sendPostRequest(String path, String jsonPayload) {
        return restClient(port).post()
            .uri(path)
            .body(jsonPayload)
            .exchange((req, resp) -> resp.bodyTo(new ParameterizedTypeReference<Map<String, Object>>() {
            }));
    }

    protected Map<String, Object> sendGetRequest(String path) {
        return restClient(port).get()
            .uri(path)
            .exchange((req, resp) -> resp.bodyTo(new ParameterizedTypeReference<Map<String, Object>>() {
            }));
    }

    protected void givenKafkaIsDown() {
        stringKafkaTemplateSpy.send(msg -> {
            throw new KafkaException("Ooupps!! Kafka is down!");
        });
    }

    protected void givenKafkaIsDownFor(Duration duration) {
        stringKafkaTemplateSpy.send(msg -> {
            throw new KafkaException("Ooupps!! Kafka is down!");
        });
        Thread.ofVirtual().start(() -> {
            sleep(duration);
            stringKafkaTemplateSpy.callRealFunction();
        });
    }

    @BeforeEach
    void stub() {
       systemTimeSpy.callRealFunction();
       stringKafkaTemplateSpy.callRealFunction();
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static RestClient restClient(int port) {
        return RestClient.builder()
            .defaultHeader("Content-Type", "application/json")
            .baseUrl("http://localhost:%s/api/".formatted(port))
            .build();
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
        @Primary
        SystemTime systemTimeStub(SystemTime systemTime) {
            return new SystemTimeSpy(systemTime);
        }

        @Bean
        @Primary
        KafkaOperations<String, String> stringKafkaTemplateSpy(KafkaOperations<String, String> stringKafkaTemplate) {
            return new StringKafkaTemplateSpy(stringKafkaTemplate);
        }

    }

}
