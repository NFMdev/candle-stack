package com.github.nfmdev.candlestack.generator_service.infrastructure.delivery;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.github.nfmdev.candlestack.generator_service.domain.model.DeliveryConfig;
import com.github.nfmdev.candlestack.generator_service.domain.model.MarketTradeEvent;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;
import com.github.nfmdev.candlestack.generator_service.domain.ports.EventDeliveryPort;

@Component
public class HttpIngestionClient implements EventDeliveryPort {
    private final RestClient.Builder restClientBuilder;

    public HttpIngestionClient(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    public void deliver(SimulationScenario scenario, MarketTradeEvent event) {
        DeliveryConfig delivery = scenario.getDeliveryConfig();

        RestClient restClient = restClientBuilder
                .baseUrl(delivery.ingestionBaseUrl())
                .build();

        try {
            restClient.post()
                    .uri(delivery.endpointPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException ex) {
            throw new IllegalStateException("Failed to deliver event to ingestion-service", ex);
        }
    }
}