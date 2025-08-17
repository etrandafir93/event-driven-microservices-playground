package io.github.etr.playground.application;

import static java.util.concurrent.CompletableFuture.failedFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PostConstruct;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.github.etr.playground.infra.OutgoingKafkaMessages;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public abstract class IntegrationTest {

    @Container
    protected static final ComposeContainer ENV = new ComposeContainer(new File("../docker-compose.yml")).withServices("postgres", "kafka", "zookeeper")
        .withLocalCompose(true);

    protected static final RestClient restClient = RestClient.builder()
        .defaultHeader("Content-Type", "application/json")
        .baseUrl("http://localhost:8081/api/")
        .build();

    protected static Map<String, Object> sendPostRequest(String path, String jsonPayload) {
        return restClient.post()
            .uri(path)
            .body(jsonPayload)
            .exchange((req, resp) -> resp.bodyTo(new ParameterizedTypeReference<Map<String, Object>>() {
            }));
    }

    @Autowired
    protected OutgoingKafkaMessages outgoingKafkaMessages;

    @MockitoSpyBean
    private KafkaTemplate<String, String> stringKafkaTemplate;

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
    void setup() {
        healthyKafka.set(true);
    }

    @BeforeEach
    void stub() {
        Mockito.doAnswer(invocation ->
                healthyKafka.get()
                    ? invocation.callRealMethod()
                    : failedFuture(new KafkaException("Ouupps!! Kafka is down!")))
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
}
