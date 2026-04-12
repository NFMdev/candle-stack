package com.github.nfmdev.candlestack.ingestion_service.api.dto;

import java.util.UUID;

public record EventAcceptedResponse(
    String status,
    UUID eventId
) {}
