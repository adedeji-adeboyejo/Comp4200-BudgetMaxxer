package com.group22.budgetmaxxer.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.group22.budgetmaxxer.R;
import com.group22.budgetmaxxer.database.Expense;
import com.group22.budgetmaxxer.viewmodel.ExpenseViewModel;

public class HistoryActivity extends AppCompatActivity {

    private ExpenseViewModel mViewModel;
    private ExpenseAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new ExpenseAdapter(expense -> {
            // NOTE: If Member 2 hasn't created AddExpenseActivity yet, this might show red!
            // You can comment out the Intent lines temporarily if needed.
            Intent intent = new Intent(this, com.group22.budgetmaxxer.AddExpenseActivity.class);
            intent.putExtra("expense_id", expense.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(mAdapter);

        mViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        mViewModel.mAllExpenses.observe(this, expenses -> mAdapter.setExpenses(expenses));
        mViewModel.mAllCategories.observe(this, categories -> mAdapter.setCategories(categories));

        // Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Expense deleted = mAdapter.getExpenseAt(viewHolder.getAdapterPosition());
                mViewModel.delete(deleted);

                Snackbar.make(recyclerView, "Expense deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> mViewModel.insert(deleted))
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }
}