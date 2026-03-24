package com.group22.budgetmaxxer;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.group22.budgetmaxxer.database.Category;
import com.group22.budgetmaxxer.viewmodel.ExpenseViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class SummaryActivity extends AppCompatActivity {

    private ExpenseViewModel mViewModel;
    private TextView tvSummaryMonthLabel;
    private TextView tvSummaryTotal;
    private LocalDate currentMonth = LocalDate.now();
    private List<Category> allCategories;

    // You will need to build this adapter to show the progress bars!
    // private SummaryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSummary);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvSummaryMonthLabel = findViewById(R.id.tvSummaryMonthLabel);
        tvSummaryTotal = findViewById(R.id.tvSummaryTotal);

        RecyclerView recyclerView = findViewById(R.id.recyclerSummary);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // adapter = new SummaryAdapter();
        // recyclerView.setAdapter(adapter);

        mViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        // Load categories so we can match IDs to names and colors
        mViewModel.mAllCategories.observe(this, categories -> {
            allCategories = categories;
            // adapter.setCategories(categories);
        });

        setupMonthNavigation();
        updateMonthData();
    }

    private void setupMonthNavigation() {
        findViewById(R.id.btnSummaryPrevMonth).setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            updateMonthData();
        });

        findViewById(R.id.btnSummaryNextMonth).setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            updateMonthData();
        });
    }

    private void updateMonthData() {
        // Update Label
        tvSummaryMonthLabel.setText(currentMonth.format(
                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        ));

        String pattern = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-%";

        // Ensure switchMap updates the total
        mViewModel.setMonthFilter(pattern);

        // 1. Observe the total for the selected month
        mViewModel.monthTotal.observe(this, total -> {
            double monthlyTotal = total == null ? 0.0 : total;
            tvSummaryTotal.setText(String.format(Locale.getDefault(), "$%.2f", monthlyTotal));

            // Pass the total down to the adapter so it can calculate percentages!
            // if (adapter != null) adapter.setMonthlyTotal(monthlyTotal);
        });

        // 2. Observe the category breakdown for the selected month
        mViewModel.getTotalsByCategory(pattern).observe(this, categoryTotals -> {
            // Because your query groups by category_id, categories with $0 are automatically hidden!
            // adapter.setCategoryTotals(categoryTotals);
        });
    }
}