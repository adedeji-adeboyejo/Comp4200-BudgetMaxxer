package com.group22.budgetmaxxer.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group22.budgetmaxxer.R;
import com.group22.budgetmaxxer.database.Category;
import com.group22.budgetmaxxer.database.Expense;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> mExpenses = new ArrayList<>();
    private List<Category> mCategories = new ArrayList<>();
    private OnExpenseClickListener mListener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
    }

    public ExpenseAdapter(OnExpenseClickListener listener) {
        mListener = listener;
    }

    public void setExpenses(List<Expense> expenses) {
        mExpenses = expenses;
        notifyDataSetChanged();
    }

    public void setCategories(List<Category> categories) {
        mCategories = categories;
        notifyDataSetChanged();
    }

    public Expense getExpenseAt(int position) {
        return mExpenses.get(position);
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = mExpenses.get(position);

        // Match category
        Category cat = null;
        for (Category c : mCategories) {
            if (c.getId() == expense.getCategoryId()) {
                cat = c;
                break;
            }
        }

        holder.tvAmount.setText(String.format("$%.2f", expense.getAmount()));
        holder.tvDate.setText(expense.getDate());
        holder.tvDescription.setText(
                expense.getDescription() != null ? expense.getDescription() : ""
        );

        if (cat != null) {
            holder.tvIcon.setText(cat.getIcon());
            holder.tvCategory.setText(cat.getName());

            // Tint the circular emoji background with a transparent version of category color
            try {
                int baseColor = Color.parseColor(cat.getColor());
                int transparentColor = Color.argb(40,
                        Color.red(baseColor),
                        Color.green(baseColor),
                        Color.blue(baseColor));
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                circle.setColor(transparentColor);
                holder.tvIcon.setBackground(circle);
            } catch (IllegalArgumentException e) {
                holder.tvIcon.setBackgroundResource(R.drawable.bg_category_icon);
            }
        } else {
            holder.tvIcon.setText("📦");
            holder.tvCategory.setText("Unknown");
            holder.tvIcon.setBackgroundResource(R.drawable.bg_category_icon);
        }

        holder.itemView.setOnClickListener(v -> mListener.onExpenseClick(expense));
    }

    @Override
    public int getItemCount() { return mExpenses.size(); }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvCategory, tvDescription, tvDate, tvAmount;

        ExpenseViewHolder(View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}