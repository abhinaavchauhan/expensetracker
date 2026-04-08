package com.expensetracker.presentation.transactions;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.expensetracker.presentation.addexpense.CategoryAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.expensetracker.domain.model.Category;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.Navigation;

import com.expensetracker.R;
import com.expensetracker.core.constants.CategoryConstants;
import com.expensetracker.core.utils.CurrencyUtils;
import com.expensetracker.core.utils.DateUtils;
import com.expensetracker.data.local.entity.ExpenseEntity;
import com.expensetracker.databinding.FragmentTransactionsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.List;

public class TransactionsFragment extends Fragment {

    private FragmentTransactionsBinding binding;
    private TransactionsViewModel viewModel;
    private TransactionAdapter adapter;
    private LiveData<List<ExpenseEntity>> currentData;

    // Filter state
    private String selectedType = "all";
    private String selectedCategory = null;
    private Integer selectedMonth = null;
    private Long selectedExactDate = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TransactionsViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupFilters();
        applyAllFilters(); // Initial load
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter();
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(adapter);

        // Tap → open detail bottom sheet with delete option
        adapter.setOnTransactionClickListener(this::showTransactionDetailSheet);

        // Long-press → quick delete confirmation dialog
        adapter.setOnTransactionLongClickListener(this::showDeleteConfirmation);

        // Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final ColorDrawable background = new ColorDrawable(getResources().getColor(R.color.expense_red));
            private final Drawable deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                TransactionListItem item = adapter.getCurrentList().get(position);
                if (item.getType() == TransactionListItem.TYPE_TRANSACTION) {
                    showDeleteConfirmationWithReset(item.getExpense(), position);
                } else {
                    adapter.notifyItemChanged(position); // Prevent header swipe
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                if (dX < 0) {
                    background.setBounds(
                            itemView.getRight() + (int) dX,
                            itemView.getTop(),
                            itemView.getRight(),
                            itemView.getBottom()
                    );
                    background.draw(c);

                    if (deleteIcon != null) {
                        int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                        int iconRight = itemView.getRight() - iconMargin;
                        int iconLeft = iconRight - deleteIcon.getIntrinsicWidth();
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        deleteIcon.draw(c);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(binding.rvTransactions);
    }

    /**
     * Shows a premium bottom sheet with full transaction details and a Delete button.
     */
    private void showTransactionDetailSheet(ExpenseEntity expense) {
        if (expense == null || getContext() == null) return;

        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialog);
        View sheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_transaction_detail, null);
        bottomSheet.setContentView(sheetView);

        // Populate data
        View iconBg = sheetView.findViewById(R.id.detail_icon_bg);
        ImageView ivCategory = sheetView.findViewById(R.id.detail_iv_category);
        TextView tvCategory = sheetView.findViewById(R.id.detail_tv_category);
        TextView tvType = sheetView.findViewById(R.id.detail_tv_type);
        TextView tvAmount = sheetView.findViewById(R.id.detail_tv_amount);
        TextView tvDate = sheetView.findViewById(R.id.detail_tv_date);
        LinearLayout noteContainer = sheetView.findViewById(R.id.detail_note_container);
        TextView tvNote = sheetView.findViewById(R.id.detail_tv_note);
        MaterialButton btnEdit = sheetView.findViewById(R.id.detail_btn_edit);
        MaterialButton btnDelete = sheetView.findViewById(R.id.detail_btn_delete);

        // Category icon + color
        int iconRes = CategoryConstants.getCategoryIcon(expense.getCategory());
        int color = CategoryConstants.getCategoryColor(expense.getCategory());
        ivCategory.setImageResource(iconRes);

        GradientDrawable bgShape = new GradientDrawable();
        bgShape.setShape(GradientDrawable.OVAL);
        bgShape.setColor(color);
        iconBg.setBackground(bgShape);

        // Text fields
        tvCategory.setText(expense.getCategory() != null ? expense.getCategory() : "Other");

        boolean isExpense = "expense".equalsIgnoreCase(expense.getType());
        tvType.setText(isExpense ? getString(R.string.expense) : getString(R.string.income));
        tvType.setTextColor(isExpense ?
                getResources().getColor(R.color.expense_red) :
                getResources().getColor(R.color.income_green));

        String formattedAmount = CurrencyUtils.formatAmountWithSign(expense.getAmount(), isExpense);
        tvAmount.setText(formattedAmount);
        tvAmount.setTextColor(isExpense ?
                getResources().getColor(R.color.expense_red) :
                getResources().getColor(R.color.income_green));

        tvDate.setText(DateUtils.formatDate(expense.getDate()));

        // Note
        String note = expense.getNote();
        if (note != null && !note.isEmpty()) {
            noteContainer.setVisibility(View.VISIBLE);
            tvNote.setText(note);
        } else {
            noteContainer.setVisibility(View.GONE);
        }

        // Edit button
        btnEdit.setOnClickListener(v -> {
            com.expensetracker.core.animations.AnimationUtils.scalePress(v);
            bottomSheet.dismiss();
            Bundle args = new Bundle();
            args.putSerializable("transaction", expense);
            Navigation.findNavController(requireView()).navigate(R.id.action_transactions_to_addExpense, args);
        });

        // Delete button
        btnDelete.setOnClickListener(v -> {
            com.expensetracker.core.animations.AnimationUtils.scalePress(v);
            bottomSheet.dismiss();
            showDeleteConfirmation(expense);
        });

        bottomSheet.show();
    }

    /**
     * Shows a confirmation dialog before deleting a transaction.
     */
    private void showDeleteConfirmation(ExpenseEntity expense) {
        if (expense == null || getContext() == null) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    viewModel.deleteExpense(expense);
                    Toast.makeText(requireContext(),
                            expense.getCategory() + " transaction deleted",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    /**
     * Delete confirmation that resets the swiped item if user cancels.
     */
    private void showDeleteConfirmationWithReset(ExpenseEntity expense, int position) {
        if (expense == null || getContext() == null) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    viewModel.deleteExpense(expense);
                    Toast.makeText(requireContext(),
                            expense.getCategory() + " transaction deleted",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    adapter.notifyItemChanged(position);
                })
                .setOnCancelListener(dialog -> {
                    adapter.notifyItemChanged(position);
                })
                .show();
    }

    private void setupSearch() {
        binding.ivFilter.setOnClickListener(v -> {
            com.expensetracker.core.animations.AnimationUtils.scalePress(v);
            showFilterDialog();
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    applyAllFilters();
                } else {
                    observeSearchResults(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_all)) {
                selectedType = "all";
            } else if (checkedIds.contains(R.id.chip_income)) {
                selectedType = "income";
            } else if (checkedIds.contains(R.id.chip_expense)) {
                selectedType = "expense";
            }
            applyAllFilters();
        });
    }

    private void observeSearchResults(String query) {
        if (currentData != null) {
            currentData.removeObservers(getViewLifecycleOwner());
        }

        currentData = viewModel.searchTransactions(query);
        currentData.observe(getViewLifecycleOwner(), this::updateAdapterUI);
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialog);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);
        dialog.setContentView(view);

        ChipGroup cgMonths = view.findViewById(R.id.cg_months);
        RecyclerView rvCategories = view.findViewById(R.id.rv_categories);
        MaterialButton btnReset = view.findViewById(R.id.btn_reset);
        MaterialButton btnApply = view.findViewById(R.id.btn_apply);

        // Populate and Pre-select Months
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (int i = 0; i < months.length; i++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, cgMonths, false);
            chip.setText(months[i]);
            chip.setTag(i); // Store index
            cgMonths.addView(chip);

            if (selectedMonth != null && selectedMonth == i) {
                chip.setChecked(true);
            }
        }

        MaterialButton btnPickDate = view.findViewById(R.id.btn_pick_date);
        btnPickDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setSelection(selectedExactDate != null ? selectedExactDate : MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedExactDate = selection;
                cgMonths.clearCheck(); // Clear month selection if an exact date is chosen
                btnPickDate.setText(DateUtils.formatDateShort(selection));
            });
            datePicker.show(getParentFragmentManager(), "date_picker");
        });

        if (selectedExactDate != null) {
            btnPickDate.setText(DateUtils.formatDateShort(selectedExactDate));
        }

        // Setup and Pre-select Categories
        CategoryAdapter categoryAdapter = new CategoryAdapter();
        rvCategories.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(requireContext(), 4));
        rvCategories.setAdapter(categoryAdapter);
        List<Category> allCategories = CategoryConstants.getAllCategories();
        categoryAdapter.setCategories(allCategories);

        // Pre-select current category
        if (selectedCategory != null) {
            categoryAdapter.setSelectedCategory(selectedCategory);
        }

        categoryAdapter.setOnCategorySelectedListener(category -> selectedCategory = category.getName());

        btnApply.setOnClickListener(v -> {
            int checkedChipId = cgMonths.getCheckedChipId();
            if (checkedChipId != -1) {
                selectedMonth = (Integer) cgMonths.findViewById(checkedChipId).getTag();
                selectedExactDate = null;
            } else {
                selectedMonth = null;
            }

            applyAllFilters();
            dialog.dismiss();
        });

        btnReset.setOnClickListener(v -> {
            selectedMonth = null;
            selectedExactDate = null;
            selectedCategory = null;
            selectedType = "all";
            binding.chipGroupFilter.check(R.id.chip_all);
            applyAllFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void applyAllFilters() {
        if (currentData != null) {
            currentData.removeObservers(getViewLifecycleOwner());
        }

        Long start = null;
        Long end = null;

        if (selectedExactDate != null) {
            Calendar utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
            utcCal.setTimeInMillis(selectedExactDate);
            
            Calendar localCal = Calendar.getInstance();
            localCal.set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH), utcCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            localCal.set(Calendar.MILLISECOND, 0);
            start = localCal.getTimeInMillis();

            localCal.set(Calendar.HOUR_OF_DAY, 23);
            localCal.set(Calendar.MINUTE, 59);
            localCal.set(Calendar.SECOND, 59);
            localCal.set(Calendar.MILLISECOND, 999);
            end = localCal.getTimeInMillis();
        } else if (selectedMonth != null) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, selectedMonth);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            start = cal.getTimeInMillis();

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            end = cal.getTimeInMillis();
        }

        String typeToQuery = "all".equals(selectedType) ? null : selectedType;

        currentData = viewModel.getFilteredExpenses(selectedCategory, typeToQuery, start, end);
        currentData.observe(getViewLifecycleOwner(), this::updateAdapterUI);
    }

    private void updateAdapterUI(List<ExpenseEntity> expenses) {
        if (expenses != null && !expenses.isEmpty()) {
            List<TransactionListItem> groupedList = groupTransactionsByMonth(expenses);
            adapter.submitList(groupedList);
            binding.rvTransactions.setVisibility(View.VISIBLE);
            binding.emptyState.setVisibility(View.GONE);
        } else {
            adapter.submitList(null);
            binding.rvTransactions.setVisibility(View.GONE);
            binding.emptyState.setVisibility(View.VISIBLE);
        }
    }

    private List<TransactionListItem> groupTransactionsByMonth(List<ExpenseEntity> expenses) {
        List<TransactionListItem> groupedList = new ArrayList<>();
        String currentMonthYear = "";
        java.text.SimpleDateFormat monthYearFormat = new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault());

        for (ExpenseEntity expense : expenses) {
            String itemMonthYear = monthYearFormat.format(new java.util.Date(expense.getDate()));
            if (!itemMonthYear.equals(currentMonthYear)) {
                currentMonthYear = itemMonthYear;
                groupedList.add(new TransactionListItem(currentMonthYear));
            }
            groupedList.add(new TransactionListItem(expense));
        }
        return groupedList;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
