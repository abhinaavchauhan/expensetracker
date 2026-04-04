package com.expensetracker.presentation.addexpense;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.R;
import com.expensetracker.domain.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private int selectedPosition = -1;
    private OnCategorySelectedListener listener;

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.listener = listener;
    }

    public Category getSelectedCategory() {
        if (selectedPosition >= 0 && selectedPosition < categories.size()) {
            return categories.get(selectedPosition);
        }
        return null;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout container;
        private final View iconBg;
        private final ImageView ivIcon;
        private final TextView tvName;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.category_container);
            iconBg = itemView.findViewById(R.id.icon_bg);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvName = itemView.findViewById(R.id.tv_name);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    int previousSelected = selectedPosition;
                    selectedPosition = pos;

                    if (previousSelected >= 0) {
                        notifyItemChanged(previousSelected);
                    }
                    notifyItemChanged(selectedPosition);

                    if (listener != null) {
                        listener.onCategorySelected(categories.get(pos));
                    }

                    // Press animation
                    com.expensetracker.core.animations.AnimationUtils.scalePress(v);
                }
            });
        }

        void bind(Category category, boolean isSelected) {
            tvName.setText(category.getName());
            ivIcon.setImageResource(category.getIconResId());

            // Set icon background color
            try {
                int color = Color.parseColor(category.getColor());
                GradientDrawable bgShape = new GradientDrawable();
                bgShape.setShape(GradientDrawable.OVAL);
                bgShape.setColor(color);
                iconBg.setBackground(bgShape);
            } catch (Exception e) {
                // Fallback color
                iconBg.setBackgroundResource(R.drawable.bg_category_icon);
            }

            container.setSelected(isSelected);
            tvName.setTextColor(isSelected ?
                    itemView.getContext().getResources().getColor(R.color.primary) :
                    itemView.getContext().getResources().getColor(R.color.text_secondary_dark));
        }
    }

    public interface OnCategorySelectedListener {
        void onCategorySelected(Category category);
    }
}
