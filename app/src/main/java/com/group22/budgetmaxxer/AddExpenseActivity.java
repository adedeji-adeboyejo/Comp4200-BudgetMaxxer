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

import com.group22.budgetmaxxer.database.Category;
import com.group22.budgetmaxxer.database.Expense;
import com.group22.budgetmaxxer.viewmodel.ExpenseViewModel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddExpenseActivity extends AppCompatActivity {

    public static final String EXTRA_EXPENSE_ID = "EXPENSE_ID";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault());
    private static final DateTimeFormatter DB_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ExpenseViewModel expenseViewModel;
    private List<Category> allCategories;

    private MaterialToolbar toolbar;
    private TextInputLayout tilAmount;
    private TextInputEditText etAmount;
    private TextInputEditText etDescription;
    private MaterialButton btnDatePicker;
    private MaterialButton btnSaveExpense;
    private MaterialButton btnDeleteExpense;

    private final Map<MaterialCardView, String> categoryCards = new LinkedHashMap<>();
    private MaterialCardView selectedCard;
    private String selectedCategoryName;
    private LocalDate selectedDate = LocalDate.now();
    private int expenseId = -1;
    private boolean isEditMode = false;
    private long originalCreatedAt = 0; // Added to preserve original creation time when editing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        bindViews();
        setupToolbar();
        setupCategorySelector();
        setupDatePickerButton();
        setupActionButtons();
        updateDateButtonText();

        expenseViewModel.mAllCategories.observe(this, categories -> {
            allCategories = categories;
            configureModeFromIntent();
        });
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
        com.group22.budgetmaxxer.database.AppDatabase.databaseWriteExecutor.execute(() -> {
            Expense expense = com.group22.budgetmaxxer.database.AppDatabase
                    .getDatabase(getApplication())
                    .expenseDao()
                    .getExpenseById(id);

            runOnUiThread(() -> {
                if (expense == null) {
                    Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                originalCreatedAt = expense.getCreatedAt(); // Save this so we don't overwrite it!

                etAmount.setText(String.valueOf(expense.getAmount()));
                etDescription.setText(expense.getDescription());
                selectedDate = LocalDate.parse(expense.getDate(), DB_FORMATTER);
                updateDateButtonText();

                if (allCategories != null) {
                    for (Category c : allCategories) {
                        if (c.getId() == expense.getCategoryId()) {
                            selectCategoryByName(c.getName());
                            break;
                        }
                    }
                }
            });
        });
    }

    private void saveExpense() {
        // Prevent saving if categories haven't loaded yet
        if (allCategories == null) return;

        tilAmount.setError(null);
        String amountText = etAmount.getText() == null ? "" : etAmount.getText().toString().trim();
        String descriptionText = etDescription.getText() == null ? "" : etDescription.getText().toString().trim();

        if (amountText.isEmpty()) {
            tilAmount.setError("Please enter an amount");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            tilAmount.setError("Invalid amount");
            return;
        }
        if (amount <= 0) {
            tilAmount.setError("Amount must be greater than 0");
            return;
        }
        if (selectedCategoryName == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDate.isAfter(LocalDate.now())) {
            Toast.makeText(this, "Warning: date is in the future", Toast.LENGTH_LONG).show();
        }

        int categoryId = 1;
        for (Category c : allCategories) {
            if (c.getName().equals(selectedCategoryName)) {
                categoryId = c.getId();
                break;
            }
        }

        String dateString = selectedDate.format(DB_FORMATTER);

        if (isEditMode) {
            // Reconstruct the expense and give it the existing ID to overwrite it in Room
            Expense updatedExpense = new Expense(
                    amount, categoryId, descriptionText, dateString, originalCreatedAt
            );
            updatedExpense.setId(expenseId);
            expenseViewModel.update(updatedExpense);
        } else {
            Expense newExpense = new Expense(
                    amount, categoryId, descriptionText, dateString, System.currentTimeMillis()
            );
            expenseViewModel.insert(newExpense);
        }

        Toast.makeText(this, "Expense saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDeleteExpense() {
        if (!isEditMode || expenseId <= 0) return;
        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Room only needs the ID to delete a row
                    Expense dummyExpense = new Expense(0, 0, "", "", 0);
                    dummyExpense.setId(expenseId);
                    expenseViewModel.delete(dummyExpense);
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
        selectedCategoryName = category;
        selectedCard.setStrokeColor(getColor(R.color.primary));
        selectedCard.setStrokeWidth(dpToPx(2));
        selectedCard.setChecked(true);
    }

    private void updateDateButtonText() {
        btnDatePicker.setText(selectedDate.format(DATE_FORMATTER));
    }

    private void openDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(selectedDate.atStartOfDay(ZoneId.systemDefault())
                        .toInstant().toEpochMilli())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = Instant.ofEpochMilli(selection)
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            updateDateButtonText();
        });
        picker.show(getSupportFragmentManager(), "expense_date_picker");
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}