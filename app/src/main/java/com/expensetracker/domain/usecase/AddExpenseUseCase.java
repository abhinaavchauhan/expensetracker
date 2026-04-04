package com.expensetracker.domain.usecase;

import android.app.Application;

import com.expensetracker.data.local.entity.ExpenseEntity;
import com.expensetracker.data.repository.ExpenseRepository;

public class AddExpenseUseCase {

    private final ExpenseRepository repository;

    public AddExpenseUseCase(Application application) {
        this.repository = new ExpenseRepository(application);
    }

    public void execute(double amount, String category, String note, long date, String type) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (category == null || category.isEmpty()) {
            throw new IllegalArgumentException("Category is required");
        }
        if (type == null || (!type.equals("income") && !type.equals("expense"))) {
            throw new IllegalArgumentException("Type must be 'income' or 'expense'");
        }

        ExpenseEntity expense = new ExpenseEntity(amount, category,
                note != null ? note : "", date > 0 ? date : System.currentTimeMillis(), type);
        repository.insert(expense);
    }
}
