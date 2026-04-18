package com.github.nfmdev.candlestack.processing_service.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "instrument_snapshot")
public class InstrumentSnapshotEntity {

    @Id
    @Column(name = "instrument_id", nullable = false, length = 64)
    private String instrumentId;

    @Column(name = "last_event_id", nullable = false)
    private UUID lastEventId;

    @Column(name = "trading_date", nullable = false)
    private LocalDate tradingDate;

    @Column(name = "last_price", nullable = false, precision = 19, scale = 8)
    private BigDecimal lastPrice;

    @Column(name = "last_quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal lastQuantity;

    @Column(name = "last_event_time", nullable = false)
    private Instant lastEventTime;

    @Column(name = "day_volume", nullable = false, precision = 19, scale = 8)
    private BigDecimal dayVolume;

    @Column(name = "day_high", nullable = false, precision = 19, scale = 8)
    private BigDecimal dayHigh;

    @Column(name = "day_low", nullable = false, precision = 19, scale = 8)
    private BigDecimal dayLow;

    @Column(name = "trade_count", nullable = false)
    private long tradeCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "currency", nullable = false, length = 16)
    private String currency;

    public InstrumentSnapshotEntity() {
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public UUID getLastEventId() {
        return lastEventId;
    }

    public void setLastEventId(UUID lastEventId) {
        this.lastEventId = lastEventId;
    }

    public LocalDate getTradingDate() {
        return tradingDate;
    }

    public void setTradingDate(LocalDate tradingDate) {
        this.tradingDate = tradingDate;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public BigDecimal getLastQuantity() {
        return lastQuantity;
    }

    public void setLastQuantity(BigDecimal lastQuantity) {
        this.lastQuantity = lastQuantity;
    }

    public Instant getLastEventTime() {
        return lastEventTime;
    }

    public void setLastEventTime(Instant lastEventTime) {
        this.lastEventTime = lastEventTime;
    }

    public BigDecimal getDayVolume() {
        return dayVolume;
    }

    public void setDayVolume(BigDecimal dayVolume) {
        this.dayVolume = dayVolume;
    }

    public BigDecimal getDayHigh() {
        return dayHigh;
    }

    public void setDayHigh(BigDecimal dayHigh) {
        this.dayHigh = dayHigh;
    }

    public BigDecimal getDayLow() {
        return dayLow;
    }

    public void setDayLow(BigDecimal dayLow) {
        this.dayLow = dayLow;
    }

    public long getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(long tradeCount) {
        this.tradeCount = tradeCount;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}