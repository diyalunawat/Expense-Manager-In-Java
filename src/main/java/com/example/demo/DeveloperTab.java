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
        
        Label rollLabel1 = new Label("Roll Number: A-015");
        rollLabel1.getStyleClass().add("developer-info");
        
        // Developer 2
        Label nameLabel2 = new Label("Aishwarya Singh");
        nameLabel2.getStyleClass().add("developer-info");
        nameLabel2.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label rollLabel2 = new Label("Roll Number: A-004");
        rollLabel2.getStyleClass().add("developer-info");
        
        // Developer 3
        Label nameLabel3 = new Label("Maahika Elenjikal");
        nameLabel3.getStyleClass().add("developer-info");
        nameLabel3.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label rollLabel3 = new Label("Roll Number: A-029");
        rollLabel3.getStyleClass().add("developer-info");
        
        Label descLabel = new Label("Simple, Beautiful Expense Management System");
        descLabel.getStyleClass().add("developer-info");
        descLabel.setStyle("-fx-font-size: 14px; -fx-opacity: 0.9;");
        
        infoCard.getChildren().addAll(nameLabel1, rollLabel1, nameLabel2, rollLabel2, nameLabel3, rollLabel3, descLabel);
        container.getChildren().addAll(title, subtitle, infoCard);
        
        tab.setContent(container);
        return tab;
    }
}

