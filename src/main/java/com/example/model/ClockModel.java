package com.example.model;

import com.example.listener.UIListener;
import com.example.listener.DataListener;
import com.example.listener.EventListListener;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClockModel {

    private final Deque<ClockEvent> events = new ArrayDeque<>();
    private final List<UIListener> uiListeners = new CopyOnWriteArrayList<>();
    private final List<DataListener> dataListeners = new CopyOnWriteArrayList<>();
    private final List<EventListListener> eventListeners = new CopyOnWriteArrayList<>();

    private volatile TimeStrategy strategy; //volatile чтобы было видно изменение стратегии
    private final int maxEvents;

    public ClockModel(TimeStrategy initialStrategy, int maxEvents) {
        this.strategy = Objects.requireNonNull(initialStrategy);
        this.maxEvents = Math.max(1, maxEvents);
    }

    // ---------------- listener management ----------------
    public void addUiListener(UIListener l) {
        uiListeners.add(Objects.requireNonNull(l));
        SwingUtilities.invokeLater(() -> {
            l.onModeChanged(strategy.getModeLabel());
            l.onTimeUpdated(strategy.getCurrentTimeMillis());
//            l.onEventsUpdated(Collections.unmodifiableList(new ArrayList<>(events))); // передача событий НЕ через UI listener
        });
    }

    public void removeUiListener(UIListener l) {
        uiListeners.remove(l);
    }

    public void addDataListener(DataListener l) {
        dataListeners.add(Objects.requireNonNull(l));
    }

    public void removeDataListener(DataListener l) {
        dataListeners.remove(l);
    }

    public void addEventListener(EventListListener l) {
        eventListeners.add(Objects.requireNonNull(l));
        // hot swap: сразу отправляем все существующие события
        System.out.println("addEventListener (as an EVENT listener): displaying full-copied events");
        List<ClockEvent> snapshot;
        synchronized (this) {
            snapshot = new ArrayList<>(events);
        }
        SwingUtilities.invokeLater(() -> {
            for (ClockEvent e : snapshot) {
                try { l.onEventAdded(e); } catch (Throwable ex) { ex.printStackTrace(); }
            }
        });
    }

    public void removeEventListener(EventListListener l) {
        eventListeners.remove(l);
    }

    // ---------------- core model operations ----------------
    public long updateTimeFromStrategy() {
        long ts = strategy.getCurrentTimeMillis();
        notifyTimeUpdated(ts);
        return ts;
    }

    public synchronized ClockEvent addEvent(String message) {
        long ts = strategy.getCurrentTimeMillis();
        ClockEvent event = new ClockEvent(ts, message != null ? message : "");

        ClockEvent removed = null;
        if (events.size() >= maxEvents) removed = events.removeFirst();
        events.addLast(event);

        final ClockEvent removedFinal = removed;
        final ClockEvent addedFinal = event;

        SwingUtilities.invokeLater(() -> {
            if (removedFinal != null) notifyEventRemoved(removedFinal);
            notifyEventAdded(addedFinal);
            notifyDataListeners(addedFinal);
        });

        return event;
    }

    public synchronized void clearEvents() {
        List<ClockEvent> removed;
        synchronized (this) {
            removed = new ArrayList<>(events);
            events.clear();
        }
        SwingUtilities.invokeLater(() -> {
            for (ClockEvent e : removed) notifyEventRemoved(e);
        });
    }

    public synchronized void setStrategy(TimeStrategy newStrategy) {
        this.strategy = Objects.requireNonNull(newStrategy);
        SwingUtilities.invokeLater(() -> {
            for (UIListener l : uiListeners) {
                l.onModeChanged(strategy.getModeLabel());
                l.onTimeUpdated(strategy.getCurrentTimeMillis());
            }
        });
    }

    public TimeStrategy getStrategy() { return this.strategy; }

    // ---------------- notification helpers ----------------
    private void notifyTimeUpdated(long ts) {
        List<UIListener> snapshot = new ArrayList<>(uiListeners);
        SwingUtilities.invokeLater(() -> {
            for (UIListener l : snapshot) {
                try { l.onTimeUpdated(ts); } catch (Throwable ex) { ex.printStackTrace(); }
            }
        });
    }

    private void notifyEventAdded(ClockEvent e) {
        for (EventListListener l : eventListeners) {
            try { l.onEventAdded(e); } catch (Throwable ex) { ex.printStackTrace(); }
        }
    }

    private void notifyEventRemoved(ClockEvent e) {
        for (EventListListener l : eventListeners) {
            try { l.onEventRemoved(e); } catch (Throwable ex) { ex.printStackTrace(); }
        }
    }

    private void notifyDataListeners(ClockEvent e) {
        for (DataListener dl : dataListeners) {
            try { dl.onNewValue(e.getTimestampMillis(), e.getMessage()); } catch (Throwable ex) { ex.printStackTrace(); }
        }
    }
    public void startStrategy() {
        synchronized (this) {
            strategy.start();
        }
    }

    public void stopStrategy() {
        synchronized (this) {
            strategy.stop();
        }
    }

    public void resetStrategy() {
        synchronized (this) {
            strategy.reset();
        }
    }

}
