package com.expensetracker.presentation.addexpense;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.expensetracker.data.local.entity.ExpenseEntity;
import com.expensetracker.data.repository.ExpenseRepository;

public class AddExpenseViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;
    private final MutableLiveData<Boolean> savingResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AddExpenseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
    }

    public void saveExpense(double amount, String category, String note, long date, String type) {
        // Validation
        if (amount <= 0) {
            errorMessage.setValue("Please enter a valid amount");
            return;
        }
        if (category == null || category.isEmpty()) {
            errorMessage.setValue("Please select a category");
            return;
        }
        if (date <= 0) {
            date = System.currentTimeMillis();
        }
        if (note == null) {
            note = "";
        }

        ExpenseEntity expense = new ExpenseEntity(amount, category, note, date, type);
        repository.insert(expense);
        savingResult.setValue(true);
    }

    public MutableLiveData<Boolean> getSavingResult() {
        return savingResult;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
