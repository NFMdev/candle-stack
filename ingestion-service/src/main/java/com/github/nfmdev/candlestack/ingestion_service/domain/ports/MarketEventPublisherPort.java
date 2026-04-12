package com.github.nfmdev.candlestack.ingestion_service.domain.ports;

import com.github.nfmdev.candlestack.ingestion_service.domain.model.MarketTradeEvent;

public interface MarketEventPublisherPort {
    void publish(MarketTradeEvent event);
}
