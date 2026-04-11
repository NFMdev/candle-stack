package com.github.nfmdev.candlestack.generator_service.infrastructure.delivery;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.github.nfmdev.candlestack.generator_service.domain.model.DeliveryConfig;
import com.github.nfmdev.candlestack.generator_service.domain.model.MarketTradeEvent;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;
import com.github.nfmdev.candlestack.generator_service.domain.ports.EventDeliveryPort;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class HttpIngestionClient implements EventDeliveryPort {
    private final ObjectMapper objectMapper;
    private final Map<Integer, HttpClient> clientsByConnectionTimeout = new ConcurrentHashMap<>();

    public HttpIngestionClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void deliver(SimulationScenario scenario, MarketTradeEvent event) {
        DeliveryConfig delivery = scenario.getDeliveryConfig();
        HttpClient client = clientFor(delivery.connectTimeoutMs());

        String payload = serialize(event);
        URI uri = URI.create(delivery.ingestionBaseUrl() + delivery.endpointPath());

        HttpRequest request = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofMillis(delivery.readTimeoutMs()))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();

        try {
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("ingestion-service responded with status " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to deliver event to ingestion-service", e);
        }
    }

    private HttpClient clientFor(int connectTimeoutMs) {
        return clientsByConnectionTimeout.computeIfAbsent(
            connectTimeoutMs, 
            timeout -> HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeout))
                .build()
        );
    }

    private String serialize(MarketTradeEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize event", e);
        }
    }
}