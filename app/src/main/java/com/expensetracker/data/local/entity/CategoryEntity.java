package com.expensetracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String icon;
    private String color;

    public CategoryEntity() {}

    public CategoryEntity(String name, String icon, String color) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public String getColor() { return color; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setColor(String color) { this.color = color; }
}
