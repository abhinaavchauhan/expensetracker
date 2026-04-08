package com.expensetracker.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.expensetracker.data.local.entity.ExpenseEntity;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    long insert(ExpenseEntity expense);

    @Update
    void update(ExpenseEntity expense);

    @Delete
    void delete(ExpenseEntity expense);

    @Query("DELETE FROM expenses WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getAllExpenses();

    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit")
    LiveData<List<ExpenseEntity>> getRecentExpenses(int limit);

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getExpensesByDateRange(long startDate, long endDate);

    @Query("SELECT * FROM expenses WHERE type = :type ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getExpensesByType(String type);

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getExpensesByCategory(String category);

    @Query("SELECT * FROM expenses WHERE (category LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%') ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> searchExpenses(String query);

    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'income' AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalIncome(long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'expense' AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalExpense(long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'income'")
    LiveData<Double> getTotalIncomeAll();

    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'expense'")
    LiveData<Double> getTotalExpenseAll();

    @Query("SELECT * FROM expenses WHERE type = 'expense' AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getExpensesOnlyByDateRange(long startDate, long endDate);

    @Query("SELECT COUNT(*) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    LiveData<Integer> getTransactionCount(long startDate, long endDate);

    @Query("SELECT * FROM expenses WHERE " +
           "(:category IS NULL OR category = :category) AND " +
           "(:type IS NULL OR type = :type) AND " +
           "(:startDate IS NULL OR date >= :startDate) AND " +
           "(:endDate IS NULL OR date <= :endDate) " +
           "ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getFilteredExpenses(String category, String type, Long startDate, Long endDate);

    // Synchronous versions for background ops
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    List<ExpenseEntity> getAllExpensesSync();

    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'expense' AND date BETWEEN :startDate AND :endDate")
    Double getTotalExpenseSync(long startDate, long endDate);

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    List<ExpenseEntity> getExpensesByDateRangeSync(long startDate, long endDate);
}
