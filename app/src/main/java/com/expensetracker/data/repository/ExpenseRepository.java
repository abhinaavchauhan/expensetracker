package com.expensetracker.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.expensetracker.data.local.dao.ExpenseDao;
import com.expensetracker.data.local.db.ExpenseDatabase;
import com.expensetracker.data.local.entity.ExpenseEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {

    private final ExpenseDao expenseDao;
    private final ExecutorService executorService;

    public ExpenseRepository(Application application) {
        ExpenseDatabase db = ExpenseDatabase.getInstance(application);
        expenseDao = db.expenseDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    // Insert
    public void insert(ExpenseEntity expense) {
        executorService.execute(() -> expenseDao.insert(expense));
    }

    // Update
    public void update(ExpenseEntity expense) {
        executorService.execute(() -> expenseDao.update(expense));
    }

    // Delete
    public void delete(ExpenseEntity expense) {
        executorService.execute(() -> expenseDao.delete(expense));
    }

    // Delete by ID
    public void deleteById(int id) {
        executorService.execute(() -> expenseDao.deleteById(id));
    }

    // Get all expenses (LiveData - observed on main thread safely)
    public LiveData<List<ExpenseEntity>> getAllExpenses() {
        return expenseDao.getAllExpenses();
    }

    // Get recent expenses
    public LiveData<List<ExpenseEntity>> getRecentExpenses(int limit) {
        return expenseDao.getRecentExpenses(limit);
    }

    // Get expenses by date range
    public LiveData<List<ExpenseEntity>> getExpensesByDateRange(long startDate, long endDate) {
        return expenseDao.getExpensesByDateRange(startDate, endDate);
    }

    // Get by type
    public LiveData<List<ExpenseEntity>> getExpensesByType(String type) {
        return expenseDao.getExpensesByType(type);
    }

    // Get by category
    public LiveData<List<ExpenseEntity>> getExpensesByCategory(String category) {
        return expenseDao.getExpensesByCategory(category);
    }

    // Search
    public LiveData<List<ExpenseEntity>> searchExpenses(String query) {
        return expenseDao.searchExpenses(query);
    }

    // Totals
    public LiveData<Double> getTotalIncome(long startDate, long endDate) {
        return expenseDao.getTotalIncome(startDate, endDate);
    }

    public LiveData<Double> getTotalExpense(long startDate, long endDate) {
        return expenseDao.getTotalExpense(startDate, endDate);
    }

    public LiveData<Double> getTotalIncomeAll() {
        return expenseDao.getTotalIncomeAll();
    }

    public LiveData<Double> getTotalExpenseAll() {
        return expenseDao.getTotalExpenseAll();
    }

    public LiveData<List<ExpenseEntity>> getExpensesOnlyByDateRange(long startDate, long endDate) {
        return expenseDao.getExpensesOnlyByDateRange(startDate, endDate);
    }

    public LiveData<Integer> getTransactionCount(long startDate, long endDate) {
        return expenseDao.getTransactionCount(startDate, endDate);
    }

    // Sync versions for export
    public List<ExpenseEntity> getAllExpensesSync() {
        return expenseDao.getAllExpensesSync();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
