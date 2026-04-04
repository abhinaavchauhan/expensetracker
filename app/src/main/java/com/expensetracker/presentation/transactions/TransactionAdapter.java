package com.expensetracker.presentation.transactions;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.R;
import com.expensetracker.core.constants.CategoryConstants;
import com.expensetracker.core.utils.CurrencyUtils;
import com.expensetracker.core.utils.DateUtils;
import com.expensetracker.data.local.entity.ExpenseEntity;

public class TransactionAdapter extends ListAdapter<ExpenseEntity, TransactionAdapter.TransactionViewHolder> {

    private OnTransactionClickListener listener;
    private OnTransactionLongClickListener longClickListener;

    public TransactionAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ExpenseEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ExpenseEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull ExpenseEntity oldItem, @NonNull ExpenseEntity newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull ExpenseEntity oldItem, @NonNull ExpenseEntity newItem) {
                    return oldItem.getAmount() == newItem.getAmount()
                            && oldItem.getDate() == newItem.getDate()
                            && (oldItem.getCategory() != null && oldItem.getCategory().equals(newItem.getCategory()))
                            && (oldItem.getNote() != null && oldItem.getNote().equals(newItem.getNote()));
                }
            };

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        ExpenseEntity expense = getItem(position);
        if (expense == null) return;

        holder.bind(expense);
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public void setOnTransactionLongClickListener(OnTransactionLongClickListener listener) {
        this.longClickListener = listener;
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final View iconBg;
        private final ImageView ivCategory;
        private final TextView tvCategory;
        private final TextView tvNote;
        private final TextView tvAmount;
        private final TextView tvDate;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            iconBg = itemView.findViewById(R.id.icon_bg);
            ivCategory = itemView.findViewById(R.id.iv_category);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvNote = itemView.findViewById(R.id.tv_note);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date);

            // Tap → open detail bottom sheet
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTransactionClick(getItem(pos));
                }
            });

            // Long-press → quick delete confirmation
            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onTransactionLongClick(getItem(pos));
                    return true;
                }
                return false;
            });
        }

        void bind(ExpenseEntity expense) {
            tvCategory.setText(expense.getCategory() != null ? expense.getCategory() : "Other");

            String note = expense.getNote();
            if (note != null && !note.isEmpty()) {
                tvNote.setText(note);
                tvNote.setVisibility(View.VISIBLE);
            } else {
                tvNote.setVisibility(View.GONE);
            }

            boolean isExpense = "expense".equalsIgnoreCase(expense.getType());
            String formattedAmount = CurrencyUtils.formatAmountWithSign(expense.getAmount(), isExpense);
            tvAmount.setText(formattedAmount);
            tvAmount.setTextColor(isExpense ?
                    itemView.getContext().getResources().getColor(R.color.expense_red) :
                    itemView.getContext().getResources().getColor(R.color.income_green));

            tvDate.setText(DateUtils.formatDateShort(expense.getDate()));

            // Set category icon and color
            int iconRes = CategoryConstants.getCategoryIcon(expense.getCategory());
            int color = CategoryConstants.getCategoryColor(expense.getCategory());
            ivCategory.setImageResource(iconRes);

            GradientDrawable bgShape = new GradientDrawable();
            bgShape.setShape(GradientDrawable.OVAL);
            bgShape.setColor(color);
            iconBg.setBackground(bgShape);
        }
    }

    public interface OnTransactionClickListener {
        void onTransactionClick(ExpenseEntity expense);
    }

    public interface OnTransactionLongClickListener {
        void onTransactionLongClick(ExpenseEntity expense);
    }
}
