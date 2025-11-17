package com.example.listener;

public interface DataListener {
    void onNewValue(long currentTimeMillis, String message);
}