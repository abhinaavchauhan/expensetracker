package com.expensetracker.presentation.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.expensetracker.core.utils.DateUtils;
import com.expensetracker.data.local.entity.ExpenseEntity;
import com.expensetracker.data.repository.ExpenseRepository;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;
    private final LiveData<List<ExpenseEntity>> recentTransactions;
    private final LiveData<Double> monthlyIncome;
    private final LiveData<Double> monthlyExpense;
    private final LiveData<Double> totalIncome;
    private final LiveData<Double> totalExpense;
    private final LiveData<List<ExpenseEntity>> monthlyExpenses;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);

        long startOfMonth = DateUtils.getStartOfMonth();
        long endOfMonth = DateUtils.getEndOfMonth();

        recentTransactions = repository.getRecentExpenses(5);
        monthlyIncome = repository.getTotalIncome(startOfMonth, endOfMonth);
        monthlyExpense = repository.getTotalExpense(startOfMonth, endOfMonth);
        totalIncome = repository.getTotalIncomeAll();
        totalExpense = repository.getTotalExpenseAll();
        monthlyExpenses = repository.getExpensesOnlyByDateRange(startOfMonth, endOfMonth);
    }

    public LiveData<List<ExpenseEntity>> getRecentTransactions() {
        return recentTransactions;
    }

    public LiveData<Double> getMonthlyIncome() {
        return monthlyIncome;
    }

    public LiveData<Double> getMonthlyExpense() {
        return monthlyExpense;
    }

    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }

    public LiveData<List<ExpenseEntity>> getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public void deleteExpense(ExpenseEntity expense) {
        if (expense != null) {
            repository.delete(expense);
        }
    }
}
