CREATE TABLE instrument_snapshot (
    instrument_id   VARCHAR(64)     PRIMARY KEY,
    last_event_id   UUID            NOT NULL,
    trading_date    DATE            NOT NULL,
    last_price      NUMERIC(19,8)   NOT NULL,
    last_quantity   NUMERIC(19,8)   NOT NULL,
    last_event_time TIMESTAMP      NOT NULL,
    day_volume      NUMERIC(19,8)   NOT NULL,
    day_high        NUMERIC(19,8)   NOT NULL,
    day_low         NUMERIC(19,8)   NOT NULL,
    trade_count     BIGINT          NOT NULL,
    updated_at      TIMESTAMP      NOT NULL,
    currency        VARCHAR(16)     NOT NULL

    CONSTRAINT chk_instrument_snapshot_last_price_positive
        CHECK (last_price > 0),
    CONSTRAINT chk_instrument_snapshot_last_quantity_positive
        CHECK (last_quantity > 0),
    CONSTRAINT chk_instrument_snapshot_day_volume_natural
        CHECK (day_volume >= 0),
    CONSTRAINT chk_instrument_snapshot_day_low_positive
        CHECK (day_low > 0),
    CONSTRAINT chk_instrument_snapshot_trade_count_non_negative
        CHECK (trade_count >= 0),
    CONSTRAINT chk_instrument_snapshot_day_high_ge_day_low
        CHECK (day_high >= day_low)
);

CREATE INDEX idx_instrument_snapshot_updated_at
    ON instrument_snapshot (updated_at);