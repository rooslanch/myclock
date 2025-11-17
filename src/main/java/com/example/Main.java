package com.example;

import com.example.controller.ClockController;
import com.example.listener.DataListener;
import com.example.model.*;
import com.example.view.ClockPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Точка входа. Создаёт модель, view (ClockPanel) подписывает её на модель,
 * создаёт контроллер и панель управления.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TimeStrategy systemTime = new SystemTimeStrategy();
            TimeStrategy stopwatch = new StopwatchStrategy();

            ClockModel model = new ClockModel(systemTime, 20);

            ClockPanel panel = new ClockPanel();
            // Подписываем view на модель (вызовет initial snapshot)
            model.addUiListener(panel);

            ClockController controller = new ClockController(model, 200, true);

            // дополнительный DataListener для логирования
            controller.addDataListener(new DataListener() {
                @Override
                public void onNewValue(long currentTimeMillis, String message) {
                    System.out.printf("DataListener: %s @ %d%n", message, currentTimeMillis);
                }
            });

            JFrame frame = new JFrame("ЛР3 — Чистый MVC + Observer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);
            frame.add(createControlPanel(controller, model, systemTime, stopwatch), BorderLayout.SOUTH);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // корректное завершение
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    controller.shutdown();
                }
            });

            controller.start();
        });
    }

    private static JPanel createControlPanel(ClockController controller, ClockModel model,
                                             TimeStrategy systemTime, TimeStrategy stopwatch) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));

        JButton startBtn = new JButton("▶ Старт");
        JButton stopBtn = new JButton("⏸ Стоп");
        JButton resetBtn = new JButton("⟲ Сброс");
        JButton toggleTicks = new JButton("Тики: вкл");
        JButton modeBtn = new JButton("Режим: Часы");

        startBtn.addActionListener(e -> {
            controller.strategyStart();
            controller.start();
        });

        stopBtn.addActionListener(e -> {
            controller.strategyStop();
            controller.stop();
        });

        resetBtn.addActionListener(e -> {
            controller.strategyReset();
            model.clearEvents();
        });

        toggleTicks.addActionListener(e -> {
            boolean currentlyEmit = toggleTicks.getText().contains("вкл");
            controller.setEmitTicks(!currentlyEmit);
            toggleTicks.setText(currentlyEmit ? "Тики: выкл" : "Тики: вкл");
        });

        modeBtn.addActionListener(e -> {
            TimeStrategy current = model.getStrategy();
            if (current instanceof SystemTimeStrategy) {
                controller.setStrategy(stopwatch, true);
                modeBtn.setText("Режим: Секундомер");
            } else {
                controller.setStrategy(systemTime, false);
                modeBtn.setText("Режим: Часы");
            }
        });

        panel.add(startBtn);
        panel.add(stopBtn);
        panel.add(resetBtn);
        panel.add(toggleTicks);
        panel.add(modeBtn);

        return panel;
    }
}