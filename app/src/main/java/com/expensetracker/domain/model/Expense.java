package com.expensetracker.domain.model;

public class Expense {
    private int id;
    private double amount;
    private String category;
    private String note;
    private long date;
    private String type;

    public Expense() {}

    public Expense(double amount, String category, String note, long date, String type) {
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.date = date;
        this.type = type;
    }

    // Getters
    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getNote() { return note; }
    public long getDate() { return date; }
    public String getType() { return type; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setCategory(String category) { this.category = category; }
    public void setNote(String note) { this.note = note; }
    public void setDate(long date) { this.date = date; }
    public void setType(String type) { this.type = type; }

    public boolean isIncome() {
        return "income".equalsIgnoreCase(type);
    }

    public boolean isExpense() {
        return "expense".equalsIgnoreCase(type);
    }
}
