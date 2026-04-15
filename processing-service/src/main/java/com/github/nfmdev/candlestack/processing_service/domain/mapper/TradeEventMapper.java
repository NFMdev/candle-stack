package com.github.nfmdev.candlestack.processing_service.domain.mapper;

import com.github.nfmdev.candlestack.processing_service.domain.event.MarketTradeEvent;
import com.github.nfmdev.candlestack.processing_service.domain.event.TradeEvent;

public class TradeEventMapper {
    

    public MarketTradeEvent toMarketTradeEvent(TradeEvent event) {
        return new MarketTradeEvent(
            
        );
    }
}