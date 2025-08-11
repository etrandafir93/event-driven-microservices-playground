package io.github.etr;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Execution(ExecutionMode.CONCURRENT)
public abstract class IntegrationTest {

    @Container
    protected static final ComposeContainer ENV = new ComposeContainer(new File("../docker-compose.yml")).withLocalCompose(true);

    protected static final RestClient restClient = RestClient.builder()
        .defaultHeader("Content-Type", "application/json")
        .baseUrl("http://localhost:8081/api/")
        .build();

    static protected Map<String, Object> givenPostRequest(String path, String jsonPayload) {
        return restClient.post()
            .uri(path)
            .body(jsonPayload)
            .exchange((request, response) ->
                response.bodyTo(new ParameterizedTypeReference<Map<String, Object>>() {}));
    }

}
