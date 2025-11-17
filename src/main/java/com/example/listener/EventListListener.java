package com.example.listener;

import com.example.model.ClockEvent;

public interface EventListListener {
    void onEventAdded(ClockEvent e);
    void onEventRemoved(ClockEvent e);
}