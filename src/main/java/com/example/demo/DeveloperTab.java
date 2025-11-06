package com.example.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

class DeveloperTab {
    Tab build() {
        Tab tab = new Tab("Developer");
        
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("developer-container");
        container.setPadding(new Insets(40));
        
        Label title = new Label("Expense Manager");
        title.getStyleClass().add("developer-title");
        
        Label subtitle = new Label("Developed By");
        subtitle.getStyleClass().add("developer-subtitle");
        
        VBox infoCard = new VBox(15);
        infoCard.getStyleClass().add("developer-card");
        infoCard.setAlignment(Pos.CENTER);
        infoCard.setPadding(new Insets(30));
        
        // Developer 1
        Label nameLabel1 = new Label("Diya Lunawat");
        nameLabel1.getStyleClass().add("developer-info");
        nameLabel1.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");


        
        Label descLabel = new Label("Simple, Beautiful Expense Management System");
        descLabel.getStyleClass().add("developer-info");
        descLabel.setStyle("-fx-font-size: 14px; -fx-opacity: 0.9;");

        infoCard.getChildren().addAll(nameLabel1,descLabel);
        container.getChildren().addAll(title, subtitle, infoCard);
        
        tab.setContent(container);
        return tab;
    }
}

