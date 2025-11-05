package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

class ExpenseManagerScene {
    final DatabaseManager dbManager;
    final ObservableList<Expense> expenses;
    String username;
    Runnable onLogout;
    DashboardTab dashboardTab;
    TabPane tabPane;

    ExpenseManagerScene(String username) {
        this.username = username;
        DatabaseManager manager = new DatabaseManager(username);
        dbManager = manager;
        ObservableList<Expense> list = FXCollections.observableArrayList(dbManager.readExpenses());
        expenses = list;
    }

    Scene createScene() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().addAll("tab-pane", "hide-headers");

        dashboardTab = new DashboardTab(username, expenses, dbManager);
        dashboardTab.setOnLogout(() -> { if (onLogout != null) onLogout.run(); });
        dashboardTab.setRefreshCallback(() -> {});
        
        AddTab addTab = new AddTab(dbManager, expenses);
        ListTab listTab = new ListTab(dbManager, expenses);
        ChartsTab chartsTab = new ChartsTab(expenses);
        BudgetTab budgetTab = new BudgetTab(dbManager, expenses, () -> {
            if (dashboardTab != null) {
                dashboardTab.refresh();
            }
        });
        DeveloperTab developerTab = new DeveloperTab();

        tabPane.getTabs().addAll(
                dashboardTab.build(),
                addTab.build(),
                listTab.build(),
                chartsTab.build(),
                budgetTab.build(),
                developerTab.build()
        );

        Label logo = new Label("💰 Expense Manager");
        logo.getStyleClass().add("logo");
        
        Label userLabel = new Label("Hello, " + username);
        userLabel.getStyleClass().add("user-label");
        
        Button btnDashboard = new Button("Dashboard");
        btnDashboard.getStyleClass().add("nav-button");
        Button btnAdd = new Button("Add Expense");
        btnAdd.getStyleClass().add("nav-button");
        Button btnList = new Button("Expense List");
        btnList.getStyleClass().add("nav-button");
        Button btnCharts = new Button("Charts & Analytics");
        btnCharts.getStyleClass().add("nav-button");
        Button btnBudget = new Button("Budget");
        btnBudget.getStyleClass().add("nav-button");
        Button btnDeveloper = new Button("Developer");
        btnDeveloper.getStyleClass().add("nav-button");
        
        Region sideSpacer = new Region();
        VBox.setVgrow(sideSpacer, Priority.ALWAYS);
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("logout-btn");
        logoutBtn.setOnAction(e -> { if (onLogout != null) onLogout.run(); });

        VBox sidebar = new VBox(12, logo, userLabel, new Separator(),
                btnDashboard, btnAdd, btnList, btnCharts, btnBudget, btnDeveloper,
                sideSpacer, logoutBtn);
        sidebar.getStyleClass().add("sidebar");

        btnDashboard.setOnAction(e -> tabPane.getSelectionModel().select(0));
        btnAdd.setOnAction(e -> tabPane.getSelectionModel().select(1));
        btnList.setOnAction(e -> tabPane.getSelectionModel().select(2));
        btnCharts.setOnAction(e -> tabPane.getSelectionModel().select(3));
        btnBudget.setOnAction(e -> tabPane.getSelectionModel().select(4));
        btnDeveloper.setOnAction(e -> tabPane.getSelectionModel().select(5));

        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(tabPane);
        root.getStyleClass().add("page-background");

        Scene scene = new Scene(root, 1100, 750);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        checkBudgetOnStart();
        
        return scene;
    }

    private void checkBudgetOnStart() {
        double budget = dbManager.readBudget();
        if (budget <= 0) {
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Welcome!");
                alert.setHeaderText("Set Your Monthly Budget");
                alert.setContentText("Please set your monthly budget in the Budget tab to start tracking your expenses.");
                alert.showAndWait();
                tabPane.getSelectionModel().select(4);
            });
        }
    }

    void setOnLogout(Runnable r) { onLogout = r; }

    String safeTrim(String s) { return s == null ? "" : s.trim(); }
}
