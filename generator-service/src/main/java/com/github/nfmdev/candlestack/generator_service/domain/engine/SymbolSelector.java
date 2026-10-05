package com.github.nfmdev.candlestack.generator_service.domain.engine;

import com.github.nfmdev.candlestack.generator_service.domain.model.SymbolConfig;

public interface SymbolSelector {
    SymbolConfig next();
}
