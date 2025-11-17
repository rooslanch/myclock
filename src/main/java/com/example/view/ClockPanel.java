package com.example.view;

import com.example.listener.ClockModelListener;
import com.example.listener.EventListListener;
import com.example.model.ClockEvent;
import com.example.model.ClockModel;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClockPanel extends JPanel implements ClockModelListener, EventListListener {

    private volatile long currentTimeMillis = 0;
    private volatile String modeLabel = "";
    private final List<ClockEvent> displayEvents = new ArrayList<>();
    private final DateTimeFormatter digitalFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public ClockPanel() {
        setPreferredSize(new Dimension(350, 350));
        setBackground(Color.WHITE);
        setDoubleBuffered(true);
    }

    @Override
    public void onTimeUpdated(long currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
        repaint();
    }
    /**
     * Этот метод не используется. Однако если понадобится обновлять список событий через UI listener,
     * то это может пригодиться. В текущей реализации через UI listener передается только время и режим.
     */
    @Override
    public void onEventsUpdated(List<ClockEvent> eventsCopy) {
        synchronized (displayEvents) {
            displayEvents.clear();
            displayEvents.addAll(eventsCopy);
            System.out.println("onEventsUpdated (as UI listener): displaying full-copied events");
        }
        repaint();
    }

    @Override
    public void onModeChanged(String modeLabel) {
        this.modeLabel = modeLabel;
        repaint();
    }



    @Override
    public void onEventAdded(ClockEvent e) {
        synchronized (displayEvents) { displayEvents.add(e); }
        repaint();
        System.out.println("onEventAdded (as an EVENT listener): displaying delta-events");
    }

    @Override
    public void onEventRemoved(ClockEvent e) {
        synchronized (displayEvents) { displayEvents.remove(e); }
        repaint();
    }


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

            // стрелка
            double secondsWithFrac = (currentTimeMillis % 60000L) / 1000.0;
            double angle = Math.toRadians((secondsWithFrac / 60.0) * 360 - 90);
            int handLength = (int) (radius * 0.8);
            int x2 = cx + (int) (Math.cos(angle) * handLength);
            int y2 = cy + (int) (Math.sin(angle) * handLength);

            g2.setStroke(new BasicStroke(3f));
            g2.setColor("Часы".equals(modeLabel) ? new Color(40, 40, 40) : new Color(40, 40, 200));
            g2.drawLine(cx, cy, x2, y2);

            g2.fillOval(cx - 4, cy - 4, 8, 8);

            // метки событий
            g2.setColor(new Color(200, 50, 50));
            synchronized (displayEvents) {
                for (ClockEvent e : displayEvents) {
                    long ts = e.getTimestampMillis();
                    double secFrac = (ts % 60000L) / 1000.0;
                    double a = Math.toRadians((secFrac / 60.0) * 360 - 90);
                    int ex = cx + (int) (Math.cos(a) * (radius * 0.9));
                    int ey = cy + (int) (Math.sin(a) * (radius * 0.9));
                    g2.fillOval(ex - 4, ey - 4, 8, 8);
                }
            }

            // цифровое время
            g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
            String digital = renderDigital();
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(digital);
            g2.setColor(Color.BLACK);
            g2.drawString(digital, cx - textWidth / 2, cy + radius + fm.getAscent());

            // режим
            g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            g2.drawString(modeLabel != null ? modeLabel : "", 8, 16);

        } finally {
            g2.dispose();
        }
    }

    private String renderDigital() {
        if ("Часы".equals(modeLabel)) {
            ZonedDateTime zdt = Instant.ofEpochMilli(currentTimeMillis).atZone(ZoneId.systemDefault());
            return zdt.format(digitalFormatter);
        } else {
            long ms = currentTimeMillis;
            long hours = ms / (1000L * 60 * 60);
            long minutes = (ms / (1000L * 60)) % 60;
            long seconds = (ms / 1000) % 60;
            long millis = ms % 1000;
            return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
        }
    }
}
