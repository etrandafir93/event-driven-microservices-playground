package io.github.etr.playground;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PostConstruct;

import org.junit.jupiter.api.BeforeAll;
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


}
