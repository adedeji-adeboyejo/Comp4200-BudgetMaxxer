package com.group22.budgetmaxxer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.group22.budgetmaxxer.database.Expense;
import com.group22.budgetmaxxer.ui.ExpenseAdapter;
import com.group22.budgetmaxxer.viewmodel.ExpenseViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ExpenseViewModel mViewModel;
    private ExpenseAdapter adapter;
    private List<Expense> allExpensesList = new ArrayList<>();
    private String selectedMonthFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        MaterialToolbar toolbar = findViewById(R.id.toolbarHistory);
        toolbar.setNavigationOnClickListener(v -> finish());

        mViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        setupRecyclerView();
        setupSpinner();

        // Observe categories first so the adapter can map IDs to icons/names
        mViewModel.mAllCategories.observe(this, categories -> {
            adapter.setCategories(categories);
        });

        // Observe all expenses (already ordered by date descending in your DAO)
        mViewModel.mAllExpenses.observe(this, expenses -> {
            allExpensesList = expenses;
            applyFilter();
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Reusing your adapter from Dashboard! Tapping opens AddExpense pre-filled
        adapter = new ExpenseAdapter(expense -> {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            intent.putExtra(AddExpenseActivity.EXTRA_EXPENSE_ID, expense.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Swipe to Delete Logic
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder tgt) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Expense expenseToDelete = adapter.getExpenseAt(position);

                mViewModel.delete(expenseToDelete);

                // Show Snackbar with Undo option
                Snackbar.make(recyclerView, "Expense deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> mViewModel.insert(expenseToDelete))
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void setupSpinner() {
        Spinner spinner = findViewById(R.id.spinnerMonthFilter);
        // In a real scenario, you'd dynamically generate this list based on actual expense dates
        String[] options = {"All", "Current Month", "Previous Month"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonthFilter = options[position];
                applyFilter();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void applyFilter() {
        if (allExpensesList == null) return;

        List<Expense> filteredList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (Expense e : allExpensesList) {
            if (selectedMonthFilter.equals("All")) {
                filteredList.add(e);
            } else if (selectedMonthFilter.equals("Current Month") && e.getDate().startsWith(now.format(dbFormatter))) {
                filteredList.add(e);
            } else if (selectedMonthFilter.equals("Previous Month") && e.getDate().startsWith(now.minusMonths(1).format(dbFormatter))) {
                filteredList.add(e);
            }
        }
        adapter.setExpenses(filteredList);
    }
}
