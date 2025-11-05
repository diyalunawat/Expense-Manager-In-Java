package com.example.demo;

import java.time.LocalDate;
import java.util.Locale;

class Expense {
    final LocalDate date;
    final String category;
    final double amount;
    final String note;
    Integer expenseId;  // Database ID for updates

    Expense(LocalDate dateParam, String categoryParam, double amountParam, String noteParam) {
        date = dateParam;
        category = categoryParam == null ? "" : categoryParam;
        amount = amountParam;
        note = noteParam == null ? "" : noteParam;
    }

    Expense(int id, LocalDate dateParam, String categoryParam, double amountParam, String noteParam) {
        this(dateParam, categoryParam, amountParam, noteParam);
        expenseId = id;
    }

    LocalDate getDate() { 
        return date; 
    }
    
    String getCategory() { 
        return category; 
    }
    
    double getAmount() { 
        return amount; 
    }
    
    String getNote() { 
        return note; 
    }

    Integer getExpenseId() {
        return expenseId;
    }

    String getDateString() { 
        return date.toString(); 
    }
    
    String getAmountString() { 
        return String.format(Locale.US, "₹%.2f", amount); 
    }
}
