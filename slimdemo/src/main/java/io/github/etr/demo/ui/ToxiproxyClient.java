package io.github.etr.demo.ui;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ToxiproxyClient {

    public static final String DB_PROXY = "postgres";
    public static final String DB_TOXIC = "db-latency";
    public static final String LOYALTY_PROXY = "wiremock";
    public static final String LOYALTY_TOXIC = "wm-latency";

    private final RestClient rest;

    public ToxiproxyClient(@Value("${toxiproxy.url:http://localhost:8474}") String url) {
        this.rest = RestClient.builder().baseUrl(url).build();
    }

    public int getLatency(String proxy, String toxic) {
        try {
            Map<?, ?> body = rest.get()
                    .uri("/proxies/{p}/toxics/{t}", proxy, toxic)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {})
                    .body(Map.class);
            if (body == null) return 0;
            Map<?, ?> attrs = (Map<?, ?>) body.get("attributes");
            return ((Number) attrs.get("latency")).intValue();
        } catch (Exception e) {
            log.warn("Failed to read toxic {}/{}: {}", proxy, toxic, e.getMessage());
            return 0;
        }
    }

    public void setLatency(String proxy, String toxic, int latencyMs) {
        deleteIfExists(proxy, toxic);
        if (latencyMs <= 0) return;
        try {
            rest.post()
                    .uri("/proxies/{p}/toxics", proxy)
                    .body(Map.of(
                            "name", toxic,
                            "type", "latency",
                            "stream", "downstream",
                            "attributes", Map.of("latency", latencyMs, "jitter", 0)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to create toxic {}/{} at {}ms: {}", proxy, toxic, latencyMs, e.getMessage());
        }
    }

    private void deleteIfExists(String proxy, String toxic) {
        try {
            rest.delete()
                    .uri("/proxies/{p}/toxics/{t}", proxy, toxic)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {})
                    .toBodilessEntity();
        } catch (Exception e) {
            log.debug("Delete toxic {}/{} ignored: {}", proxy, toxic, e.getMessage());
        }
    }
}
