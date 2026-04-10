package io.github.etr.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
class LoyaltyServiceClient {

    private final RestTemplate restTemplate;

    @Value("${loyalty.service.url:http://localhost:8090}")
    private String loyaltyServiceUrl;

    void postAwardPoints(String customerId, String orderNumber, double orderAmount) {
        String url = loyaltyServiceUrl + "/api/v2/loyalty/points";

        Map<String, Object> request = new HashMap<>();
        request.put("customerId", customerId);
        request.put("orderNumber", orderNumber);
        request.put("orderAmount", orderAmount);
        request.put("pointsToAward", calculatePoints(orderAmount));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        log.info("Calling Loyalty Service for order: {}", orderNumber);
        restTemplate.postForEntity(url, entity, String.class);
    }

    private int calculatePoints(double amount) {
        return (int) (amount * 10);
    }
}
