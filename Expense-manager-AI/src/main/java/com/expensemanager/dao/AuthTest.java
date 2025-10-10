package com.expensemanager.dao;

import com.expensemanager.models.User;
import com.expensemanager.utils.PasswordUtil;

import java.sql.SQLException;

public class AuthTest {
    public static void main(String[] args) {
        try {
            UserDAO userDAO = new UserDAO();

            // Test with testuser
            boolean result = userDAO.authenticate("testuser", "test123");
            System.out.println("Authentication result for testuser: " + result);

            // Debug: Check if user exists
            User user = userDAO.findByUsername("testuser");
            if (user != null) {
                System.out.println("User found: " + user.getUsername());
                System.out.println("Stored hash: " + user.getPassword());

                // Test password verification directly
                boolean directTest = PasswordUtil.verifyPassword("test123", user.getPassword());
                System.out.println("Direct password verification: " + directTest);
            } else {
                System.out.println("User not found!");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}