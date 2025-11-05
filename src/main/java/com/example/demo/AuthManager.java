package com.example.demo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

class AuthManager {
    private final String dbUrl;

    // Constructor - sets up connection to database
    AuthManager() {
        // Create data folder if it doesn't exist
        Path dataDir = Paths.get("data");
        if (!Files.exists(dataDir)) {
            try {
                Files.createDirectories(dataDir);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create data folder", e);
            }
        }
        
        // Database file path
        Path dbFile = dataDir.resolve("expense_manager.db");
        dbUrl = "jdbc:sqlite:" + dbFile.toAbsolutePath();
        
        // Create users table if it doesn't exist
        createUsersTable();
    }

    // Create the users table
    private void createUsersTable() {
        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            Statement stmt = conn.createStatement();
            
            // Turn on foreign keys
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Create users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL
                )
                """);
            
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot create users table", e);
        }
    }

    // Register a new user
    boolean register(String username, String password) {
        // Check if username and password are valid
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        username = username.trim().toLowerCase();
        
        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            
            // Check if username already exists
            PreparedStatement checkStmt = conn.prepareStatement("SELECT user_id FROM users WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Username already exists
                rs.close();
                checkStmt.close();
                conn.close();
                return false;
            }
            
            rs.close();
            checkStmt.close();
            
            // Username doesn't exist, create new user
            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.executeUpdate();
            
            insertStmt.close();
            conn.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check if login is correct
    boolean login(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        username = username.trim().toLowerCase();
        
        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE username = ?");
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Found user, check if password matches
                String storedPassword = rs.getString("password");
                rs.close();
                stmt.close();
                conn.close();
                return storedPassword != null && storedPassword.equals(password);
            }
            
            // User not found
            rs.close();
            stmt.close();
            conn.close();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
