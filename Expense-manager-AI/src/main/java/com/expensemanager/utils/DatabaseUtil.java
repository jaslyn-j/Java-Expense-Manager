package com.expensemanager.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    public static Connection getConnection() throws SQLException {
        String host = ConfigManager.getProperty("db.host", "localhost");
        String port = ConfigManager.getProperty("db.port", "3306");
        String dbName = ConfigManager.getProperty("db.name", "expense_manager");
        String user = ConfigManager.getProperty("db.user", "root");
        String password = ConfigManager.getProperty("db.password", "");

        String url = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC",
                host, port, dbName
        );

        return DriverManager.getConnection(url, user, password);
    }
}