package com.group22.budgetmaxxer;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.group22.budgetmaxxer.ui.SummaryAdapter;
import com.group22.budgetmaxxer.viewmodel.ExpenseViewModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SummaryActivity extends AppCompatActivity {

    private ExpenseViewModel mViewModel;
    private TextView tvSummaryMonthLabel;
    private TextView tvSummaryTotal;
    private LocalDate currentMonth = LocalDate.now();
    private SummaryAdapter adapter;

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
        adapter = new SummaryAdapter();
        recyclerView.setAdapter(adapter);

        mViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        // Observed once — categories passed to adapter for name/icon/color lookup
        mViewModel.mAllCategories.observe(this, categories -> {
            adapter.setCategories(categories);
        });

        // Observed once in onCreate — not inside updateMonthData to prevent stacking
        mViewModel.monthTotal.observe(this, total -> {
            double monthlyTotal = total == null ? 0.0 : total;
            tvSummaryTotal.setText(
                    String.format(Locale.getDefault(), "$%.2f", monthlyTotal)
            );
            adapter.setMonthlyTotal(monthlyTotal);
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
        tvSummaryMonthLabel.setText(currentMonth.format(
                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        ));

        String pattern = currentMonth.format(
                DateTimeFormatter.ofPattern("yyyy-MM")
        ) + "-%";

        // Triggers switchMap in ViewModel → monthTotal observer above fires automatically
        mViewModel.setMonthFilter(pattern);

        // getTotalsByCategory returns a new LiveData object each call,
        // so each observe() is on a different object — not the same stacking problem
        mViewModel.getTotalsByCategory(pattern).observe(this, categoryTotals -> {
            adapter.setCategoryTotals(categoryTotals);
        });
    }
}