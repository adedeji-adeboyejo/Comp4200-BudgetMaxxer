package com.group22.budgetmaxxer.database;

import androidx.lifecycle.LiveData;
import androidx.room.OnConflictStrategy;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    // All expenses, newest first
    @Query("SELECT * FROM expenses ORDER BY created_at DESC")
    LiveData<List<Expense>> getAllExpenses();

    // Filter by month
    @Query("SELECT * FROM expenses WHERE date LIKE :monthPattern ORDER BY created_at DESC")
    LiveData<List<Expense>> getExpensesByMonth(String monthPattern);

    // Total spent in a month
    @Query("SELECT SUM(amount) FROM expenses WHERE date LIKE :monthPattern")
    LiveData<Double> getTotalForMonth(String monthPattern);

    // Total per category for a month
    @Query("SELECT category_id, SUM(amount) as total FROM expenses WHERE date LIKE :monthPattern GROUP BY category_id")
    LiveData<List<CategoryTotal>> getTotalsByCategory(String monthPattern);

    // 5 most recent (for Dashboard)
    @Query("SELECT * FROM expenses ORDER BY created_at DESC LIMIT 5")
    LiveData<List<Expense>> getRecentExpenses();

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    Expense getExpenseById(int id);
}