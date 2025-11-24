package com.example.app;

import com.example.controller.ClockController;
import com.example.factory.TimeStrategyFactory;
import com.example.model.*;
import com.example.view.ClockPanel;
import com.example.view.LogWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainViewController {

    private final TimeStrategyFactory strategyFactory;

    private TimeStrategy systemTime;
    private TimeStrategy stopwatch;

    private ClockModel model;
    private ClockController controller;
    private ClockPanel clockPanel;
    private LogWindow logWindow;

    private JFrame frame;

    public MainViewController(TimeStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    public void start() {
        SwingUtilities.invokeLater(this::initUI);
    }

    private void initUI() {

        systemTime = strategyFactory.createSystemTime();
        stopwatch  = strategyFactory.createStopwatch();

        model = new ClockModel(systemTime, 20);

        clockPanel = new ClockPanel();
        logWindow = new LogWindow();
        clockPanel.setLogWindow(logWindow);

        model.addEventListener(clockPanel);
        model.addUiListener(clockPanel);

        controller = new ClockController(model, 200, true);

        controller.addDataListener((ts, msg) ->
                logWindow.log("[As DataListener] Tick: " + msg + " @ " + ts));

        frame = new JFrame("MyClock");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(clockPanel, BorderLayout.CENTER);
        frame.add(createControlPanel(), BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.shutdown();
            }
        });

        controller.start();
    }

    private JPanel createControlPanel() {
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
