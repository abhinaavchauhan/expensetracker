package com.expensetracker.domain.usecase;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.expensetracker.data.local.entity.ExpenseEntity;
import com.expensetracker.data.repository.ExpenseRepository;

import java.util.List;

public class GetExpensesUseCase {

    private final ExpenseRepository repository;

    public GetExpensesUseCase(Application application) {
        this.repository = new ExpenseRepository(application);
    }

    public LiveData<List<ExpenseEntity>> getAllExpenses() {
        return repository.getAllExpenses();
    }

    public LiveData<List<ExpenseEntity>> getRecentExpenses(int limit) {
        return repository.getRecentExpenses(limit);
    }

    public LiveData<List<ExpenseEntity>> getExpensesByDateRange(long startDate, long endDate) {
        return repository.getExpensesByDateRange(startDate, endDate);
    }

    public LiveData<List<ExpenseEntity>> getExpensesByType(String type) {
        return repository.getExpensesByType(type);
    }

    public LiveData<List<ExpenseEntity>> searchExpenses(String query) {
        return repository.searchExpenses(query);
    }
}
