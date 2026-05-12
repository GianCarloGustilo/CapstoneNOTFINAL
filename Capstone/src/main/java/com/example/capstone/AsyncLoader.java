package com.example.capstone.util;

import javafx.application.Platform;

/**
 * Simple background loader.
 * This file shows MULTITHREADING because it uses a separate Thread.
 * It shows SYNCHRONIZATION because only one load task can use the lock at a time.
 */
public class AsyncLoader {

    private static final Object LOCK = new Object();

    private AsyncLoader() {
    }

    public interface DataTask<T> {
        T run();
    }

    public interface DataHandler<T> {
        void handle(T data);
    }

    public interface ErrorHandler {
        void handle(Exception e);
    }

    public static <T> void run(DataTask<T> task, DataHandler<T> successHandler, ErrorHandler errorHandler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    T result;
                    synchronized (LOCK) {
                        result = task.run();
                    }

                    T finalResult = result;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            successHandler.handle(finalResult);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            errorHandler.handle(e);
                        }
                    });
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }
}
