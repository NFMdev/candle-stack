package com.github.nfmdev.candlestack.ingestion_service.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.github.nfmdev.candlestack.ingestion_service.api.dto.EventAcceptedResponse;
import com.github.nfmdev.candlestack.ingestion_service.application.IngestionApplicationService;
import com.github.nfmdev.candlestack.ingestion_service.domain.model.TradeSide;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = MarketEventController.class,
    properties = "spring.mvc.problemdetails.enabled=true"
)
@Import(GlobalExceptionHandler.class)
class MarketEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IngestionApplicationService ingestionApplicationService;

    @Test
    void ingestShouldReturnAccepted() throws Exception {
        UUID eventId = UUID.randomUUID();

        when(ingestionApplicationService.ingest(Mockito.any()))
                .thenReturn(new EventAcceptedResponse("ACCEPTED", eventId));

        String payload = objectMapper.writeValueAsString(new RequestFixture(
            eventId,
            UUID.randomUUID(),
            "generator-service",
            "TRADE",
            "BTC-USD",
            Instant.now(),
            1L,
            new BigDecimal("68000.50"),
            new BigDecimal("0.250000"),
            TradeSide.BUY,
            "USD"
        ));

        mockMvc.perform(post("/api/v1/market-events")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.eventId").value(eventId.toString()));
    }

    @Test
    void ingestShouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        String payload = """
                {
                    "eventId": null,
                    "scenarioId": null,
                    "source": "",
                    "eventType": "",
                    "symbol": "",
                    "eventTime": null,
                    "sequence": 0,
                    "price": -1,
                    "quantity": 0,
                    "side": null,
                    "currency": ""
                }
                """;

        mockMvc.perform(post("/api/v1/market-events")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }
    
    private record RequestFixture(
        UUID eventId,
        UUID scenarioId,
        String source,
        String eventType,
        String symbol,
        Instant eventTime,
        long sequence,
        BigDecimal price,
        BigDecimal quantity,
        TradeSide side,
        String currency
    ) {}
}
