package com.example.listener;


/**
 * Интерфейс слушателя модели (View подписывается на модель).
 * Все методы вызываются в EDT (модель гарантирует вызов через SwingUtilities.invokeLater).
 */
public interface UIListener {
    /**
     * При изменении "текущего времени" (например, тик)
     * @param currentTimeMillis значение времени, берётся из TimeStrategy
     */
    void onTimeUpdated(long currentTimeMillis);

    /**
     * При изменении списка событий — передаётся неизменяемая копия списка.
     * @param eventsCopy копия списка событий (List<ClockEvent>), может быть пустой
     */
    void onEventsUpdated(java.util.List<com.example.model.ClockEvent> eventsCopy);

    /**
     * При смене режима/стратегии (например: "Часы", "Секундомер")
     * @param modeLabel короткая текстовая метка режима
     */
    void onModeChanged(String modeLabel);
}

