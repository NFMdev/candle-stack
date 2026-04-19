package com.github.nfmdev.candlestack.processing_service.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class ProcessingStartupRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(ProcessingStartupRunner.class);

    private final SnapshotStateRehydrationService rehydrationService;
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    public ProcessingStartupRunner(
            SnapshotStateRehydrationService rehydrationService,
            KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry
    ) {
        this.rehydrationService = rehydrationService;
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting processing-service bootstrap sequence");

        rehydrationService.rehydrate();

        log.info("Starting Kafka listener containers after successful rehydration");
        kafkaListenerEndpointRegistry.start();

        log.info("Processing-service bootstrap sequence completed");
    }
}
