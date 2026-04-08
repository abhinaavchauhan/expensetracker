package com.expensetracker.presentation.transactions;

import com.expensetracker.data.local.entity.ExpenseEntity;
import java.util.Objects;

public class TransactionListItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TRANSACTION = 1;

    private final int type;
    private final String monthYear;
    private final ExpenseEntity expense;

    public TransactionListItem(String monthYear) {
        this.type = TYPE_HEADER;
        this.monthYear = monthYear;
        this.expense = null;
    }

    public TransactionListItem(ExpenseEntity expense) {
        this.type = TYPE_TRANSACTION;
        this.expense = expense;
        this.monthYear = null;
    }

    public int getType() {
        return type;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public ExpenseEntity getExpense() {
        return expense;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionListItem that = (TransactionListItem) o;
        if (type != that.type) return false;
        if (type == TYPE_HEADER) {
            return Objects.equals(monthYear, that.monthYear);
        } else {
            return expense != null && that.expense != null && expense.getId() == that.expense.getId();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, monthYear, expense);
    }
}
