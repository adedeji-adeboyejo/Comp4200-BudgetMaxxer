package com.group22.budgetmaxxer;

import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ExpenseViewModel extends ViewModel {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
    private static final Map<Integer, Expense> EXPENSE_STORE = new ConcurrentHashMap<>();

    public Expense getExpenseById(int expenseId) {
        return EXPENSE_STORE.get(expenseId);
    }

    public int insertExpense(double amount, String category, String description, LocalDate date) {
        int id = ID_GENERATOR.getAndIncrement();
        Expense expense = new Expense(id, amount, category, description, date);
        EXPENSE_STORE.put(id, expense);
        return id;
    }

    public void updateExpense(int expenseId, double amount, String category, String description, LocalDate date) {
        Expense existing = EXPENSE_STORE.get(expenseId);
        if (existing == null) {
            return;
        }
        existing.setAmount(amount);
        existing.setCategory(category);
        existing.setDescription(description);
        existing.setDate(date);
    }

    public void deleteExpense(int expenseId) {
        EXPENSE_STORE.remove(expenseId);
    }
}
