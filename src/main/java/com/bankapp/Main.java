package com.bankapp;

import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.bankapp.config.DBConnection;
public class Main {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 20; i++) {
            executor.submit(() -> {
                try (Connection conn = DBConnection.getConnection()) {
                    System.out.println(Thread.currentThread().getName() + " got connection " + conn);
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
    }
}
