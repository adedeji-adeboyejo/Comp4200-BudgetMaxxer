package com.group22.budgetmaxxer.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "color")
    private String color;

    // Constructor
    public Category(@NonNull String name, String color) {
        this.name = name;
        this.color = color;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(@NonNull String name) { this.name = name; }
    public void setColor(String color) { this.color = color; }
}