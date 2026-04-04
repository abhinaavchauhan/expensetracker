package com.expensetracker.domain.usecase;

import android.app.Application;

import com.expensetracker.data.local.entity.ExpenseEntity;
import com.expensetracker.data.repository.ExpenseRepository;

public class DeleteExpenseUseCase {

    private final ExpenseRepository repository;

    public DeleteExpenseUseCase(Application application) {
        this.repository = new ExpenseRepository(application);
    }

    public void execute(ExpenseEntity expense) {
        if (expense == null) {
            throw new IllegalArgumentException("Expense cannot be null");
        }
        repository.delete(expense);
    }

    public void executeById(int id) {
        repository.deleteById(id);
    }
}
