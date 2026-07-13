package io.github.etr.demo.loyalty;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class LoyaltyServiceClient {

    private final RestClient restClient;
    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();

    public LoyaltyServiceClient(
            @Value("${loyalty.service.url:http://localhost:8091}") String loyaltyServiceUrl,
            ObservationRegistry observationRegistry) {
        var httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.restClient = RestClient.builder()
                .baseUrl(loyaltyServiceUrl)
                .observationRegistry(observationRegistry)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    public void postAwardPoints(String customerId, String orderNumber, Object orderAmount) {
        log.info("Calling Loyalty Service for order: {}", orderNumber);
        CompletableFuture.runAsync(() -> restClient.post()
                .uri("/api/v2/loyalty/points")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "customerId", customerId,
                        "orderNumber", orderNumber,
                        "orderAmount", orderAmount,
                        "pointsToAward", orderAmount))
                .retrieve()
                .toBodilessEntity(), executor)
            .exceptionally(e -> {
                log.warn("Loyalty Service call failed for order {}", orderNumber, e);
                return null;
            });
    }
}
