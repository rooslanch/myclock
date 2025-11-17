package com.example.model;

public interface TimeStrategy {
    /**
     * Текущее значение времени в миллисекундах.
     * Для системного времени — epoch milli (System.currentTimeMillis()).
     * Для секундомера — прошедшие миллисекунды с момента старта.
     */
    long getCurrentTimeMillis();
    /**
     * Читабельное строковое представление для показа.
     */
    String getDisplayTime();
    /**
     * Запустить/остановить стратегию (для секундомера).
     */
    default void start() {}
    default void stop() {}
    default void reset() {}
    default String getModeLabel() { return "Time"; }
}
