package com.example.capstone.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple Singleton database connection.
 * The synchronized methods show SYNCHRONIZATION.
 */
public class DBConnection {

    private static DBConnection instance;
    private Connection connection;

    private final String url =
            "jdbc:mysql://127.0.0.1:3306/inventory_db" +
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila";
    private final String username = "root";
    private final String password = "";

    private DBConnection() {
        openConnection();
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                openConnection();
            }
        } catch (SQLException e) {
            System.out.println("Connection check error: " + e.getMessage());
        }
        return connection;
    }

    private void openConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected.");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL driver not found.");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }
}
