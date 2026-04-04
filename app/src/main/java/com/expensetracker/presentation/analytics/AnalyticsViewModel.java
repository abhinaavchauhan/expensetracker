package com.expensetracker.presentation.analytics;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.expensetracker.core.utils.DateUtils;
import com.expensetracker.data.local.entity.ExpenseEntity;
import com.expensetracker.data.repository.ExpenseRepository;

import java.util.List;

public class AnalyticsViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;

    public AnalyticsViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
    }

    public LiveData<List<ExpenseEntity>> getExpensesByDateRange(long start, long end) {
        return repository.getExpensesByDateRange(start, end);
    }

    public LiveData<List<ExpenseEntity>> getExpensesOnlyByDateRange(long start, long end) {
        return repository.getExpensesOnlyByDateRange(start, end);
    }

    public LiveData<Double> getTotalExpense(long start, long end) {
        return repository.getTotalExpense(start, end);
    }

    public LiveData<Integer> getTransactionCount(long start, long end) {
        return repository.getTransactionCount(start, end);
    }

    public LiveData<List<ExpenseEntity>> getWeeklyExpenses() {
        return repository.getExpensesByDateRange(DateUtils.getStartOfWeek(), DateUtils.getEndOfWeek());
    }

    public LiveData<List<ExpenseEntity>> getMonthlyExpenses() {
        return repository.getExpensesByDateRange(DateUtils.getStartOfMonth(), DateUtils.getEndOfMonth());
    }

    public LiveData<List<ExpenseEntity>> getYearlyExpenses() {
        return repository.getExpensesByDateRange(DateUtils.getStartOfYear(), DateUtils.getEndOfYear());
    }
}
