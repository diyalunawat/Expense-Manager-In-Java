package com.example.demo;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

class DashboardTab {
    final ObservableList<Expense> expenses;
    final DatabaseManager dbManager;
    final String username;
    Runnable onLogout;
    Runnable refreshCallback;
    Runnable refreshMethod;

    DashboardTab(String user, ObservableList<Expense> list, DatabaseManager manager) {
        username = user;
        expenses = list;
        dbManager = manager;
    }

    void setOnLogout(Runnable r) { onLogout = r; }
    void setRefreshCallback(Runnable r) { refreshCallback = r; }

    Tab build() {
        Tab tab = new Tab("Dashboard");

        VBox container = new VBox(20);
        container.getStyleClass().add("dashboard-container");

        Label title = new Label("Expense Manager");
        title.getStyleClass().add("dashboard-title");

        Label hello = new Label("Hello, " + username);
        hello.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #2d3748;");

        HBox headerBox = new HBox(15, title, hello);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        GridPane kpiGrid = new GridPane();
        kpiGrid.setHgap(20);
        kpiGrid.setVgap(20);
        kpiGrid.setAlignment(Pos.CENTER);
        kpiGrid.getStyleClass().add("kpi-grid");

        Label kpi1Label = new Label("This Month");
        kpi1Label.getStyleClass().add("kpi-label");
        Label kpi1Value = new Label();
        kpi1Value.getStyleClass().add("kpi-value");

        Label kpi2Label = new Label("Previous Month");
        kpi2Label.getStyleClass().add("kpi-label");
        Label kpi2Value = new Label();
        kpi2Value.getStyleClass().add("kpi-value");

        Label kpi3Label = new Label("Monthly Budget");
        kpi3Label.getStyleClass().add("kpi-label");
        Label kpi3Value = new Label();
        kpi3Value.getStyleClass().add("kpi-value");

        Label kpi4Label = new Label("Extra Income");
        kpi4Label.getStyleClass().add("kpi-label");
        Label kpi4Value = new Label();
        kpi4Value.getStyleClass().add("kpi-value");

        Label kpi5Label = new Label("Remaining");
        kpi5Label.getStyleClass().add("kpi-label");
        Label kpi5Value = new Label();
        kpi5Value.getStyleClass().add("kpi-value");

        VBox kpi1Box = createKpiBox(kpi1Label, kpi1Value);
        VBox kpi2Box = createKpiBox(kpi2Label, kpi2Value);
        VBox kpi3Box = createKpiBox(kpi3Label, kpi3Value);
        VBox kpi4Box = createKpiBox(kpi4Label, kpi4Value);
        VBox kpi5Box = createKpiBox(kpi5Label, kpi5Value);

        kpiGrid.add(kpi1Box, 0, 0);
        kpiGrid.add(kpi2Box, 1, 0);
        kpiGrid.add(kpi3Box, 2, 0);
        kpiGrid.add(kpi4Box, 0, 1);
        kpiGrid.add(kpi5Box, 1, 1);

        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        y.setLabel("Amount (₹)");
        LineChart<String, Number> trend = new LineChart<>(x, y);
        trend.getStyleClass().add("chart");
        trend.setTitle("3-Month Expense Trend");
        trend.setLegendVisible(false);
        trend.setPrefHeight(300);

        Button quickAddMonthly = new Button("Quick Add Monthly Expense");
        quickAddMonthly.getStyleClass().add("btn-success");
        HBox quickLinks = new HBox(10,
                styledGhost("Add Expense"),
                styledGhost("View List"),
                styledGhost("Open Budget")
        );

        Runnable refresh = () -> {
            double budget = dbManager.readBudget();
            double extra = dbManager.readExtra();
            java.time.YearMonth now = java.time.YearMonth.now();
            java.time.YearMonth prev = now.minusMonths(1);

            double totalNow = expenses.stream()
                    .filter(e -> java.time.YearMonth.from(e.getDate()).equals(now))
                    .mapToDouble(Expense::getAmount).sum();
            double totalPrev = expenses.stream()
                    .filter(e -> java.time.YearMonth.from(e.getDate()).equals(prev))
                    .mapToDouble(Expense::getAmount).sum();
            double remaining = (budget + extra) - totalNow;

            kpi1Value.setText(String.format(java.util.Locale.US, "₹%.2f", totalNow));
            kpi2Value.setText(String.format(java.util.Locale.US, "₹%.2f", totalPrev));
            kpi3Value.setText(String.format(java.util.Locale.US, "₹%.2f", budget));
            kpi4Value.setText(String.format(java.util.Locale.US, "₹%.2f", extra));
            kpi5Value.setText(String.format(java.util.Locale.US, "₹%.2f", remaining));

            java.time.YearMonth m2 = now.minusMonths(2);
            java.util.Map<java.time.YearMonth, Double> byMonth = new java.util.HashMap<>();
            byMonth.put(m2, 0.0); byMonth.put(prev, 0.0); byMonth.put(now, 0.0);
            for (Expense ex : expenses) {
                java.time.YearMonth ym = java.time.YearMonth.from(ex.getDate());
                if (byMonth.containsKey(ym)) byMonth.put(ym, byMonth.get(ym) + ex.getAmount());
            }
            java.util.List<java.time.YearMonth> order = java.util.Arrays.asList(m2, prev, now);
            java.util.List<String> labels = new java.util.ArrayList<>();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (java.time.YearMonth ym : order) {
                String label = ym.getMonth().toString().substring(0,3) + " " + ym.getYear();
                labels.add(label);
                series.getData().add(new XYChart.Data<>(label, byMonth.get(ym)));
            }
            x.setCategories(javafx.collections.FXCollections.observableArrayList(labels));
            trend.getData().setAll(series);
        };

        refreshMethod = refresh;

        expenses.addListener((javafx.collections.ListChangeListener<? super Expense>) change -> {
            refresh.run();
            if (refreshCallback != null) refreshCallback.run();
        });

        quickAddMonthly.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Monthly Expense");
            dialog.setHeaderText("Add a monthly expense amount (₹)");
            dialog.setContentText("Amount:");
            java.util.Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    double amount = Double.parseDouble(result.get().trim());
                    if (amount > 0) {
                        Expense exp = new Expense(java.time.LocalDate.now(), "Monthly", amount, "Quick add");
                        expenses.add(0, exp);
                        dbManager.appendExpense(exp);
                    }
                } catch (Exception ignore) {}
            }
        });

        refresh.run();

        VBox center = new VBox(25, headerBox, kpiGrid, trend, quickLinks, quickAddMonthly);
        center.setPadding(new Insets(25));
        center.setAlignment(Pos.TOP_CENTER);
        center.getStyleClass().add("dashboard-background");

        container.getChildren().add(center);
        tab.setContent(container);
        return tab;
    }

    void refresh() {
        if (refreshMethod != null) {
            refreshMethod.run();
        }
    }

    VBox createKpiBox(Label label, Label value) {
        VBox box = new VBox(8);
        box.getStyleClass().add("kpi-box");
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.getChildren().addAll(label, value);
        return box;
    }

    Button styledGhost(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-ghost");
        return b;
    }
}
