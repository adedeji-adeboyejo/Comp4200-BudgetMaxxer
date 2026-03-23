package com.group22.budgetmaxxer.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class Expense {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "amount")
    private double amount;

    @ColumnInfo(name = "category_id")
    private int categoryId;

    @ColumnInfo(name = "description")
    private String description;

    @NonNull
    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // Constructor
    public Expense(double amount, int categoryId, String description,
                   @NonNull String date, long createdAt) {
        this.amount = amount;
        this.categoryId = categoryId;
        this.description = description;
        this.date = date;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public double getAmount() { return amount; }
    public int getCategoryId() { return categoryId; }
    public String getDescription() { return description; }
    public String getDate() { return date; }

    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(@NonNull String date) { this.date = date; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}