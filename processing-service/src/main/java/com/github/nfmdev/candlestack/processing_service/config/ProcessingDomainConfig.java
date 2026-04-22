package com.github.nfmdev.candlestack.processing_service.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.nfmdev.candlestack.processing_service.domain.mapper.TradeEventMapper;
import com.github.nfmdev.candlestack.processing_service.domain.service.InstrumentNormalizer;
import com.github.nfmdev.candlestack.processing_service.domain.service.InstrumentSnapshotCalculator;
import com.github.nfmdev.candlestack.processing_service.domain.validation.TradeEventValidator;

@Configuration
public class ProcessingDomainConfig {
    @Bean
    public Clock processingClock() {
        return Clock.systemUTC();
    }

    @Bean
    public InstrumentNormalizer instrumentNormalizer() {
        return new InstrumentNormalizer();
    }

    @Bean
    public TradeEventMapper tradeEventMapper(InstrumentNormalizer instrumentNormalizer) {
        return new TradeEventMapper(instrumentNormalizer);
    }

    @Bean
    public TradeEventValidator tradeEventValidator() {
        return new TradeEventValidator();
    }

    @Bean
    public InstrumentSnapshotCalculator instrumentSnapshotCalculator(Clock processingClock) {
        return new InstrumentSnapshotCalculator(processingClock);
    }
}
