package com.expensetracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "expenses")
public class ExpenseEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private double amount;
    private String category;
    private String note;
    private long date;
    private String type; // "income" or "expense"

    @Ignore
    public ExpenseEntity() {}

    public ExpenseEntity(double amount, String category, String note, long date, String type) {
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
}
