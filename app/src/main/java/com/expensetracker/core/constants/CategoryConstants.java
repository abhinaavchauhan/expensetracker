package com.expensetracker.core.constants;

import com.expensetracker.R;
import com.expensetracker.domain.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryConstants {

    public static final String FOOD = "Food";
    public static final String TRANSPORT = "Transport";
    public static final String SHOPPING = "Shopping";
    public static final String ENTERTAINMENT = "Entertainment";
    public static final String BILLS = "Bills";
    public static final String HEALTH = "Health";
    public static final String EDUCATION = "Education";
    public static final String TRAVEL = "Travel";
    public static final String GROCERIES = "Groceries";
    public static final String RENT = "Rent";
    public static final String SALARY = "Salary";
    public static final String INVESTMENT = "Investment";
    public static final String FREELANCE = "Freelance";
    public static final String GIFT = "Gift";
    public static final String OTHER = "Other";

    public static List<Category> getExpenseCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(FOOD, R.drawable.ic_food, "#FF6B6B"));
        categories.add(new Category(TRANSPORT, R.drawable.ic_transport, "#4DABF7"));
        categories.add(new Category(SHOPPING, R.drawable.ic_shopping, "#FF9F43"));
        categories.add(new Category(ENTERTAINMENT, R.drawable.ic_entertainment, "#A66CFF"));
        categories.add(new Category(BILLS, R.drawable.ic_bills, "#FF6B6B"));
        categories.add(new Category(HEALTH, R.drawable.ic_health, "#00C9A7"));
        categories.add(new Category(EDUCATION, R.drawable.ic_education, "#4DABF7"));
        categories.add(new Category(TRAVEL, R.drawable.ic_travel, "#FFD93D"));
        categories.add(new Category(GROCERIES, R.drawable.ic_groceries, "#6BCB77"));
        categories.add(new Category(RENT, R.drawable.ic_rent, "#E84393"));
        categories.add(new Category(OTHER, R.drawable.ic_other, "#A0A5B5"));
        return categories;
    }

    public static List<Category> getIncomeCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(SALARY, R.drawable.ic_salary, "#00C9A7"));
        categories.add(new Category(INVESTMENT, R.drawable.ic_investment, "#6C63FF"));
        categories.add(new Category(FREELANCE, R.drawable.ic_freelance, "#45B7D1"));
        categories.add(new Category(GIFT, R.drawable.ic_gift, "#FF9FF3"));
        categories.add(new Category(OTHER, R.drawable.ic_other, "#A0A5B5"));
        return categories;
    }

    public static List<Category> getAllCategories() {
        List<Category> categories = getExpenseCategories();
        for (Category income : getIncomeCategories()) {
            boolean exists = false;
            for (Category expense : categories) {
                if (expense.getName().equals(income.getName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                categories.add(income);
            }
        }
        return categories;
    }

    public static int getCategoryIcon(String categoryName) {
        if (categoryName == null) return R.drawable.ic_other;
        switch (categoryName) {
            case FOOD: return R.drawable.ic_food;
            case TRANSPORT: return R.drawable.ic_transport;
            case SHOPPING: return R.drawable.ic_shopping;
            case ENTERTAINMENT: return R.drawable.ic_entertainment;
            case BILLS: return R.drawable.ic_bills;
            case HEALTH: return R.drawable.ic_health;
            case EDUCATION: return R.drawable.ic_education;
            case TRAVEL: return R.drawable.ic_travel;
            case GROCERIES: return R.drawable.ic_groceries;
            case RENT: return R.drawable.ic_rent;
            case SALARY: return R.drawable.ic_salary;
            case INVESTMENT: return R.drawable.ic_investment;
            case FREELANCE: return R.drawable.ic_freelance;
            case GIFT: return R.drawable.ic_gift;
            default: return R.drawable.ic_other;
        }
    }

    public static int getCategoryColor(String categoryName) {
        if (categoryName == null) return android.graphics.Color.parseColor("#A0A5B5");
        switch (categoryName) {
            case FOOD: return android.graphics.Color.parseColor("#FF6B6B");
            case TRANSPORT: return android.graphics.Color.parseColor("#4DABF7");
            case SHOPPING: return android.graphics.Color.parseColor("#FF9F43");
            case ENTERTAINMENT: return android.graphics.Color.parseColor("#A66CFF");
            case BILLS: return android.graphics.Color.parseColor("#FF6B6B");
            case HEALTH: return android.graphics.Color.parseColor("#00C9A7");
            case EDUCATION: return android.graphics.Color.parseColor("#4DABF7");
            case TRAVEL: return android.graphics.Color.parseColor("#FFD93D");
            case GROCERIES: return android.graphics.Color.parseColor("#6BCB77");
            case RENT: return android.graphics.Color.parseColor("#E84393");
            case SALARY: return android.graphics.Color.parseColor("#00C9A7");
            case INVESTMENT: return android.graphics.Color.parseColor("#6C63FF");
            case FREELANCE: return android.graphics.Color.parseColor("#45B7D1");
            case GIFT: return android.graphics.Color.parseColor("#FF9FF3");
            default: return android.graphics.Color.parseColor("#A0A5B5");
        }
    }
}
