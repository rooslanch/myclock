package com.example.model;
import java.time.LocalTime;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class StopwatchStrategy implements TimeStrategy {
    // baseNano - момент старта в System.nanoTime()
    private final AtomicLong baseNano = new AtomicLong(0);
    private final AtomicLong pausedElapsedNano = new AtomicLong(0);
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public long getCurrentTimeMillis() {
        long elapsedNano = pausedElapsedNano.get();
        if (running.get()) {
            elapsedNano += System.nanoTime() - baseNano.get();
        }
        return elapsedNano / 1_000_000L;
    }

    @Override
    public String getDisplayTime() {
        long ms = getCurrentTimeMillis();
        long seconds = (ms / 1000) % 60;
        long minutes = (ms / (1000 * 60)) % 60;
        long hours = (ms / (1000 * 60 * 60));
        long millis = ms % 1000;
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            baseNano.set(System.nanoTime());
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            long elapsedSinceBase = System.nanoTime() - baseNano.get();
            pausedElapsedNano.addAndGet(elapsedSinceBase);
        }
    }

    @Override
    public void reset() {
        running.set(false);
        baseNano.set(0);
        pausedElapsedNano.set(0);
    }
    @Override
    public String getModeLabel() {
        return "Секундомер";
    }
}
