package com.github.nfmdev.candlestack.ingestion_service.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.nfmdev.candlestack.ingestion_service.api.dto.EventAcceptedResponse;
import com.github.nfmdev.candlestack.ingestion_service.api.dto.MarketTradeEventRequest;
import com.github.nfmdev.candlestack.ingestion_service.application.IngestionApplicationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/market-events")
public class MarketEventController {
    private final IngestionApplicationService ingestionApplicationService;

    public MarketEventController(IngestionApplicationService ingestionApplicationService) {
        this.ingestionApplicationService = ingestionApplicationService;
    }

    @PostMapping
    public ResponseEntity<EventAcceptedResponse> ingest(@Valid @RequestBody MarketTradeEventRequest request) {
        EventAcceptedResponse response = ingestionApplicationService.ingest(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
