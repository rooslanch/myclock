package com.example.factory;

import com.example.model.TimeStrategy;

public interface TimeStrategyFactory {
    TimeStrategy createSystemTime();
    TimeStrategy createStopwatch();
}
