package com.example.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

class RegisterScene {
    final AuthManager authManager;
    Runnable onRegisterSuccess;
    Runnable onLoginClick;

    RegisterScene(AuthManager manager) {
        authManager = manager;
    }

    void setOnRegisterSuccess(Runnable callback) {
        onRegisterSuccess = callback;
    }
    void setOnLoginClick(Runnable r) { onLoginClick = r; }

    Scene createScene() {
        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("auth-container");

        Label logo = new Label("💰 Expense Manager");
        logo.getStyleClass().add("logo");
        logo.setStyle("-fx-font-size: 32px;");

        Label titleLabel = new Label("Create Account");
        titleLabel.getStyleClass().add("auth-title");

        VBox formBox = new VBox(18);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(40));
        formBox.getStyleClass().add("auth-card");
        formBox.setMaxWidth(380);

        Label subtitle = new Label("Join Expense Manager");
        subtitle.getStyleClass().add("auth-subtitle");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(280);
        usernameField.getStyleClass().add("form-input");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(280);
        passwordField.getStyleClass().add("form-input");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setPrefWidth(280);
        confirmPasswordField.getStyleClass().add("form-input");

        Label statusLabel = new Label("");
        statusLabel.getStyleClass().add("status-error");

        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(280);
        registerButton.setPrefHeight(45);
        registerButton.getStyleClass().add("btn-primary");

        Hyperlink loginLink = new Hyperlink("Already have an account? Login");
        loginLink.setStyle("-fx-text-fill: #667eea; -fx-font-size: 13px; -fx-cursor: hand;");
        loginLink.setOnAction(e -> { if (onLoginClick != null) onLoginClick.run(); });

        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (username.trim().isEmpty()) {
                statusLabel.setText("Username cannot be empty!");
                statusLabel.getStyleClass().clear();
                statusLabel.getStyleClass().add("status-error");
                return;
            }
            if (password.isEmpty()) {
                statusLabel.setText("Password cannot be empty!");
                statusLabel.getStyleClass().clear();
                statusLabel.getStyleClass().add("status-error");
                return;
            }
            if (!password.equals(confirmPassword)) {
                statusLabel.setText("Passwords do not match!");
                statusLabel.getStyleClass().clear();
                statusLabel.getStyleClass().add("status-error");
                return;
            }

            if (authManager.register(username, password)) {
                statusLabel.setText("Registration successful! Please login.");
                statusLabel.getStyleClass().clear();
                statusLabel.getStyleClass().add("status-success");
                if (onRegisterSuccess != null) {
                    onRegisterSuccess.run();
                }
            } else {
                statusLabel.setText("Username already exists!");
                statusLabel.getStyleClass().clear();
                statusLabel.getStyleClass().add("status-error");
            }
        });

        formBox.getChildren().addAll(subtitle, usernameField, passwordField, confirmPasswordField, 
                registerButton, statusLabel, loginLink);

        root.getChildren().addAll(logo, titleLabel, formBox);

        Scene scene = new Scene(root, 550, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        return scene;
    }
}
