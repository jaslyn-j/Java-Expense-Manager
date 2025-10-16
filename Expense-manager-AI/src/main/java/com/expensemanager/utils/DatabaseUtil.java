package com.expensemanager.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/expense_manager?useSSL=false&serverTimezone=UTC",
                "root",
                "avocadodo"
        );
    }
}