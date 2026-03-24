package com.group22.budgetmaxxer.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.group22.budgetmaxxer.database.Category;
import com.group22.budgetmaxxer.database.CategoryTotal;
import com.group22.budgetmaxxer.database.Expense;
import com.group22.budgetmaxxer.repository.ExpenseRepository;

import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {

    private ExpenseRepository mRepository;

    public LiveData<List<Expense>> mAllExpenses;
    public LiveData<List<Expense>> mRecentExpenses;
    public LiveData<List<Category>> mAllCategories;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        mRepository = new ExpenseRepository(application);
        mAllExpenses = mRepository.getAllExpenses();
        mRecentExpenses = mRepository.getRecentExpenses();
        mAllCategories = mRepository.getAllCategories();
    }

    public void insert(Expense expense) { mRepository.insert(expense); }
    public void update(Expense expense) { mRepository.update(expense); }
    public void delete(Expense expense) { mRepository.delete(expense); }

    public LiveData<List<Expense>> getExpensesByMonth(String monthPattern) {
        return mRepository.getExpensesByMonth(monthPattern);
    }

    public LiveData<Double> getTotalForMonth(String monthPattern) {
        return mRepository.getTotalForMonth(monthPattern);
    }

    public LiveData<List<CategoryTotal>> getTotalsByCategory(String monthPattern) {
        return mRepository.getTotalsByCategory(monthPattern);
    }
}