package com.example.demo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class DatabaseManager {
    private final String dbUrl;
    private final String username;
    private int userId;

    DatabaseManager(String username) {
        this.username = username;
        
        Path dataDir = Paths.get("data");
        if (!Files.exists(dataDir)) {
            try {
                Files.createDirectories(dataDir);
            } catch (java.io.IOException e) {
                throw new RuntimeException("Cannot create data folder", e);
            }
        }
        
        Path dbFile = dataDir.resolve("expense_manager.db");
        dbUrl = "jdbc:sqlite:" + dbFile.toAbsolutePath();
        
        createTables();
        userId = getUserIdFromDatabase();
    }

    private void createTables() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("PRAGMA foreign_keys = ON");
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL
                )
                """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS expenses (
                    expense_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    date TEXT NOT NULL,
                    category TEXT NOT NULL,
                    amount REAL NOT NULL,
                    note TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                )
                """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS budgets (
                    user_id INTEGER PRIMARY KEY,
                    monthly_budget REAL DEFAULT 0.0,
                    extra_income REAL DEFAULT 0.0,
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                )
                """);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot create database tables", e);
        }
    }

    private int getUserIdFromDatabase() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM users WHERE username = ?")) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
            throw new RuntimeException("User not found: " + username);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot get user ID", e);
        }
    }

    List<Expense> readExpenses() {
        List<Expense> expenseList = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT expense_id, date, category, amount, note FROM expenses WHERE user_id = ? ORDER BY date DESC")) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("expense_id");
                LocalDate date = LocalDate.parse(rs.getString("date"));
                String category = rs.getString("category");
                double amount = rs.getDouble("amount");
                String note = rs.getString("note");
                if (note == null) note = "";
                
                expenseList.add(new Expense(id, date, category, amount, note));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return expenseList;
    }

    void appendExpense(Expense expense) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO expenses (user_id, date, category, amount, note) VALUES (?, ?, ?, ?, ?)")) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, expense.getDate().toString());
            stmt.setString(3, expense.getCategory());
            stmt.setDouble(4, expense.getAmount());
            stmt.setString(5, expense.getNote());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void updateExpense(Expense expense) {
        if (expense.getExpenseId() == null) {
            return;
        }
        
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(
                "UPDATE expenses SET date = ?, category = ?, amount = ?, note = ? WHERE expense_id = ? AND user_id = ?")) {
            
            stmt.setString(1, expense.getDate().toString());
            stmt.setString(2, expense.getCategory());
            stmt.setDouble(3, expense.getAmount());
            stmt.setString(4, expense.getNote());
            stmt.setInt(5, expense.getExpenseId());
            stmt.setInt(6, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void writeAll(List<Expense> allExpenses) {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM expenses WHERE user_id = ?");
            deleteStmt.setInt(1, userId);
            deleteStmt.executeUpdate();
            
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO expenses (user_id, date, category, amount, note) VALUES (?, ?, ?, ?, ?)");
            
            for (Expense expense : allExpenses) {
                insertStmt.setInt(1, userId);
                insertStmt.setString(2, expense.getDate().toString());
                insertStmt.setString(3, expense.getCategory());
                insertStmt.setDouble(4, expense.getAmount());
                insertStmt.setString(5, expense.getNote());
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    double readBudget() {
        return readBudgetColumn("monthly_budget");
    }

    void writeBudget(double amount) {
        writeBudgetColumn("monthly_budget", amount);
    }

    double readExtra() {
        return readBudgetColumn("extra_income");
    }

    void writeExtra(double amount) {
        writeBudgetColumn("extra_income", amount);
    }

    private double readBudgetColumn(String columnName) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT " + columnName + " FROM budgets WHERE user_id = ?")) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double value = rs.getDouble(columnName);
                rs.close();
                return value;
            }
            rs.close();
            
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO budgets (user_id, monthly_budget, extra_income) VALUES (?, 0.0, 0.0)");
            insertStmt.setInt(1, userId);
            insertStmt.executeUpdate();
            return 0.0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private void writeBudgetColumn(String columnName, double amount) {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            PreparedStatement checkStmt = conn.prepareStatement("SELECT user_id FROM budgets WHERE user_id = ?");
            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                rs.close();
                checkStmt.close();
                PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO budgets (user_id, monthly_budget, extra_income) VALUES (?, 0.0, 0.0)");
                insertStmt.setInt(1, userId);
                insertStmt.executeUpdate();
                insertStmt.close();
            } else {
                rs.close();
                checkStmt.close();
            }
            
            PreparedStatement updateStmt = conn.prepareStatement(
                "UPDATE budgets SET " + columnName + " = ? WHERE user_id = ?");
            updateStmt.setDouble(1, amount);
            updateStmt.setInt(2, userId);
            updateStmt.executeUpdate();
            updateStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
