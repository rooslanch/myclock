package com.example.factory;

import com.example.model.StopwatchStrategy;
import com.example.model.SystemTimeStrategy;
import com.example.model.TimeStrategy;

public class DefaultTimeStrategyFactory implements TimeStrategyFactory {

    @Override
    public TimeStrategy createSystemTime() {
        return new SystemTimeStrategy();
    }

    @Override
    public TimeStrategy createStopwatch() {
        return new StopwatchStrategy();
    }
}
