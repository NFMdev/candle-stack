package com.github.nfmdev.candlestack.generator_service.domain.engine.payload;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.github.nfmdev.candlestack.generator_service.domain.engine.inter.PayloadGenerator;

@Component 
public final class RandomAsciiPayloadGenerator implements PayloadGenerator {
    private static final char[] CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static RandomAsciiPayloadGenerator instance;
    private final ConcurrentMap<Integer, String> cache = new ConcurrentHashMap<>();

    private final Random random;

    private RandomAsciiPayloadGenerator() {
        this.random = new Random();
    }

    public static RandomAsciiPayloadGenerator getInstance() {
        if (instance == null) {
            instance = new RandomAsciiPayloadGenerator();
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
        char[] payload = new char[sizeBytes];

        for (int i = 0; i < sizeBytes; i++) {
            payload[i] = CHARACTERS[random.nextInt(CHARACTERS.length)];
        }

        return new String(payload);
    }
}
