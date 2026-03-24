package com.group22.budgetmaxxer.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.group22.budgetmaxxer.database.Category;
import com.group22.budgetmaxxer.database.CategoryTotal;
import com.group22.budgetmaxxer.database.Expense;
import com.group22.budgetmaxxer.repository.ExpenseRepository;

import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;

    // LiveData streams for the UI to observe
    public final LiveData<List<Category>> mAllCategories;
    public final LiveData<List<Expense>> mRecentExpenses;
    public final LiveData<List<Expense>> mAllExpenses;

    // --- Dashboard Observer Bug Fix ---
    // We use a MutableLiveData to hold the current search pattern
    private final MutableLiveData<String> currentMonthFilter = new MutableLiveData<>();
    // switchMap listens to currentMonthFilter. When the filter changes, it automatically
    // swaps out the underlying database query without adding a new observer!
    public final LiveData<Double> monthTotal;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);

        // Wire up the standard lists
        mAllCategories = repository.getAllCategories();
        mRecentExpenses = repository.getRecentExpenses();
        mAllExpenses = repository.getAllExpenses();

        // Wire up the switchMap for the Dashboard total
        monthTotal = Transformations.switchMap(currentMonthFilter, pattern ->
                repository.getTotalForMonth(pattern)
        );
    }

    public LiveData<List<CategoryTotal>> getTotalsByCategory(String monthPattern) {
        return repository.getTotalsByCategory(monthPattern);
    }

    // Call this from DashboardActivity when the user clicks the arrow buttons
    public void setMonthFilter(String monthPattern) {
        currentMonthFilter.setValue(monthPattern);
    }

    // --- Write Operations (Delegated to Repository) ---
    public void insert(Expense expense) {
        repository.insert(expense);
    }

    public void update(Expense expense) {
        repository.update(expense);
    }

    public void delete(Expense expense) {
        repository.delete(expense);
    }
}