package com.example.model;

import com.example.listener.ClockModelListener;
import com.example.listener.DataListener;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Модель — хранит стратегию времени, очередь событий и список слушателей (UI и внешние).
 * Вызовы слушателей, относящиеся к UI, выполняются в EDT через SwingUtilities.invokeLater.
 */
public class ClockModel {

    private final Deque<ClockEvent> events = new ArrayDeque<>();
    private final List<ClockModelListener> uiListeners = new CopyOnWriteArrayList<>();
    private final List<DataListener> dataListeners = new CopyOnWriteArrayList<>();
    private volatile TimeStrategy strategy;
    private final int maxEvents;

    public ClockModel(TimeStrategy initialStrategy, int maxEvents) {
        this.strategy = Objects.requireNonNull(initialStrategy);
        this.maxEvents = Math.max(1, maxEvents);
    }

    // ---------------- listener management ----------------
    public void addUiListener(ClockModelListener l) {
        uiListeners.add(Objects.requireNonNull(l));
        // при подписке сразу отправляем "снимок" состояния (в EDT)
        SwingUtilities.invokeLater(() -> {
            l.onModeChanged(strategy.getModeLabel());
            l.onTimeUpdated(strategy.getCurrentTimeMillis());
            l.onEventsUpdated(Collections.unmodifiableList(new ArrayList<>(events)));
        });
    }

    public void removeUiListener(ClockModelListener l) {
        uiListeners.remove(l);
    }

    public void addDataListener(DataListener l) {
        dataListeners.add(Objects.requireNonNull(l));
    }

    public void removeDataListener(DataListener l) {
        dataListeners.remove(l);
    }

    // ---------------- core model operations ----------------

    /**
     * Обновить "текущее время" из стратегии и уведомить UI.
     * Возвращает значение времени.
     */
    public long updateTimeFromStrategy() {
        long ts = strategy.getCurrentTimeMillis();
        notifyTimeUpdated(ts);
        return ts;
    }

    /**
     * Добавляет событие (источник времени — текущая стратегия).
     */
    public synchronized ClockEvent addEvent(String message) {
        long ts = strategy.getCurrentTimeMillis();
        ClockEvent event = new ClockEvent(ts, message != null ? message : "");
        if (events.size() >= maxEvents) {
            events.removeFirst();
        }
        events.addLast(event);
        notifyEventsChanged();
        notifyDataListeners(event);
        return event;
    }

    /**
     * Возвращает копию текущего списка событий — для тестирования/вне-UI использования.
     * (View не должен вызывать это, но оставлено для других подсистем)
     */
    public synchronized List<ClockEvent> getEventsCopy() {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }

    public synchronized void clearEvents() {
        events.clear();
        notifyEventsChanged();
    }

    /**
     * Плавно сменить стратегию: модель обновляет стратегию и уведомляет UI о смене режима и текущем времени.
     * Если newStrategy поддерживает start() — контроллер решает, запускать ли её.
     */
    public synchronized void setStrategy(TimeStrategy newStrategy) {
        this.strategy = Objects.requireNonNull(newStrategy);
        // уведомим UI о смене режима и текущем времени/событиях
        SwingUtilities.invokeLater(() -> {
            for (ClockModelListener l : uiListeners) {
                l.onModeChanged(strategy.getModeLabel());
                l.onTimeUpdated(strategy.getCurrentTimeMillis());
                l.onEventsUpdated(Collections.unmodifiableList(new ArrayList<>(events)));
            }
        });
    }

    public TimeStrategy getStrategy() {
        return this.strategy;
    }

    // ---------------- notification helpers ----------------

    private void notifyTimeUpdated(long ts) {
        // объединяем уведомления в один Runnable для уменьшения количества вызовов enqueue
        List<ClockModelListener> snapshot = new ArrayList<>(uiListeners);
        SwingUtilities.invokeLater(() -> {
            for (ClockModelListener l : snapshot) {
                try {
                    l.onTimeUpdated(ts);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void notifyEventsChanged() {
        List<ClockModelListener> snapshot = new ArrayList<>(uiListeners);
        List<ClockEvent> eventsCopy;
        synchronized (this) {
            eventsCopy = Collections.unmodifiableList(new ArrayList<>(events));
        }
        SwingUtilities.invokeLater(() -> {
            for (ClockModelListener l : snapshot) {
                try {
                    l.onEventsUpdated(eventsCopy);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void notifyDataListeners(ClockEvent e) {
        for (DataListener dl : dataListeners) {
            try {
                dl.onNewValue(e.getTimestampMillis(), e.getMessage());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }
}
