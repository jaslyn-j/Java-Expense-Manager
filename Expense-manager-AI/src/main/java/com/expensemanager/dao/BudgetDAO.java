package com.expensemanager.dao;

import com.expensemanager.models.Budget;
import com.expensemanager.utils.DatabaseUtil;
import java.sql.*;

public class BudgetDAO {
    
    public Budget createBudget(Budget budget) throws SQLException {
        String sql = "INSERT INTO budgets (user_id, category_id, amount, start_date, end_date) " +
                    "SELECT ?, id, ?, ?, ? FROM categories WHERE name = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, budget.getUserId());
            pstmt.setBigDecimal(2, budget.getAmount());
            pstmt.setDate(3, Date.valueOf(budget.getStartDate()));
            pstmt.setDate(4, Date.valueOf(budget.getEndDate()));
            pstmt.setString(5, budget.getCategory());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating budget failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    budget.setId(generatedKeys.getInt(1));
                    return budget;
                } else {
                    throw new SQLException("Creating budget failed, no ID obtained.");
                }
            }
        }
    }


    public boolean deleteBudget(int budgetId, int userId) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, budgetId);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
} 