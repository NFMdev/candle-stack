package com.github.nfmdev.candlestack.generator_service.runtime;

import java.util.List;
import java.util.Random;

import com.github.nfmdev.candlestack.generator_service.domain.engine.SymbolSelector;
import com.github.nfmdev.candlestack.generator_service.domain.model.SymbolConfig;

public final class WeightedRandomSymbolSelector implements SymbolSelector {
    private final List<SymbolConfig> symbols;
    private final double[] cumulativeWeights;
    private final double totalWeight;
    private final Random random;

    public WeightedRandomSymbolSelector(List<SymbolConfig> symbols, Random random) {
        if (symbols.isEmpty()) {
            throw new IllegalArgumentException("At least one symbol is required.");
        }

        this.symbols = List.copyOf(symbols);
        this.random = random;
        this.cumulativeWeights = new double[symbols.size()];

        var cumulative = 0.0;

        for (int i = 0; i < symbols.size(); i++) {
            SymbolConfig symbol = symbols.get(i);

            if (symbol.loadWeight() <= 0.0) {
                throw new IllegalArgumentException("Symbol load weight must be > 0.");
            }

            cumulative += symbol.loadWeight();
            cumulativeWeights[i] = cumulative;
        }

        this.totalWeight = cumulative;
    }

    @Override
    public SymbolConfig next() {
        double target = random.nextDouble() * totalWeight;

        for (int i = 0; i < cumulativeWeights.length; i++) {
            if (target < cumulativeWeights[i]) {
                return symbols.get(i);
            }
        }

        return symbols.getLast();
    }
    
}
