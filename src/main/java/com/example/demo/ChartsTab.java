package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.stream.Collectors;

class ChartsTab {
    final ObservableList<Expense> expenses;

    ChartsTab(ObservableList<Expense> list) { expenses = list; }

    Tab build() {
        Tab tab = new Tab("Charts & Analytics");

        VBox container = new VBox(20);
        container.setPadding(new Insets(25));
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("page-background");

        PieChart pieChart = new PieChart();
        pieChart.getStyleClass().add("chart");
        pieChart.setPrefSize(400, 350);
        
        BarChart<String, Number> barChart = createBarChart();
        barChart.getStyleClass().add("chart");
        barChart.setPrefSize(400, 350);
        
        Label totalLabel = new Label();
        totalLabel.getStyleClass().add("chart-title");
        
        CategoryAxis mx = new CategoryAxis();
        NumberAxis my = new NumberAxis();
        LineChart<String, Number> monthLine = new LineChart<>(mx, my);
        monthLine.getStyleClass().add("chart");
        monthLine.setPrefSize(800, 300);
        monthLine.setTitle("Last 3 Months Total");
        monthLine.setLegendVisible(false);
        my.setLabel("Amount (₹)");
        
        Label thisMonthLabel = new Label();
        thisMonthLabel.getStyleClass().add("status-info");
        thisMonthLabel.setStyle("-fx-font-size: 16px;");
        
        Label prevMonthLabel = new Label();
        prevMonthLabel.getStyleClass().add("status-info");
        prevMonthLabel.setStyle("-fx-font-size: 16px;");

        Runnable refresh = () -> {
            double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
            totalLabel.setText(String.format(java.util.Locale.US, "Total Expenses: ₹%.2f", total));

            Map<String, Double> sums = expenses.stream()
                    .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)));

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (Map.Entry<String, Double> entry : sums.entrySet()) {
                pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            pieChart.setData(pieData);
            pieChart.setTitle("Expenses by Category");
            pieChart.setLabelsVisible(true);

            updateBarChart(barChart, sums);

            java.time.YearMonth now = java.time.YearMonth.now();
            java.time.YearMonth prev1 = now.minusMonths(1);
            java.time.YearMonth prev2 = now.minusMonths(2);
            Map<java.time.YearMonth, Double> byMonth = new HashMap<>();
            byMonth.put(now, 0.0); byMonth.put(prev1, 0.0); byMonth.put(prev2, 0.0);
            for (Expense ex : expenses) {
                java.time.YearMonth ym = java.time.YearMonth.from(ex.getDate());
                if (byMonth.containsKey(ym)) byMonth.put(ym, byMonth.get(ym) + ex.getAmount());
            }
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            java.util.List<java.time.YearMonth> order = java.util.Arrays.asList(prev2, prev1, now);
            java.util.List<String> labels = new java.util.ArrayList<>();
            for (java.time.YearMonth ym : order) {
                String label = ym.getMonth().toString().substring(0,3) + " " + ym.getYear();
                labels.add(label);
                series.getData().add(new XYChart.Data<>(label, byMonth.get(ym)));
            }
            mx.setCategories(FXCollections.observableArrayList(labels));
            monthLine.getData().setAll(series);
            thisMonthLabel.setText(String.format(java.util.Locale.US, "This month: ₹%.2f", byMonth.get(now)));
            prevMonthLabel.setText(String.format(java.util.Locale.US, "Previous month: ₹%.2f", byMonth.get(prev1)));
        };

        expenses.addListener((javafx.collections.ListChangeListener<? super Expense>) change -> refresh.run());
        refresh.run();

        HBox chartsBox = new HBox(25, pieChart, barChart);
        chartsBox.setPadding(new Insets(20));
        chartsBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, totalLabel, thisMonthLabel, prevMonthLabel, chartsBox, monthLine);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        container.getChildren().add(root);
        tab.setContent(container);
        return tab;
    }

    BarChart<String, Number> createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Expenses by Category");
        barChart.setLegendVisible(false);
        xAxis.setLabel("Category");
        yAxis.setLabel("Amount (₹)");
        return barChart;
    }

    void updateBarChart(BarChart<String, Number> barChart, Map<String, Double> data) {
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        java.util.List<Map.Entry<String, Double>> sorted = new ArrayList<>(data.entrySet());
        sorted.sort(Map.Entry.comparingByValue(java.util.Comparator.reverseOrder()));
        for (Map.Entry<String, Double> entry : sorted) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        barChart.getData().add(series);
    }
}
