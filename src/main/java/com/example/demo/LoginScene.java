package com.example.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.function.Consumer;

class LoginScene {
    final AuthManager authManager;
    Consumer<String> onLoginSuccess;
    TextField usernameField;
    Runnable onRegisterClick;

    LoginScene(AuthManager manager) {
        authManager = manager;
    }

    void setOnLoginSuccess(Consumer<String> callback) {
        onLoginSuccess = callback;
    }
    void setOnRegisterClick(Runnable r) { onRegisterClick = r; }

    Scene createScene() {
        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("auth-container");

        Label logo = new Label("💰 Expense Manager");
        logo.getStyleClass().add("logo");
        logo.setStyle("-fx-font-size: 32px;");

        Label titleLabel = new Label("Login");
        titleLabel.getStyleClass().add("auth-title");

        VBox formBox = new VBox(18);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(40));
        formBox.getStyleClass().add("auth-card");
        formBox.setMaxWidth(380);

        Label subtitle = new Label("Welcome Back!");
        subtitle.getStyleClass().add("auth-subtitle");

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(280);
        usernameField.getStyleClass().add("form-input");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(280);
        passwordField.getStyleClass().add("form-input");

        Label statusLabel = new Label("");
        statusLabel.getStyleClass().add("status-error");

        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(280);
        loginButton.setPrefHeight(45);
        loginButton.getStyleClass().add("btn-primary");

        Hyperlink registerLink = new Hyperlink("Don't have an account? Register");
        registerLink.setStyle("-fx-text-fill: #667eea; -fx-font-size: 13px; -fx-cursor: hand;");
        registerLink.setOnAction(e -> { if (onRegisterClick != null) onRegisterClick.run(); });

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (authManager.login(username, password)) {
                if (onLoginSuccess != null) {
                    onLoginSuccess.accept(username);
                }
            } else {
                statusLabel.setText("Invalid username or password!");
                statusLabel.getStyleClass().clear();
                statusLabel.getStyleClass().add("status-error");
                passwordField.clear();
            }
        });

        formBox.getChildren().addAll(subtitle, usernameField, passwordField, loginButton, statusLabel, registerLink);

        root.getChildren().addAll(logo, titleLabel, formBox);

        Scene scene = new Scene(root, 550, 650);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        return scene;
    }

    Runnable getRegisterAction() { return () -> {}; }
}
