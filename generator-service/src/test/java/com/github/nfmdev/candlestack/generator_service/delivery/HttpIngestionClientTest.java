package com.github.nfmdev.candlestack.generator_service.delivery;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import com.github.nfmdev.candlestack.generator_service.domain.model.DeliveryConfig;
import com.github.nfmdev.candlestack.generator_service.domain.model.DeliveryMode;
import com.github.nfmdev.candlestack.generator_service.domain.model.MarketTradeEvent;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;
import com.github.nfmdev.candlestack.generator_service.domain.model.SymbolConfig;
import com.github.nfmdev.candlestack.generator_service.domain.model.TradeSide;
import com.github.nfmdev.candlestack.generator_service.infrastructure.delivery.HttpIngestionClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(HttpIngestionClient.class)
class HttpIngestionClientTest {

    @Autowired
    private HttpIngestionClient httpIngestionClient;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void deliverShouldPostEventToIngestionService() {
        server.expect(requestTo("http://localhost:8081/api/v1/market-events"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess());

        httpIngestionClient.deliver(scenario(), event());

        server.verify();
    }

    private SimulationScenario scenario() {
        Instant now = Instant.now();

        return new SimulationScenario(
                UUID.randomUUID(),
                "crypto-dev",
                List.of(
                        new SymbolConfig(
                                "BTC-USD",
                                new BigDecimal("68000.00"),
                                new BigDecimal("50000.00"),
                                new BigDecimal("90000.00"),
                                new BigDecimal("0.010000"),
                                new BigDecimal("1.500000"),
                                5,
                                "USD"
                        )
                ),
                new DeliveryConfig(
                        DeliveryMode.HTTP,
                        "http://localhost:8081",
                        "/api/v1/market-events",
                        1000,
                        2000
                ),
                42L,
                now,
                now
        );
    }

    private MarketTradeEvent event() {
        return new MarketTradeEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "generator-service",
                "TRADE",
                "BTC-USD",
                Instant.now(),
                1L,
                new BigDecimal("68123.45"),
                new BigDecimal("0.245000"),
                TradeSide.BUY,
                "USD"
        );
    }
}