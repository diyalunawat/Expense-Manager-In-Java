package com.example.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    Stage stage;
    AuthManager authManager;
    LoginScene loginScene;
    RegisterScene registerScene;
    String currentUser;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        authManager = new AuthManager();
        loginScene = new LoginScene(authManager);
        registerScene = new RegisterScene(authManager);

        stage.setTitle("Expense Manager");
        stage.setResizable(true);

        setupLoginScene();
        stage.show();
    }

    void setupLoginScene() {
        loginScene.setOnLoginSuccess((username) -> {
            currentUser = username;
            showExpenseManager();
        });
        
        loginScene.setOnRegisterClick(() -> showRegisterScene());

        Scene scene = loginScene.createScene();
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    void showRegisterScene() {
        registerScene.setOnRegisterSuccess(() -> {
            setupLoginScene();
        });

        registerScene.setOnLoginClick(() -> setupLoginScene());

        Scene scene = registerScene.createScene();
        stage.setScene(scene);
    }

    void showExpenseManager() {
        ExpenseManagerScene managerScene = new ExpenseManagerScene(currentUser);
        managerScene.setOnLogout(() -> setupLoginScene());
        
        Scene scene = managerScene.createScene();
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(700);
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
