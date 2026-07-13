package io.github.etr.demo.ui;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DemoClient {

    private static final List<String> CUSTOMER_POOL;
    private static final Random RANDOM = new Random();

    static {
        List<String> pool = new ArrayList<>(2000);
        for (int i = 0; i < 2000; i++) {
            pool.add(UUID.randomUUID().toString());
        }
        CUSTOMER_POOL = Collections.unmodifiableList(pool);
    }

    private final RestClient restClient;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private volatile int getRate = 6;
    private volatile int postRate = 2;

    private ScheduledFuture<?> getFuture;
    private ScheduledFuture<?> postFuture;

    public DemoClient(@Value("${server.port:8084}") int port) {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        log.info("App ready — delaying demo traffic by 3s");
        scheduler.schedule(() -> {
            scheduleGet(getRate);
            schedulePost(postRate);
            log.info("Demo traffic started (GET {}rps, POST {}rps)", getRate, postRate);
        }, 3, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdownNow();
    }

    public void setGetRate(int requestsPerSecond) {
        getRate = requestsPerSecond;
        if (getFuture != null) getFuture.cancel(false);
        scheduleGet(requestsPerSecond);
    }

    public void setPostRate(int requestsPerSecond) {
        postRate = requestsPerSecond;
        if (postFuture != null) postFuture.cancel(false);
        schedulePost(requestsPerSecond);
    }

    public int getGetRate() { return getRate; }
    public int getPostRate() { return postRate; }

    private void scheduleGet(int rps) {
        if (rps <= 0) return;
        long intervalMs = 1000L / rps;
        getFuture = scheduler.scheduleAtFixedRate(this::sendGetRequest, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    private void schedulePost(int rps) {
        if (rps <= 0) return;
        long intervalMs = 1000L / rps;
        postFuture = scheduler.scheduleAtFixedRate(this::sendPostRequest, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    private void sendGetRequest() {
        String customerId = randomCustomer();
        try {
            restClient.get()
                    .uri("/api/orders/customer/{id}", customerId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("GET /api/orders/customer/{} failed", customerId, e);
        }
    }

    private void sendPostRequest() {
        String customerId = randomCustomer();
        try {
            restClient.post()
                    .uri("/api/orders")
                    .body(Map.of("customerId", customerId, "customerEmail", customerId + "@demo.com"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("POST /api/orders failed for customer {}", customerId, e);
        }
    }

    private String randomCustomer() {
        return CUSTOMER_POOL.get(RANDOM.nextInt(CUSTOMER_POOL.size()));
    }


}
