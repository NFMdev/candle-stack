package com.github.nfmdev.candlestack.processing_service.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import com.github.nfmdev.candlestack.processing_service.domain.event.MarketTradeEvent;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, MarketTradeEvent> marketTradeConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);

        JacksonJsonDeserializer<MarketTradeEvent> valueDeserializer = new JacksonJsonDeserializer<>(MarketTradeEvent.class);

        valueDeserializer.ignoreTypeHeaders();
        valueDeserializer.trustedPackages("com.github.nfmdev.candlestasck.processing_service.domain.event");

        return new DefaultKafkaConsumerFactory<>(
            props,
            new StringDeserializer(),
            valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MarketTradeEvent> marketTradeKafkaListenerContainerFactory(
        ConsumerFactory<String, MarketTradeEvent> marketTradeConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, MarketTradeEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(marketTradeConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setConcurrency(1);

        return factory;
    }
}