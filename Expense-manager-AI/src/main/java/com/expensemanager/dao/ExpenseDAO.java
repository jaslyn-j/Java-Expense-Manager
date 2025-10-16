package com.expensemanager.dao;

import com.expensemanager.models.Expense;
import com.expensemanager.utils.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;

public class ExpenseDAO {
    
    public BigDecimal getTotalExpenses(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM expenses WHERE user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
                return BigDecimal.ZERO;
            }
        }
    }
    
    public Map<String, BigDecimal> getExpensesByDateRange(int userId, LocalDate startDate, LocalDate endDate) 
            throws SQLException {
        String sql = "SELECT c.name, COALESCE(SUM(e.amount), 0) as total " +
                    "FROM expenses e " +
                    "JOIN categories c ON e.category_id = c.id " +
                    "WHERE e.user_id = ? AND e.date BETWEEN ? AND ? " +
                    "GROUP BY c.id, c.name ORDER BY total DESC";
        
        Map<String, BigDecimal> expenses = new LinkedHashMap<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("name");
                    BigDecimal total = rs.getBigDecimal("total");
                    expenses.put(category, total);
                }
            }
        }
        
        return expenses;
    }

    public Map<String, BigDecimal> getExpensesByCategory(int userId) throws SQLException {
        String sql = "SELECT c.name, COALESCE(SUM(e.amount), 0) as total " +
                    "FROM expenses e " +
                    "JOIN categories c ON e.category_id = c.id " +
                    "WHERE e.user_id = ? " +
                    "GROUP BY c.id, c.name ORDER BY total DESC";

        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("name");
                    BigDecimal total = rs.getBigDecimal("total");
                    categoryTotals.put(category, total);
                }
            }
        }

        return categoryTotals;
    }

    public List<Expense> getAllExpensesWithCategory(int userId) throws SQLException {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT e.id, e.user_id, e.amount, e.category_id, c.name as category_name, e.date, e.description " +
                "FROM expenses e " +
                "JOIN categories c ON e.category_id = c.id " +
                "WHERE e.user_id = ? ORDER BY e.date DESC, e.id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Expense expense = new Expense(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getBigDecimal("amount"),
                            rs.getInt("category_id"),
                            rs.getString("category_name"),
                            rs.getDate("date").toLocalDate(),
                            rs.getString("description")
                    );
                    expenses.add(expense);
                }
            }
        }
        return expenses;
    }
    
    public List<Map<String, Object>> getRecentExpenses(int userId, int limit) throws SQLException {
        String sql = "SELECT e.id, e.amount, c.name as category, e.date, e.description " +
                    "FROM expenses e " +
                    "JOIN categories c ON e.category_id = c.id " +
                    "WHERE e.user_id = ? ORDER BY e.date DESC, e.id DESC LIMIT ?";
        
        List<Map<String, Object>> expenses = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, Math.min(limit, 100));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> expense = new HashMap<>();
                    expense.put("id", rs.getInt("id"));
                    expense.put("amount", rs.getBigDecimal("amount"));
                    expense.put("category", rs.getString("category"));
                    Date date = rs.getDate("date");
                    expense.put("date", date != null ? date.toLocalDate() : null);
                    expense.put("description", rs.getString("description"));
                    expenses.add(expense);
                }
            }
        }
        
        return expenses;
    }
    
    public void createExpense(Expense expense) throws SQLException {
        String getCategoryIdSql = "SELECT id FROM categories WHERE name = ?";
        String insertExpenseSql = "INSERT INTO expenses (user_id, amount, date, description, category_id) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            int categoryId;
            try (PreparedStatement stmt = conn.prepareStatement(getCategoryIdSql)) {
                stmt.setString(1, expense.getCategoryName());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Category not found: " + expense.getCategoryName());
                    }
                    categoryId = rs.getInt("id");
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertExpenseSql)) {
                stmt.setInt(1, expense.getUserId());
                stmt.setBigDecimal(2, expense.getAmount());
                stmt.setDate(3, java.sql.Date.valueOf(expense.getDate()));
                stmt.setString(4, expense.getDescription());
                stmt.setInt(5, categoryId);
                stmt.executeUpdate();
            }
        }
    }
    
    public void updateExpense(Expense expense) throws SQLException {
        String getCategoryIdSql = "SELECT id FROM categories WHERE name = ?";
        String updateExpenseSql = "UPDATE expenses SET amount = ?, date = ?, description = ?, category_id = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            int categoryId;
            try (PreparedStatement stmt = conn.prepareStatement(getCategoryIdSql)) {
                stmt.setString(1, expense.getCategoryName());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Category not found: " + expense.getCategoryName());
                    }
                    categoryId = rs.getInt("id");
                }
            }


            try (PreparedStatement stmt = conn.prepareStatement(updateExpenseSql)) {
                stmt.setBigDecimal(1, expense.getAmount());
                stmt.setDate(2, java.sql.Date.valueOf(expense.getDate()));
                stmt.setString(3, expense.getDescription());
                stmt.setInt(4, categoryId);
                stmt.setInt(5, expense.getId());
                stmt.executeUpdate();
            }
        }
    }
    
    public void deleteExpense(int expenseId) throws SQLException {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, expenseId);
            stmt.executeUpdate();
        }
    }

    public BigDecimal getMonthlyAverage(int userId) throws SQLException {
        String sql = "SELECT COALESCE(AVG(monthly_total), 0) as avg_monthly FROM " +
                "(SELECT SUM(amount) as monthly_total FROM expenses " +
                "WHERE user_id = ? " +
                "GROUP BY YEAR(date), MONTH(date)) as monthly_totals";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("avg_monthly");
                }
                return BigDecimal.ZERO;
            }
        }
    }

    public List<Map<String, Object>> getBudgetsWithSpending(int userId) throws SQLException {
        String sql = "SELECT b.id, b.category_id, c.name as category_name, b.amount as budget_amount, " +
                "b.start_date, b.end_date, " +
                "COALESCE(SUM(e.amount), 0) as spent_amount " +
                "FROM budgets b " +
                "JOIN categories c ON b.category_id = c.id " +
                "LEFT JOIN expenses e ON e.user_id = b.user_id " +
                "  AND e.category_id = b.category_id " +
                "  AND e.date BETWEEN b.start_date AND b.end_date " +
                "WHERE b.user_id = ? " +
                "GROUP BY b.id, b.category_id, c.name, b.amount, b.start_date, b.end_date " +
                "ORDER BY b.end_date DESC";

        List<Map<String, Object>> budgets = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> budget = new HashMap<>();
                    budget.put("id", rs.getInt("id"));
                    budget.put("category_id", rs.getInt("category_id"));
                    budget.put("category_name", rs.getString("category_name"));
                    budget.put("budget_amount", rs.getBigDecimal("budget_amount"));
                    budget.put("spent_amount", rs.getBigDecimal("spent_amount"));
                    budget.put("start_date", rs.getDate("start_date").toLocalDate());
                    budget.put("end_date", rs.getDate("end_date").toLocalDate());
                    budgets.add(budget);
                }
            }
        }

        return budgets;
    }

    public void createBudget(int userId, String categoryName, BigDecimal amount,
                             LocalDate startDate, LocalDate endDate) throws SQLException {
        String getCategoryIdSql = "SELECT id FROM categories WHERE name = ?";
        String insertBudgetSql = "INSERT INTO budgets (user_id, category_id, amount, start_date, end_date) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection()) {
            int categoryId;
            try (PreparedStatement stmt = conn.prepareStatement(getCategoryIdSql)) {
                stmt.setString(1, categoryName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Category not found: " + categoryName);
                    }
                    categoryId = rs.getInt("id");
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertBudgetSql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, categoryId);
                stmt.setBigDecimal(3, amount);
                stmt.setDate(4, java.sql.Date.valueOf(startDate));
                stmt.setDate(5, java.sql.Date.valueOf(endDate));
                stmt.executeUpdate();
            }
        }
    }

    public void deleteBudget(int budgetId) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, budgetId);
            stmt.executeUpdate();
        }
    }

    public String getTopCategory(int userId) throws SQLException {
        String sql = "SELECT c.name FROM expenses e " +
                "JOIN categories c ON e.category_id = c.id " +
                "WHERE e.user_id = ? " +
                "GROUP BY c.id, c.name " +
                "ORDER BY SUM(e.amount) DESC LIMIT 1";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
                return "No expenses";
            }
        }
    }

    public List<Expense> getAllExpensesByUserId(int userId) throws SQLException {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Expense expense = new Expense(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getBigDecimal("amount"),
                            rs.getInt("category_id"),
                            null,
                            rs.getDate("date").toLocalDate(),
                            rs.getString("description")
                    );
                    expenses.add(expense);
                }
            }
        }
        return expenses;
    }
}