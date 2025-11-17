package com.example.view;


import com.example.listener.ClockModelListener;
import com.example.model.ClockModel;
import com.example.model.ClockEvent;
import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Deque;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * View: не трогает модель напрямую; получает данные через ClockModelListener.
 */
public class ClockPanel extends JPanel implements ClockModelListener {

    // локальный снимок состояния (всё, что нужно для отрисовки)
    private volatile long currentTimeMillis = 0;
    private volatile List<ClockEvent> events = List.of();
    private volatile String modeLabel = "";
    private final DateTimeFormatter digitalFormatter =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public ClockPanel() {
        setPreferredSize(new Dimension(350, 350));
        setBackground(Color.WHITE);
        setDoubleBuffered(true);
    }

    // ---------------- ClockModelListener ----------------
    @Override
    public void onTimeUpdated(long currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
        repaint();
    }

    @Override
    public void onEventsUpdated(java.util.List<ClockEvent> eventsCopy) {
        this.events = eventsCopy;
        repaint();
    }

    @Override
    public void onModeChanged(String modeLabel) {
        this.modeLabel = modeLabel;
        repaint();
    }

    // ---------------- painting ----------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int radius = Math.min(cx, cy) - 20;

            // фон циферблата
            g2.setColor(new Color(240, 240, 240));
            g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);

            // деления
            for (int i = 0; i < 60; i++) {
                double angle = Math.toRadians(i * 6 - 90);
                int inner = (i % 5 == 0) ? (int) (radius * 0.85) : (int) (radius * 0.92);
                int x1 = cx + (int) (Math.cos(angle) * inner);
                int y1 = cy + (int) (Math.sin(angle) * inner);
                int x2 = cx + (int) (Math.cos(angle) * radius);
                int y2 = cy + (int) (Math.sin(angle) * radius);
                g2.drawLine(x1, y1, x2, y2);
            }

            // стрелка (используем локальный currentTimeMillis — взят из модели через слушатель)
            double secondsWithFrac = (currentTimeMillis % 60000L) / 1000.0; // 0..59.999
            double angle = Math.toRadians((secondsWithFrac / 60.0) * 360 - 90);

            int handLength = (int) (radius * 0.8);
            int x2 = cx + (int) (Math.cos(angle) * handLength);
            int y2 = cy + (int) (Math.sin(angle) * handLength);

            g2.setStroke(new BasicStroke(3f));
            g2.setColor(new Color(40, 40, 40));
            g2.drawLine(cx, cy, x2, y2);

            // центр
            g2.fillOval(cx - 4, cy - 4, 8, 8);

            // метки событий (используем локальную копию events)
            if ("Часы".equals(modeLabel)) {
                g2.setColor(new Color(40, 40, 40)); // серый
            } else {
                g2.setColor(new Color(40, 40, 200)); // синий
            }
            for (ClockEvent e : events) {
                long ts = e.getTimestampMillis();
                double secFrac = (ts % 60000L) / 1000.0;
                double a = Math.toRadians((secFrac / 60.0) * 360 - 90);
                int ex = cx + (int) (Math.cos(a) * (radius * 0.9));
                int ey = cy + (int) (Math.sin(a) * (radius * 0.9));
                g2.fillOval(ex - 4, ey - 4, 8, 8);
            }

            // цифровое время + режим
            g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
            String digital = renderDigital();
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(digital);
            g2.setColor(Color.BLACK);
            g2.drawString(digital, cx - textWidth / 2, cy + radius + fm.getAscent());

            // режим (небольшой текст)
            g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            String mode = modeLabel != null ? modeLabel : "";
            g2.drawString(mode, 8, 16);
        } finally {
            g2.dispose();
        }
    }

    private String renderDigital() {
        if ("Часы".equals(modeLabel)) {
            // системное время
            ZonedDateTime zdt = Instant.ofEpochMilli(currentTimeMillis)
                    .atZone(ZoneId.systemDefault());
            return zdt.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        } else {
            // секундомер — длительность от 0
            long ms = currentTimeMillis;
            long hours = ms / (1000L * 60 * 60);
            long minutes = (ms / (1000L * 60)) % 60;
            long seconds = (ms / 1000L) % 60;
            long millis = ms % 1000;
            return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
        }
    }
}

