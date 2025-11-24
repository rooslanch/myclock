package com.example.view;

import javax.swing.*;
import java.awt.*;

public class LogWindow extends JFrame {

    private final JTextArea textArea = new JTextArea();

    public LogWindow() {
        super("Log");

        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(textArea);

        add(scroll, BorderLayout.CENTER);

        setSize(600, 400);
        setLocation(100, 100);
        setVisible(true);
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(message + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }
}
