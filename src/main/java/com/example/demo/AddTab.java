package com.example.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;

class AddTab {
    final DatabaseManager dbManager;
    final javafx.collections.ObservableList<Expense> expenses;

    AddTab(DatabaseManager manager, javafx.collections.ObservableList<Expense> list) {
        dbManager = manager;
        expenses = list;
    }

    Tab build() {
        Tab tab = new Tab("Add Expense");

        // ============================================
        // SECTION 1: Create Main Container
        // ============================================
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(30));
        container.getStyleClass().add("page-background");

        // ============================================
        // SECTION 2: Create Form Grid Layout
        // ============================================
        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(20);
        form.setPadding(new Insets(40));
        form.setAlignment(Pos.CENTER);
        form.getStyleClass().add("form-container");

        // Title label
        Label title = new Label("Add New Expense");
        title.getStyleClass().add("form-title");
        GridPane.setColumnSpan(title, 2);
        GridPane.setHalignment(title, HPos.CENTER);

        // ============================================
        // SECTION 3: Create Date Picker
        // ============================================
        Label dateLabel = new Label("Date:");
        dateLabel.getStyleClass().add("form-label");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.getStyleClass().add("date-picker");
        datePicker.setPrefWidth(250);

        // ============================================
        // SECTION 4: Create Category Field with Quick-Select Buttons
        // ============================================
        Label categoryLabel = new Label("Category:");
        categoryLabel.getStyleClass().add("form-label");
        ComboBox<String> categoryField = new ComboBox<>();
        categoryField.setEditable(true);
        categoryField.getItems().addAll("Food", "Transport", "Bills", "Shopping", "Health", "Entertainment", "Other");
        categoryField.getSelectionModel().selectFirst();
        categoryField.getStyleClass().add("combo-box");
        categoryField.setPrefWidth(250);

        // Quick-select category buttons (chips)
        HBox chips = new HBox(8);
        String[] chipCats = {"Food","Transport","Bills","Shopping","Health","Entertainment","Other"};
        for (String c : chipCats) {
            Button b = new Button(c);
            b.getStyleClass().add("chip");
            b.setOnAction(e -> { categoryField.getEditor().setText(c); });
            chips.getChildren().add(b);
        }

        // ============================================
        // SECTION 5: Create Amount Input Field
        // ============================================
        Label amountLabel = new Label("Amount:");
        amountLabel.getStyleClass().add("form-label");
        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        amountField.getStyleClass().add("form-input");
        amountField.setPrefWidth(250);

        // ============================================
        // SECTION 6: Create Note Text Area
        // ============================================
        Label noteLabel = new Label("Note:");
        noteLabel.getStyleClass().add("form-label");
        TextArea noteArea = new TextArea();
        noteArea.setPromptText("Optional");
        noteArea.setPrefRowCount(3);
        noteArea.getStyleClass().add("text-area");
        noteArea.setPrefWidth(250);

        // ============================================
        // SECTION 7: Create Submit Button and Status Label
        // ============================================
        Button addButton = new Button("Add Expense");
        addButton.setPrefWidth(250);
        addButton.setPrefHeight(45);
        addButton.getStyleClass().add("btn-primary");

        Label status = new Label("");
        status.getStyleClass().add("status-info");

        // ============================================
        // SECTION 8: Add All Fields to Form Grid
        // ============================================
        form.add(title, 0, 0);
        form.add(dateLabel, 0, 1);
        form.add(datePicker, 1, 1);
        form.add(categoryLabel, 0, 2);
        VBox catBox = new VBox(8, categoryField, chips);
        form.add(catBox, 1, 2);
        form.add(amountLabel, 0, 3);
        form.add(amountField, 1, 3);
        form.add(noteLabel, 0, 4);
        form.add(noteArea, 1, 4);
        GridPane.setColumnSpan(addButton, 2);
        GridPane.setHalignment(addButton, HPos.CENTER);
        form.add(addButton, 0, 5);
        form.add(status, 0, 6);
        GridPane.setColumnSpan(status, 2);
        GridPane.setHalignment(status, HPos.CENTER);

        // ============================================
        // SECTION 9: Handle Form Submission
        // ============================================
        addButton.setOnAction(e -> {
            // Get values from form fields
            LocalDate date = datePicker.getValue();
            String category = categoryField.getEditor().getText() == null ? "" : categoryField.getEditor().getText().trim();
            String amountText = amountField.getText() == null ? "" : amountField.getText().trim();
            String note = noteArea.getText() == null ? "" : noteArea.getText().trim();

            // Validate date is selected
            if (date == null) { 
                status.setText("Please choose a date."); 
                status.getStyleClass().clear();
                status.getStyleClass().add("status-error");
                return; 
            }
            
            // Validate category is entered
            if (category.isEmpty()) { 
                status.setText("Please enter a category."); 
                status.getStyleClass().clear();
                status.getStyleClass().add("status-error");
                return; 
            }
            
            // Validate amount is a valid number
            double amount;
            try { amount = Double.parseDouble(amountText); }
            catch (Exception ex) { 
                status.setText("Amount must be a number."); 
                status.getStyleClass().clear();
                status.getStyleClass().add("status-error");
                return; 
            }
            
            // Validate amount is positive
            if (amount <= 0) { 
                status.setText("Amount must be greater than 0."); 
                status.getStyleClass().clear();
                status.getStyleClass().add("status-error");
                return; 
            }

            // Create new expense and save
            Expense newExpense = new Expense(date, category, amount, note);
            expenses.add(0, newExpense);
            dbManager.appendExpense(newExpense);

            // Show success message
            status.setText("✓ Expense added successfully!");
            status.getStyleClass().clear();
            status.getStyleClass().add("status-success");
            
            // Clear form fields for next entry
            amountField.clear();
            noteArea.clear();
            categoryField.getEditor().selectAll();
            categoryField.getEditor().requestFocus();
        });

        // ============================================
        // SECTION 10: Finalize Tab
        // ============================================
        container.getChildren().add(form);
        tab.setContent(container);
        return tab;
    }
}
