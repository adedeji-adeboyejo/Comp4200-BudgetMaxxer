package com.group22.budgetmaxxer.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Expense.class, Category.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ExpenseDao expenseDao();
    public abstract CategoryDao categoryDao();

    private static volatile AppDatabase INSTANCE;

    // ExecutorService for running DB operations off the main thread
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "pocket_budget_db")
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // This runs when the DB is first created — seeds default categories
    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    // Seed categories in the background
                    databaseWriteExecutor.execute(() -> {
                        CategoryDao dao = INSTANCE.categoryDao();
                        dao.insert(new Category("Food",      "🍔", "#F59E0B"));
                        dao.insert(new Category("Transport", "🚌", "#3B82F6"));
                        dao.insert(new Category("Bills",     "💡", "#8B5CF6"));
                        dao.insert(new Category("Shopping",  "🛍️", "#EC4899"));
                        dao.insert(new Category("Health",    "💊", "#10B981"));
                        dao.insert(new Category("Other",     "📦", "#9CA3AF"));
                    });
                }
            };
}