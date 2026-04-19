package com.github.nfmdev.candlestack.processing_service.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.github.nfmdev.candlestack.processing_service.application.TradeProcessingService;
import com.github.nfmdev.candlestack.processing_service.domain.event.MarketTradeEvent;
import com.github.nfmdev.candlestack.processing_service.support.exception.InvalidTradeEventException;

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
            containerFactory = "marketTradeKafkaListenerContainerFactory",
            autoStartup = "false"
    )
    public void onMessage(
            ConsumerRecord<String, MarketTradeEvent> record,
            Acknowledgment acknowledgment
    ) {
        MarketTradeEvent event = record.value();
        try {
            tradeProcessingService.process(event);
            acknowledgment.acknowledge();

            log.debug(
                    "Kafka message processed and acknowledged. topic={}, partition={}, offset={}, eventId={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    event != null ? event.eventId() : null
            );
        } catch (InvalidTradeEventException ex) {
            acknowledgment.acknowledge();
            log.warn(
                "Invalid trade event discarded and acknowledged. topic={}, partition={}, offset={}, reason={}",
                record.topic(),
                record.partition(),
                record.offset(),
                ex.getMessage()
            );
        } catch (DataAccessException ex) {
            log.error(
                "Database error while processing trade event. topic={}, partition={}, offset={}, eventId={}",
                record.topic(),
                record.partition(),
                record.offset(),
                event != null ? event.eventId() : null,
                ex
            );
            throw ex;
        } catch (Exception ex) {
            log.error(
                "Unexpected error while processing trade event. topic={}, partition={}, offset={}, eventId={}",
                record.topic(),
                record.partition(),
                record.offset(),
                event != null ? event.eventId() : null,
                ex
            );
            throw ex;
        }
    }
}
