package io.github.etr.playground;

import static java.util.concurrent.CompletableFuture.failedFuture;
import static org.mockito.ArgumentMatchers.any;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import io.github.etr.playground.infra.OutgoingKafkaMessages;

@ActiveProfiles("test")
@Import(IntegrationTest.ContainersConfig.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

    @LocalServerPort
    private int port;

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

    @Autowired
    protected OutgoingKafkaMessages outgoingKafkaMessages;

    @MockitoSpyBean
    protected KafkaTemplate<String, String> stringKafkaTemplate;

    private final AtomicBoolean healthyKafka = new AtomicBoolean(true);

    protected void givenKafkaIsDown() {
        givenKafkaIsDownFor(Duration.ofHours(1));
    }

    protected void givenKafkaIsDownFor(Duration duration) {
        healthyKafka.set(false);
        Thread.ofVirtual()
            .start(() -> {
                sleep(duration);
                healthyKafka.set(true);
            });
    }

    @BeforeEach
    void beforeEach() {
        healthyKafka.set(true);
    }

    @BeforeEach
    void stub() {
        Mockito.doAnswer(invocation -> healthyKafka.get() ? invocation.callRealMethod() : failedFuture(new KafkaException("Ouupps!! Kafka is down!")))
            .when(stringKafkaTemplate)
            .send(any(ProducerRecord.class));
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
    static class ContainersConfig {

        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgres() {
            return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("order_db");
//                .withReuse(true);
        }

        @Bean
        @ServiceConnection
        ConfluentKafkaContainer kafka() {
            return new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0");
//                .withReuse(true);
        }
    }

}
