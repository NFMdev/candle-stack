package com.github.nfmdev.candlestack.ingestion_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "candlestack.kafka.topics")
public record KafkaTopicsProperties(
    String marketTrades
) {
    public KafkaTopicsProperties {
        if (marketTrades == null || marketTrades.isBlank()) {
            throw new IllegalArgumentException("candlestack.kafka.topics.market-trades cannot be blank");
        }
    }
}
