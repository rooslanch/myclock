package com.example;

import com.example.app.MainViewController;
import com.example.factory.DefaultTimeStrategyFactory;

public class Main {

    public static void main(String[] args) {
        new MainViewController(new DefaultTimeStrategyFactory()).start();
    }
}