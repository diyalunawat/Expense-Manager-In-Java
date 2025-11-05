package com.example.demo;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;

class ListTab {
    final DatabaseManager dbManager;
    final ObservableList<Expense> expenses;

    ListTab(DatabaseManager manager, ObservableList<Expense> list) {
        dbManager = manager;
        expenses = list;
    }

    Tab build() {
        Tab tab = new Tab("Expense List");

        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("page-background");

        TextField search = new TextField();
        search.setPromptText("Search notes/category...");
        search.getStyleClass().add("form-input");
        search.setPrefWidth(240);

        ComboBox<String> monthFilter = new ComboBox<>();
        monthFilter.getItems().add("All Months");
        java.time.YearMonth now = java.time.YearMonth.now();
        for (int i = 0; i < 12; i++) {
            java.time.YearMonth ym = now.minusMonths(i);
            monthFilter.getItems().add(ym.toString());
        }
        monthFilter.getSelectionModel().select(0);
        monthFilter.getStyleClass().add("combo-box");

        HBox toolbar = new HBox(12, search, monthFilter);
        toolbar.getStyleClass().add("table-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        FilteredList<Expense> filtered = new FilteredList<>(expenses, ex -> true);
        
        search.textProperty().addListener((o, oldV, newV) -> {
            String q = newV == null ? "" : newV.trim().toLowerCase();
            filtered.setPredicate(ex -> {
                boolean matchText = q.isEmpty() || ex.getCategory().toLowerCase().contains(q) || ex.getNote().toLowerCase().contains(q);
                String sel = monthFilter.getSelectionModel().getSelectedItem();
                boolean matchMonth = sel == null || sel.equals("All Months") || java.time.YearMonth.from(ex.getDate()).toString().equals(sel);
                return matchText && matchMonth;
            });
        });
        
        monthFilter.valueProperty().addListener((o, oldV, newV) -> {
            String sel = newV;
            String q = search.getText() == null ? "" : search.getText().trim().toLowerCase();
            filtered.setPredicate(ex -> {
                boolean matchText = q.isEmpty() || ex.getCategory().toLowerCase().contains(q) || ex.getNote().toLowerCase().contains(q);
                boolean matchMonth = sel == null || sel.equals("All Months") || java.time.YearMonth.from(ex.getDate()).toString().equals(sel);
                return matchText && matchMonth;
            });
        });
        
        SortedList<Expense> sorted = new SortedList<>(filtered);
        TableView<Expense> table = new TableView<>(sorted);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getStyleClass().add("table-view");
        table.setPrefHeight(500);

        TableColumn<Expense, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().getDateString()));
        dateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Expense, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().getCategory()));

        TableColumn<Expense, String> amountCol = new TableColumn<>("Amount (₹)");
        amountCol.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().getAmountString()));
        amountCol.setStyle("-fx-alignment: CENTER_RIGHT;");

        TableColumn<Expense, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().getNote()));

        table.getColumns().addAll(dateCol, categoryCol, amountCol, noteCol);
        sorted.comparatorProperty().bind(table.comparatorProperty());

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setPrefHeight(40);
        deleteButton.getStyleClass().add("btn-danger");

        Button editButton = new Button("Edit Selected");
        editButton.setPrefHeight(40);
        editButton.getStyleClass().add("btn-info");

        Label status = new Label("");
        status.getStyleClass().add("status-info");

        deleteButton.setOnAction(e -> {
            Expense selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                status.setText("Please select a row to delete.");
                status.getStyleClass().clear();
                status.getStyleClass().add("status-error");
                return;
            }
            expenses.remove(selected);
            dbManager.writeAll(expenses);
            status.setText("✓ Expense deleted!");
            status.getStyleClass().clear();
            status.getStyleClass().add("status-success");
        });

        editButton.setOnAction(e -> {
            Expense selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                status.setText("Please select a row to edit.");
                status.getStyleClass().clear();
                status.getStyleClass().add("status-error");
                return;
            }
            showEditDialog(selected, table);
        });

        Label stats = new Label();
        stats.getStyleClass().add("footer-stats");
        Runnable updateStats = () -> {
            int count = filtered.size();
            double total = filtered.stream().mapToDouble(Expense::getAmount).sum();
            stats.setText("Items: " + count + "    Total: ₹" + String.format(java.util.Locale.US, "%.2f", total));
        };
        filtered.addListener((javafx.collections.ListChangeListener<? super Expense>) c -> updateStats.run());
        updateStats.run();

        HBox actions = new HBox(15, deleteButton, editButton, status);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(10, 0, 0, 0));

        container.getChildren().addAll(toolbar, table, actions, stats);
        tab.setContent(container);
        return tab;
    }

    private void showEditDialog(Expense expense, TableView<Expense> table) {
        Dialog<Expense> dialog = new Dialog<>();
        dialog.setTitle("Edit Expense");
        dialog.setHeaderText("Edit expense details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        DatePicker datePicker = new DatePicker(expense.getDate());
        TextField categoryField = new TextField(expense.getCategory());
        TextField amountField = new TextField(String.valueOf(expense.getAmount()));
        TextArea noteArea = new TextArea(expense.getNote());
        noteArea.setPrefRowCount(3);

        grid.add(new Label("Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(new Label("Amount:"), 0, 2);
        grid.add(amountField, 1, 2);
        grid.add(new Label("Note:"), 0, 3);
        grid.add(noteArea, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    LocalDate date = datePicker.getValue();
                    String category = categoryField.getText().trim();
                    double amount = Double.parseDouble(amountField.getText().trim());
                    String note = noteArea.getText().trim();
                    
                    if (date != null && !category.isEmpty() && amount > 0 && expense.getExpenseId() != null) {
                        Expense updated = new Expense(expense.getExpenseId(), date, category, amount, note);
                        for (int i = 0; i < expenses.size(); i++) {
                            if (expenses.get(i).getExpenseId() != null && 
                                expenses.get(i).getExpenseId().equals(expense.getExpenseId())) {
                                expenses.set(i, updated);
                                dbManager.updateExpense(updated);
                                return updated;
                            }
                        }
                    }
                } catch (Exception ex) {
                }
            }
            return null;
        });

        java.util.Optional<Expense> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            table.refresh();
        }
    }
}
