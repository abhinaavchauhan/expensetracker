package com.expensetracker.domain.model;

public class Category {
    private int id;
    private String name;
    private int iconResId;
    private String color;
    private boolean isSelected;

    public Category(String name, int iconResId, String color) {
        this.name = name;
        this.iconResId = iconResId;
        this.color = color;
        this.isSelected = false;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getIconResId() { return iconResId; }
    public String getColor() { return color; }
    public boolean isSelected() { return isSelected; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
    public void setColor(String color) { this.color = color; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
