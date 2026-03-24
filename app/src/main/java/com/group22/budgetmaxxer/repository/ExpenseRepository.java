package com.group22.budgetmaxxer.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.group22.budgetmaxxer.database.AppDatabase;
import com.group22.budgetmaxxer.database.Category;
import com.group22.budgetmaxxer.database.CategoryDao;
import com.group22.budgetmaxxer.database.CategoryTotal;
import com.group22.budgetmaxxer.database.Expense;
import com.group22.budgetmaxxer.database.ExpenseDao;

import java.util.List;

public class ExpenseRepository {

    private ExpenseDao mExpenseDao;
    private CategoryDao mCategoryDao;

    private LiveData<List<Expense>> mAllExpenses;
    private LiveData<List<Expense>> mRecentExpenses;
    private LiveData<List<Category>> mAllCategories;

    public ExpenseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mExpenseDao = db.expenseDao();
        mCategoryDao = db.categoryDao();
        mAllExpenses = mExpenseDao.getAllExpenses();
        mRecentExpenses = mExpenseDao.getRecentExpenses();
        mAllCategories = mCategoryDao.getAllCategories();
    }

    // --- Getters (these return LiveData — no background thread needed) ---

    public LiveData<List<Expense>> getAllExpenses() {
        return mAllExpenses;
    }

    public LiveData<List<Expense>> getRecentExpenses() {
        return mRecentExpenses;
    }

    public LiveData<List<Category>> getAllCategories() {
        return mAllCategories;
    }

    public LiveData<List<Expense>> getExpensesByMonth(String monthPattern) {
        return mExpenseDao.getExpensesByMonth(monthPattern);
    }

    public LiveData<Double> getTotalForMonth(String monthPattern) {
        return mExpenseDao.getTotalForMonth(monthPattern);
    }

    public LiveData<List<CategoryTotal>> getTotalsByCategory(String monthPattern) {
        return mExpenseDao.getTotalsByCategory(monthPattern);
    }

    // --- Write operations (MUST run on background thread) ---

    public void insert(Expense expense) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                mExpenseDao.insert(expense)
        );
    }

    public void update(Expense expense) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                mExpenseDao.update(expense)
        );
    }

    public void delete(Expense expense) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                mExpenseDao.delete(expense)
        );
    }
}