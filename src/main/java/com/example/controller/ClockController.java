package com.example.controller;

import com.example.listener.DataListener;
import com.example.model.ClockModel;
import com.example.model.TimeStrategy;
import com.example.view.ClockPanel;
import javax.swing.*;
import java.util.concurrent.*;

/**
 * Контроллер — управляет периодическим опросом модели (через strategy),
 * а также переключением режимов и запросами добавления событий.
 *
 * Важно: контроллер не дергает view напрямую — view подписаны на модель.
 */
public class ClockController {

    private final ClockModel model;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> task;
    private final long periodMillis;
    private volatile boolean emitTicks;

    public ClockController(ClockModel model, long periodMillis, boolean emitTicks) {
        this.model = model;
        this.periodMillis = Math.max(10, periodMillis);
        this.emitTicks = emitTicks;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "clock-ticker");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        if (task != null && !task.isCancelled() && !task.isDone()) return;
        task = scheduler.scheduleAtFixedRate(() -> {
            long t = model.updateTimeFromStrategy(); // модель уведомит UI
            if (emitTicks) {
                model.addEvent("Tick");
            }
            // NB: model.updateTimeFromStrategy() уже вызывает SwingUtilities.invokeLater для UI listeners
        }, 0, periodMillis, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (task != null) {
            task.cancel(false);
            task = null;
        }
    }

    public void shutdown() {
        stop();
        scheduler.shutdownNow();
    }

    public void setEmitTicks(boolean emitTicks) {
        this.emitTicks = emitTicks;
    }

    // переключение стратегии: останавливаем тикер, переключаем, опционально стартуем стратегию, возобновляем тикер
    public void setStrategy(TimeStrategy newStrategy, boolean startStrategy) {
        stop();
        model.setStrategy(newStrategy);
        if (startStrategy) newStrategy.start();
        // в модели при смене стратегии уже отправлены notifications
        start();
    }

    // пользовательские команды к стратегии (start/stop/reset) делаем через модель.getStrategy()
    public void strategyStart() {
        TimeStrategy s = model.getStrategy();
        s.start();
    }

    public void strategyStop() {
        TimeStrategy s = model.getStrategy();
        s.stop();
    }

    public void strategyReset() {
        TimeStrategy s = model.getStrategy();
        s.reset();
    }

    // проксирование для внешних DataListener (если нужно)
    public void addDataListener(DataListener l) {
        model.addDataListener(l);
    }

    public void removeDataListener(DataListener l) {
        model.removeDataListener(l);
    }
}