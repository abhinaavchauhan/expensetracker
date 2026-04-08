package com.expensetracker.presentation.transactions;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class TransactionAdapter extends ListAdapter<TransactionListItem, RecyclerView.ViewHolder> {

    private OnTransactionClickListener listener;
    private OnTransactionLongClickListener longClickListener;

    public TransactionAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<TransactionListItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TransactionListItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull TransactionListItem oldItem, @NonNull TransactionListItem newItem) {
                    if (oldItem.getType() != newItem.getType()) return false;
                    if (oldItem.getType() == TransactionListItem.TYPE_HEADER) {
                        return oldItem.getMonthYear().equals(newItem.getMonthYear());
                    } else {
                        return oldItem.getExpense().getId() == newItem.getExpense().getId();
                    }
                }

                @Override
                public boolean areContentsTheSame(@NonNull TransactionListItem oldItem, @NonNull TransactionListItem newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TransactionListItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TransactionListItem item = getItem(position);
        if (item.getType() == TransactionListItem.TYPE_HEADER) {
            ((HeaderViewHolder) holder).bind(item.getMonthYear());
        } else {
            ((TransactionViewHolder) holder).bind(item.getExpense());
        }
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public void setOnTransactionLongClickListener(OnTransactionLongClickListener listener) {
        this.longClickListener = listener;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_header_title);
        }

        void bind(String title) {
            tvTitle.setText(title);
        }
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

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    TransactionListItem item = getItem(pos);
                    if (item.getType() == TransactionListItem.TYPE_TRANSACTION) {
                        listener.onTransactionClick(item.getExpense());
                    }
                }
            });

            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && longClickListener != null) {
                    TransactionListItem item = getItem(pos);
                    if (item.getType() == TransactionListItem.TYPE_TRANSACTION) {
                        longClickListener.onTransactionLongClick(item.getExpense());
                        return true;
                    }
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
