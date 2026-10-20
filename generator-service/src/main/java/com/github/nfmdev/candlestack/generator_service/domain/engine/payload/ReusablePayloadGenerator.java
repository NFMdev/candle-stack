package com.github.nfmdev.candlestack.generator_service.domain.engine.payload;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.github.nfmdev.candlestack.generator_service.domain.engine.inter.PayloadGenerator;

@Component 
@Primary
public final class ReusablePayloadGenerator implements PayloadGenerator {
    private static final String PATTERN = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static ReusablePayloadGenerator instance;
    private final ConcurrentMap<Integer, String> cache = new ConcurrentHashMap<>();

    private ReusablePayloadGenerator() {}

    public static ReusablePayloadGenerator getInstance() {
        if (instance == null) {
            instance = new ReusablePayloadGenerator();
        }

        return instance;
    }

    @Override
    public String generate(int sizeBytes) {
        return cache.computeIfAbsent(
            sizeBytes,
            this::createPayload
        );
    }

    private String createPayload(int sizeBytes) {
        StringBuilder builder =
            new StringBuilder(sizeBytes);

        while (builder.length() < sizeBytes) {
            int remaining =
                sizeBytes - builder.length();

            builder.append(
                PATTERN,
                0,
                Math.min(remaining, PATTERN.length())
            );
        }

        return builder.toString();
    }
}
