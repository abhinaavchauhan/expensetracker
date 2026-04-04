package com.expensetracker.presentation.transactions;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.expensetracker.data.local.entity.ExpenseEntity;
import com.expensetracker.data.repository.ExpenseRepository;

import java.util.List;

public class TransactionsViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;
    private final LiveData<List<ExpenseEntity>> allTransactions;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> filterType = new MutableLiveData<>("all");

    public TransactionsViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        allTransactions = repository.getAllExpenses();
    }

    public LiveData<List<ExpenseEntity>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<List<ExpenseEntity>> searchTransactions(String query) {
        return repository.searchExpenses(query);
    }

    public LiveData<List<ExpenseEntity>> getTransactionsByType(String type) {
        return repository.getExpensesByType(type);
    }

    public void deleteExpense(ExpenseEntity expense) {
        if (expense != null) {
            repository.delete(expense);
        }
    }

    public MutableLiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public MutableLiveData<String> getFilterType() {
        return filterType;
    }

    public LiveData<List<ExpenseEntity>> getTransactionsByCategory(String category) {
        return repository.getExpensesByCategory(category);
    }

    public LiveData<List<ExpenseEntity>> getTransactionsByDateRange(long start, long end) {
        return repository.getExpensesByDateRange(start, end);
    }
}
