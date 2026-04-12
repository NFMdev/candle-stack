package com.github.nfmdev.candlestack.ingestion_service.infrastructure.kafka;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.github.nfmdev.candlestack.ingestion_service.config.KafkaTopicsProperties;
import com.github.nfmdev.candlestack.ingestion_service.domain.exception.EventPublishingException;
import com.github.nfmdev.candlestack.ingestion_service.domain.model.MarketTradeEvent;
import com.github.nfmdev.candlestack.ingestion_service.domain.ports.MarketEventPublisherPort;

@Component
public class KafkaMarketEventPublisher implements MarketEventPublisherPort {
    private static final long SEND_TIMEOUT_SECONDS = 5L;

    private final KafkaTemplate<String, MarketTradeEvent> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    public KafkaMarketEventPublisher(
        KafkaTemplate<String, MarketTradeEvent> kafkaTemplate,
        KafkaTopicsProperties kafkaTopicsProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTopicsProperties = kafkaTopicsProperties;
    }

    @Override
    public void publish(MarketTradeEvent event) {
        try {
            kafkaTemplate.send(
                kafkaTopicsProperties.marketTrades(),
                event.symbol(),
                event
            ).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new EventPublishingException("Publishing interrupted", ex);
        } catch (ExecutionException | TimeoutException | KafkaException ex) {
            throw new EventPublishingException("Failed to publish event to Kafka", ex);
        }
    }
}
