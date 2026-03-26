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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {

    private ExpenseViewModel mViewModel;
    private ExpenseAdapter adapter;
    private ArrayAdapter<String> spinnerAdapter;
    private Spinner spinner;

    private List<Expense> allExpensesList = new ArrayList<>();

    // Maps display label "March 2026" → pattern "2026-03-%"
    // LinkedHashMap preserves insertion order (newest first)
    private final Map<String, String> monthLabelToPattern = new LinkedHashMap<>();
    private String selectedPattern = null; // null means "All"

    // Formatters
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        MaterialToolbar toolbar = findViewById(R.id.toolbarHistory);
        toolbar.setNavigationOnClickListener(v -> finish());

        mViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        setupRecyclerView();
        setupSpinner();

        // Observe categories so the adapter can map IDs to icons/names
        mViewModel.mAllCategories.observe(this, categories ->
                adapter.setCategories(categories)
        );

        // Observe all expenses — rebuild spinner options + re-filter on every change
        mViewModel.mAllExpenses.observe(this, expenses -> {
            allExpensesList = expenses;
            rebuildSpinnerOptions(expenses);
            applyFilter();
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ExpenseAdapter(expense -> {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            intent.putExtra(AddExpenseActivity.EXTRA_EXPENSE_ID, expense.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder tgt) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                 int direction) {
                int position = viewHolder.getAdapterPosition();
                Expense expenseToDelete = adapter.getExpenseAt(position);
                mViewModel.delete(expenseToDelete);

                Snackbar.make(recyclerView, "Expense deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> mViewModel.insert(expenseToDelete))
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void setupSpinner() {
        spinner = findViewById(R.id.spinnerMonthFilter);

        // Start with just "All" — options are rebuilt once expenses load
        List<String> initialOptions = new ArrayList<>();
        initialOptions.add("All");

        spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                initialOptions
        );
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String label = spinnerAdapter.getItem(position);
                // "All" → no pattern filter; anything else → look up its pattern
                selectedPattern = "All".equals(label)
                        ? null
                        : monthLabelToPattern.get(label);
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void rebuildSpinnerOptions(List<Expense> expenses) {
        // Remember what was selected before rebuilding
        String previousLabel = (String) spinner.getSelectedItem();

        monthLabelToPattern.clear();

        // Extract unique yyyy-MM values from all expense dates, newest first
        List<String> seenMonths = new ArrayList<>();
        for (Expense e : expenses) {
            if (e.getDate() == null || e.getDate().length() < 7) continue;
            String yearMonth = e.getDate().substring(0, 7); // "yyyy-MM"
            if (!seenMonths.contains(yearMonth)) {
                seenMonths.add(yearMonth);
            }
        }

        // Sort descending (newest first) — substring format sorts lexicographically
        seenMonths.sort((a, b) -> b.compareTo(a));

        // Build display labels and patterns
        for (String ym : seenMonths) {
            try {
                YearMonth yearMonth = YearMonth.parse(ym, YEAR_MONTH_FORMATTER);
                String label = yearMonth.format(DISPLAY_FORMATTER);  // "March 2026"
                String pattern = ym + "-%";                           // "2026-03-%"
                monthLabelToPattern.put(label, pattern);
            } catch (Exception e) {
                // Skip malformed date entries
            }
        }

        // Rebuild spinner list: "All" first, then each month
        List<String> options = new ArrayList<>();
        options.add("All");
        options.addAll(monthLabelToPattern.keySet());

        spinnerAdapter.clear();
        spinnerAdapter.addAll(options);
        spinnerAdapter.notifyDataSetChanged();

        // Restore previous selection if it still exists, otherwise default to "All"
        if (previousLabel != null && options.contains(previousLabel)) {
            spinner.setSelection(options.indexOf(previousLabel));
        } else {
            spinner.setSelection(0);
        }
    }

    private void applyFilter() {
        if (allExpensesList == null) return;

        List<Expense> filtered = new ArrayList<>();

        if (selectedPattern == null) {
            // "All" — no filter
            filtered.addAll(allExpensesList);
        } else {
            // e.g. pattern = "2026-03-%" → match dates starting with "2026-03-"
            String prefix = selectedPattern.replace("%", "");
            for (Expense e : allExpensesList) {
                if (e.getDate() != null && e.getDate().startsWith(prefix)) {
                    filtered.add(e);
                }
            }
        }

        adapter.setExpenses(filtered);
    }
}
