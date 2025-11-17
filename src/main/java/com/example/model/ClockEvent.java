package com.example.model;
import java.time.LocalTime;

/**
 * Command / Event object — минимальный immutable объект для события.
 * timestampMillis — источник времени берётся из TimeStrategy.getCurrentTimeMillis()
 */
public final class ClockEvent {
    private final long timestampMillis;
    private final String message;

    public ClockEvent(long timestampMillis, String message) {
        this.timestampMillis = timestampMillis;
        this.message = message;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "[" + timestampMillis + "] " + message;
    }
}