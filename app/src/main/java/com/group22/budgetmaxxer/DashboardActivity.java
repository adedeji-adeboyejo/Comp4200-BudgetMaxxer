package com.group22.budgetmaxxer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.group22.budgetmaxxer.ui.ExpenseAdapter;
import com.group22.budgetmaxxer.viewmodel.ExpenseViewModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private ExpenseViewModel mViewModel;
    private TextView tvMonthTotal;
    private TextView tvMonthLabel;
    private LocalDate currentMonth = LocalDate.now();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbarDashboard);
        setSupportActionBar(toolbar); // ← add this

        tvMonthTotal = findViewById(R.id.tvMonthTotal);
        tvMonthLabel = findViewById(R.id.tvMonthLabel);

        RecyclerView recyclerView = findViewById(R.id.recyclerRecentExpenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ExpenseAdapter adapter = new ExpenseAdapter(expense -> {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            intent.putExtra(AddExpenseActivity.EXTRA_EXPENSE_ID, expense.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        mViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        // 1. Observe lists for the RecyclerView
        mViewModel.mRecentExpenses.observe(this, adapter::setExpenses);
        mViewModel.mAllCategories.observe(this, adapter::setCategories);

        // 2. Observe the month total EXACTLY ONCE here to prevent memory leaks/glitches
        mViewModel.monthTotal.observe(this, total -> {
            double amount = total == null ? 0.0 : total;
            tvMonthTotal.setText(String.format(Locale.getDefault(), "$%.2f", amount));
        });

        // 3. Initialize the UI and trigger the first database fetch
        updateMonthData();

        FloatingActionButton fab = findViewById(R.id.fabAddExpense);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddExpenseActivity.class))
        );

        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            updateMonthData();
        });

        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            updateMonthData();
        });
    }

    private void updateMonthData() {
        // Update the text label visually
        tvMonthLabel.setText(currentMonth.format(
                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        ));

        // Tell the ViewModel to swap the database query to the new month
        String pattern = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-%";
        mViewModel.setMonthFilter(pattern);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.menu_summary) {
            startActivity(new Intent(this, SummaryActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}