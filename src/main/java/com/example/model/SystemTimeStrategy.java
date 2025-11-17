package com.example.model;

import java.time.LocalTime;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SystemTimeStrategy implements TimeStrategy {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    @Override
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public String getDisplayTime() {
        return FORMATTER.format(Instant.ofEpochMilli(getCurrentTimeMillis()));
    }

    @Override
    public String getModeLabel() {
        return "Часы";
    }
}
