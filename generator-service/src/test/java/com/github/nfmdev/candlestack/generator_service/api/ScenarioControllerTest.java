package com.github.nfmdev.candlestack.generator_service.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.github.nfmdev.candlestack.generator_service.api.dto.CreateDeliveryRequest;
import com.github.nfmdev.candlestack.generator_service.api.dto.CreateScenarioRequest;
import com.github.nfmdev.candlestack.generator_service.api.dto.CreateSymbolRequest;
import com.github.nfmdev.candlestack.generator_service.api.dto.DeliveryConfigResponse;
import com.github.nfmdev.candlestack.generator_service.api.dto.ScenarioResponse;
import com.github.nfmdev.candlestack.generator_service.api.dto.SymbolConfigResponse;
import com.github.nfmdev.candlestack.generator_service.application.ScenarioApplicationService;
import com.github.nfmdev.candlestack.generator_service.domain.exception.ScenarioNotFoundException;
import com.github.nfmdev.candlestack.generator_service.domain.model.DeliveryMode;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScenarioController.class)
@Import(GlobalExceptionHandler.class)
class ScenarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScenarioApplicationService scenarioApplicationService;

    @Test
    void createShouldReturn201() throws Exception {
        CreateScenarioRequest request = new CreateScenarioRequest(
                "trade-dev",
                42L,
                List.of(
                        new CreateSymbolRequest(
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
                new CreateDeliveryRequest(
                        DeliveryMode.HTTP,
                        "http://localhost:8081",
                        "/api/v1/market-events",
                        1000,
                        2000
                )
        );

        UUID scenarioId = UUID.randomUUID();
        Instant now = Instant.now();

        ScenarioResponse response = new ScenarioResponse(
                scenarioId,
                "trade-dev",
                "STOPPED",
                42L,
                List.of(new SymbolConfigResponse(
                        "BTC-USD",
                        new BigDecimal("68000.00"),
                        new BigDecimal("50000.00"),
                        new BigDecimal("90000.00"),
                        new BigDecimal("0.010000"),
                        new BigDecimal("1.500000"),
                        5,
                        "USD"
                )),
                new DeliveryConfigResponse(
                        "HTTP",
                        "http://localhost:8081",
                        "/api/v1/market-events",
                        1000,
                        2000
                ),
                now,
                now
        );

        when(scenarioApplicationService.create(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(scenarioId.toString()))
                .andExpect(jsonPath("$.name").value("trade-dev"))
                .andExpect(jsonPath("$.status").value("STOPPED"))
                .andExpect(jsonPath("$.symbols[0].symbol").value("BTC-USD"))
                .andExpect(jsonPath("$.delivery.mode").value("HTTP"));
    }

    @Test
    void getShouldReturn404WhenScenarioDoesNotExist() throws Exception {
        UUID scenarioId = UUID.randomUUID();

        when(scenarioApplicationService.get(scenarioId))
                .thenThrow(new ScenarioNotFoundException(scenarioId));

        mockMvc.perform(get("/api/v1/scenarios/{scenarioId}", scenarioId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Scenario not found"));
    }
}
