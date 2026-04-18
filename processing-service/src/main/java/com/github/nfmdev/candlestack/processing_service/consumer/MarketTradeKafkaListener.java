package com.github.nfmdev.candlestack.processing_service.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.github.nfmdev.candlestack.processing_service.application.TradeProcessingService;
import com.github.nfmdev.candlestack.processing_service.domain.event.MarketTradeEvent;

@Component
public class MarketTradeKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(MarketTradeKafkaListener.class);

    private final TradeProcessingService tradeProcessingService;

    public MarketTradeKafkaListener(TradeProcessingService tradeProcessingService) {
        this.tradeProcessingService = tradeProcessingService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.market-trades}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "marketTradeKafkaListenerContainerFactory"
    )
    public void onMessage(
            ConsumerRecord<String, MarketTradeEvent> record,
            Acknowledgment acknowledgment
    ) {
        MarketTradeEvent event = record.value();

        tradeProcessingService.process(event);
        acknowledgment.acknowledge();

        log.debug(
                "Kafka message processed and acknowledged. topic={}, partition={}, offset={}, eventId={}",
                record.topic(),
                record.partition(),
                record.offset(),
                event != null ? event.eventId() : null
        );
    }
}
