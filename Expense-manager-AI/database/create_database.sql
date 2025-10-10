-- Clean slate: Drop database if exists
DROP DATABASE IF EXISTS expense_manager;

-- Create and use the database
CREATE DATABASE expense_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE expense_manager;

-- Create Users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    full_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Categories table
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    icon_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Expenses table
CREATE TABLE expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    description VARCHAR(255),
    date DATE NOT NULL,
    payment_method VARCHAR(50),
    is_recurring BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Budget table
CREATE TABLE budgets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_budget (user_id, category_id, start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add foreign key constraints
ALTER TABLE expenses
ADD CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
ADD CONSTRAINT fk_expenses_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT;

ALTER TABLE budgets
ADD CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
ADD CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE;

-- Add indexes for better performance
ALTER TABLE expenses
ADD INDEX idx_user_date (user_id, date),
ADD INDEX idx_category (category_id),
ADD INDEX idx_date (date);

ALTER TABLE budgets
ADD INDEX idx_user_budget (user_id),
ADD INDEX idx_budget_dates (start_date, end_date);

-- Insert default categories
INSERT INTO categories (name, description, icon_name) VALUES
('Food', 'Groceries, restaurants, and food delivery', 'food'),
('Transport', 'Public transport, fuel, taxi, and vehicle maintenance', 'transport'),
('Housing', 'Rent, utilities, and home maintenance', 'home'),
('Entertainment', 'Movies, games, and recreational activities', 'entertainment'),
('Shopping', 'Clothing, electronics, and general shopping', 'shopping'),
('Healthcare', 'Medical expenses and healthcare', 'health'),
('Education', 'Books, courses, and educational materials', 'education'),
('Bills', 'Regular monthly bills and subscriptions', 'bill'),
('Travel', 'Vacations and business travel expenses', 'travel'),
('Others', 'Miscellaneous expenses', 'other');

-- Insert sample users (password is 'password123' hashed)
INSERT INTO users (username, password, email, full_name) VALUES
('john_doe', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewqyPCGmgGHs2hvi', 'john@example.com', 'John Doe'),
('jane_smith', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewqyPCGmgGHs2hvi', 'jane@example.com', 'Jane Smith');

-- Insert sample expenses for John Doe
INSERT INTO expenses (user_id, category_id, amount, description, date, payment_method) VALUES
(1, 1, 50.00, 'Grocery shopping', CURDATE(), 'Credit Card'),
(1, 1, 25.00, 'Lunch at restaurant', CURDATE(), 'Cash'),
(1, 2, 30.00, 'Bus ticket', CURDATE(), 'Debit Card'),
(1, 4, 60.00, 'Movie night', CURDATE(), 'Credit Card');

-- Insert sample expenses for Jane Smith
INSERT INTO expenses (user_id, category_id, amount, description, date, payment_method) VALUES
(2, 1, 45.00, 'Dinner', CURDATE(), 'Credit Card'),
(2, 5, 120.00, 'New clothes', CURDATE(), 'Credit Card'),
(2, 3, 800.00, 'Rent payment', CURDATE(), 'Bank Transfer'),
(2, 8, 50.00, 'Internet bill', CURDATE(), 'Direct Debit');

-- Insert sample budgets
INSERT INTO budgets (user_id, category_id, amount, start_date, end_date) VALUES
(1, 1, 500.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 MONTH)),
(1, 2, 200.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 MONTH)),
(2, 1, 400.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 MONTH)),
(2, 5, 300.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 MONTH));

-- Create views for common queries
CREATE VIEW expense_summary AS
SELECT
    u.username,
    c.name as category,
    e.date,
    SUM(e.amount) as total_amount,
    COUNT(*) as transaction_count
FROM expenses e
JOIN users u ON e.user_id = u.id
JOIN categories c ON e.category_id = c.id
GROUP BY u.username, c.name, e.date;

CREATE VIEW budget_vs_actual AS
SELECT
    u.username,
    c.name as category,
    b.amount as budget_amount,
    COALESCE(SUM(e.amount), 0) as spent_amount,
    b.amount - COALESCE(SUM(e.amount), 0) as remaining_amount,
    b.start_date,
    b.end_date
FROM budgets b
JOIN users u ON b.user_id = u.id
JOIN categories c ON b.category_id = c.id
LEFT JOIN expenses e ON e.user_id = b.user_id
    AND e.category_id = b.category_id
    AND e.date BETWEEN b.start_date AND b.end_date
GROUP BY b.id, u.username, c.name, b.amount, b.start_date, b.end_date;