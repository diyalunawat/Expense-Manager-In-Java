package com.example.demo;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

class BudgetTab {
    final DatabaseManager dbManager;
    final ObservableList<Expense> expenses;
    Runnable onBudgetChange;

    BudgetTab(DatabaseManager manager, ObservableList<Expense> list, Runnable callback) {
        dbManager = manager;
        expenses = list;
        onBudgetChange = callback;
    }

    Tab build() {
        Tab tab = new Tab("Budget");

        VBox container = new VBox(20);
        container.getStyleClass().add("budget-container");

        Label title = new Label("Budget Management");
        title.getStyleClass().add("budget-title");

        Label monthLabel = new Label(java.time.YearMonth.now().toString());
        monthLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        double currentBudget = dbManager.readBudget();
        double currentExtra = dbManager.readExtra();
        
        Label budgetLabel = new Label("Monthly Budget (₹):");
        budgetLabel.getStyleClass().add("form-label");
        TextField budgetField = new TextField(currentBudget > 0 ? String.format(java.util.Locale.US, "%.2f", currentBudget) : "");
        budgetField.setPromptText("0.00");
        budgetField.getStyleClass().add("form-input");
        
        Label extraLabel = new Label("Extra Income (₹):");
        extraLabel.getStyleClass().add("form-label");
        TextField extraField = new TextField(currentExtra > 0 ? String.format(java.util.Locale.US, "%.2f", currentExtra) : "");
        extraField.setPromptText("0.00");
        extraField.getStyleClass().add("form-input");

        Label spentLabel = new Label();
        spentLabel.getStyleClass().add("status-info");
        Label remainingLabel = new Label();
        remainingLabel.getStyleClass().add("status-info");
        ProgressBar progress = new ProgressBar(0);
        progress.setPrefWidth(400);
        progress.setPrefHeight(25);

        Runnable refresh = () -> {
            java.time.YearMonth nowYm = java.time.YearMonth.now();
            double monthTotal = expenses.stream()
                    .filter(e -> java.time.YearMonth.from(e.getDate()).equals(nowYm))
                    .mapToDouble(Expense::getAmount)
                    .sum();
            spentLabel.setText(String.format(java.util.Locale.US, "Spent this month: ₹%.2f", monthTotal));
            double budgetVal;
            try { budgetVal = Double.parseDouble(budgetField.getText().trim()); }
            catch (Exception ex) { budgetVal = 0; }
            double extraVal;
            try { extraVal = Double.parseDouble(extraField.getText().trim()); }
            catch (Exception ex) { extraVal = 0; }
            if (budgetVal <= 0) {
                remainingLabel.setText("Remaining: set a budget");
                remainingLabel.getStyleClass().remove("status-success");
                remainingLabel.getStyleClass().remove("status-error");
                remainingLabel.getStyleClass().add("status-info");
                progress.setProgress(0);
            } else {
                double remaining = (budgetVal + extraVal) - monthTotal;
                remainingLabel.setText(String.format(java.util.Locale.US, "Remaining: ₹%.2f", remaining));
                if (remaining >= 0) {
                    remainingLabel.getStyleClass().remove("status-error");
                    remainingLabel.getStyleClass().add("status-success");
                } else {
                    remainingLabel.getStyleClass().remove("status-success");
                    remainingLabel.getStyleClass().add("status-error");
                }
                progress.setProgress(Math.max(0, Math.min(1, monthTotal / Math.max(1, budgetVal + extraVal))));
            }
            if (onBudgetChange != null) onBudgetChange.run();
        };

        Button saveBudget = new Button("Save Budget");
        saveBudget.getStyleClass().add("btn-info");
        saveBudget.setOnAction(e -> {
            double val;
            try { val = Double.parseDouble(budgetField.getText().trim()); } catch (Exception ex) { val = 0; }
            dbManager.writeBudget(val);
            refresh.run();
            if (onBudgetChange != null) onBudgetChange.run();
        });
        
        Button saveExtra = new Button("Save Extra");
        saveExtra.getStyleClass().add("btn-info");
        saveExtra.setOnAction(e -> {
            double val;
            try { val = Double.parseDouble(extraField.getText().trim()); } catch (Exception ex) { val = 0; }
            dbManager.writeExtra(val);
            refresh.run();
            if (onBudgetChange != null) onBudgetChange.run();
        });

        Button quickAdd = new Button("Quick Add Monthly Expense");
        quickAdd.getStyleClass().add("btn-success");
        quickAdd.setOnAction(e -> {
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
                        refresh.run();
                    }
                } catch (Exception ignore) { }
            }
        });

        expenses.addListener((javafx.collections.ListChangeListener<? super Expense>) c -> refresh.run());
        refresh.run();

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));

        grid.add(title, 0, 0);
        GridPane.setColumnSpan(title, 3);
        GridPane.setHalignment(title, HPos.CENTER);
        
        grid.add(new Label("Month:"), 0, 1);
        grid.add(monthLabel, 1, 1);
        
        grid.add(budgetLabel, 0, 2);
        grid.add(budgetField, 1, 2);
        grid.add(saveBudget, 2, 2);
        
        grid.add(extraLabel, 0, 3);
        grid.add(extraField, 1, 3);
        grid.add(saveExtra, 2, 3);
        
        grid.add(spentLabel, 0, 4);
        GridPane.setColumnSpan(spentLabel, 3);
        grid.add(remainingLabel, 0, 5);
        GridPane.setColumnSpan(remainingLabel, 3);
        grid.add(progress, 0, 6);
        GridPane.setColumnSpan(progress, 3);

        VBox root = new VBox(20, grid, quickAdd);
        root.setPadding(new Insets(20));
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.getStyleClass().add("page-background");

        container.getChildren().add(root);
        tab.setContent(container);
        return tab;
    }
}
