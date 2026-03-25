package com.group22.budgetmaxxer.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group22.budgetmaxxer.R;
import com.group22.budgetmaxxer.database.Category;
import com.group22.budgetmaxxer.database.CategoryTotal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder> {

    private List<CategoryTotal> mCategoryTotals = new ArrayList<>();
    private List<Category> mCategories = new ArrayList<>();
    private double mMonthlyTotal = 0.0;

    // --- Three setters — each triggers a redraw ---

    public void setCategoryTotals(List<CategoryTotal> totals) {
        mCategoryTotals = totals != null ? totals : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setCategories(List<Category> categories) {
        mCategories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setMonthlyTotal(double total) {
        mMonthlyTotal = total;
        notifyDataSetChanged();
    }

    // --- RecyclerView boilerplate ---

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_summary, parent, false);
        return new SummaryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        CategoryTotal ct = mCategoryTotals.get(position);

        // Match category_id → Category object
        Category cat = null;
        for (Category c : mCategories) {
            if (c.getId() == ct.category_id) {
                cat = c;
                break;
            }
        }

        // Amount
        holder.tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", ct.total));

        // Percentage — guard against divide-by-zero
        int percent = 0;
        if (mMonthlyTotal > 0) {
            percent = (int) Math.round((ct.total / mMonthlyTotal) * 100);
        }
        holder.tvPercent.setText(String.format(Locale.getDefault(), "%d%%", percent));
        holder.progressBar.setProgress(percent);

        // Category details — fallback gracefully if not found
        if (cat != null) {
            holder.tvIcon.setText(cat.getIcon());
            holder.tvCategory.setText(cat.getName());

            // Use the category color on the progress bar
            try {
                holder.progressBar.getProgressDrawable()
                        .setColorFilter(
                                Color.parseColor(cat.getColor()),
                                android.graphics.PorterDuff.Mode.SRC_IN
                        );
            } catch (IllegalArgumentException e) {
                // Malformed color string — leave default color
            }
        } else {
            holder.tvIcon.setText("📦");
            holder.tvCategory.setText("Unknown");
        }
    }

    @Override
    public int getItemCount() {
        return mCategoryTotals.size();
    }

    // --- ViewHolder ---

    static class SummaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvCategory, tvAmount, tvPercent;
        ProgressBar progressBar;

        SummaryViewHolder(View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvSummaryIcon);
            tvCategory = itemView.findViewById(R.id.tvSummaryCategory);
            tvAmount = itemView.findViewById(R.id.tvSummaryAmount);
            tvPercent = itemView.findViewById(R.id.tvSummaryPercent);
            progressBar = itemView.findViewById(R.id.progressCategory);
        }
    }
}
