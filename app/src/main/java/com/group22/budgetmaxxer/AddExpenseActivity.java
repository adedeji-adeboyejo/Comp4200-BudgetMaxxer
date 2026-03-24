package com.group22.budgetmaxxer;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class AddExpenseActivity extends AppCompatActivity {

    private static final String EXTRA_EXPENSE_ID = "EXPENSE_ID";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault());

    private ExpenseViewModel expenseViewModel;

    private MaterialToolbar toolbar;
    private TextInputLayout tilAmount;
    private TextInputEditText etAmount;
    private TextInputEditText etDescription;
    private MaterialButton btnDatePicker;
    private MaterialButton btnSaveExpense;
    private MaterialButton btnDeleteExpense;

    private final Map<MaterialCardView, String> categoryCards = new LinkedHashMap<>();
    private MaterialCardView selectedCard;
    private String selectedCategory;
    private LocalDate selectedDate = LocalDate.now();

    private int expenseId = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        bindViews();
        setupToolbar();
        setupCategorySelector();
        setupDatePickerButton();
        configureModeFromIntent();
        setupActionButtons();
        updateDateButtonText();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbarAddExpense);
        tilAmount = findViewById(R.id.tilAmount);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);
        btnDeleteExpense = findViewById(R.id.btnDeleteExpense);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupCategorySelector() {
        categoryCards.put(findViewById(R.id.cardCategoryFood), "Food");
        categoryCards.put(findViewById(R.id.cardCategoryTransport), "Transport");
        categoryCards.put(findViewById(R.id.cardCategoryBills), "Bills");
        categoryCards.put(findViewById(R.id.cardCategoryShopping), "Shopping");
        categoryCards.put(findViewById(R.id.cardCategoryHealth), "Health");
        categoryCards.put(findViewById(R.id.cardCategoryOther), "Other");

        for (Map.Entry<MaterialCardView, String> entry : categoryCards.entrySet()) {
            MaterialCardView card = entry.getKey();
            String category = entry.getValue();
            card.setClickable(true);
            card.setOnClickListener(v -> selectCategory(card, category));
        }
    }

    private void setupDatePickerButton() {
        btnDatePicker.setOnClickListener(v -> openDatePicker());
    }

    private void configureModeFromIntent() {
        if (getIntent() != null && getIntent().hasExtra(EXTRA_EXPENSE_ID)) {
            int idFromIntent = getIntent().getIntExtra(EXTRA_EXPENSE_ID, -1);
            if (idFromIntent > 0) {
                isEditMode = true;
                expenseId = idFromIntent;
                toolbar.setTitle("Edit Expense");
                btnDeleteExpense.setVisibility(android.view.View.VISIBLE);
                loadExpense(expenseId);
                return;
            }
        }

        isEditMode = false;
        toolbar.setTitle("Add Expense");
        btnDeleteExpense.setVisibility(android.view.View.GONE);
    }

    private void setupActionButtons() {
        btnSaveExpense.setOnClickListener(v -> saveExpense());
        btnDeleteExpense.setOnClickListener(v -> confirmDeleteExpense());
    }

    private void loadExpense(int id) {
        Expense expense = expenseViewModel.getExpenseById(id);
        if (expense == null) {
            Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etAmount.setText(String.valueOf(expense.getAmount()));
        etDescription.setText(expense.getDescription());
        selectedDate = expense.getDate();
        updateDateButtonText();
        selectCategoryByName(expense.getCategory());
    }

    private void openDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(toUtcMillis(selectedDate))
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = fromUtcMillis(selection);
            updateDateButtonText();
        });
        picker.show(getSupportFragmentManager(), "expense_date_picker");
    }

    private void saveExpense() {
        tilAmount.setError(null);

        String amountText = toSafeString(etAmount.getText()).trim();
        String descriptionText = toSafeString(etDescription.getText()).trim();

        if (amountText.isEmpty()) {
            tilAmount.setError("Please enter an amount");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            tilAmount.setError("Please enter an amount");
            return;
        }

        if (amount <= 0) {
            tilAmount.setError("Amount must be greater than 0");
            return;
        }

        if (selectedCategory == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isAfter(LocalDate.now())) {
            Toast.makeText(this, "Warning: selected date is in the future", Toast.LENGTH_LONG).show();
        }

        if (isEditMode) {
            expenseViewModel.updateExpense(
                    expenseId,
                    amount,
                    selectedCategory,
                    descriptionText,
                    selectedDate
            );
        } else {
            expenseViewModel.insertExpense(
                    amount,
                    selectedCategory,
                    descriptionText,
                    selectedDate
            );
        }

        finish();
    }

    private void confirmDeleteExpense() {
        if (!isEditMode || expenseId <= 0) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    expenseViewModel.deleteExpense(expenseId);
                    finish();
                })
                .show();
    }

    private void selectCategoryByName(@NonNull String categoryName) {
        for (Map.Entry<MaterialCardView, String> entry : categoryCards.entrySet()) {
            if (categoryName.equals(entry.getValue())) {
                selectCategory(entry.getKey(), categoryName);
                return;
            }
        }
    }

    private void selectCategory(@NonNull MaterialCardView card, @NonNull String category) {
        if (selectedCard != null) {
            selectedCard.setStrokeWidth(0);
            selectedCard.setChecked(false);
        }

        selectedCard = card;
        selectedCategory = category;
        selectedCard.setStrokeColor(getColor(R.color.primary));
        selectedCard.setStrokeWidth(dpToPx(2));
        selectedCard.setChecked(true);
    }

    private void updateDateButtonText() {
        btnDatePicker.setText(selectedDate.format(DATE_FORMATTER));
    }

    private long toUtcMillis(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private LocalDate fromUtcMillis(long utcMillis) {
        return Instant.ofEpochMilli(utcMillis).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private String toSafeString(CharSequence value) {
        return value == null ? "" : value.toString();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
