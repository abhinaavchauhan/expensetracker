package com.expensetracker.presentation.dashboard;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.expensetracker.R;
import com.expensetracker.core.animations.AnimationUtils;
import com.expensetracker.core.constants.CategoryConstants;
import com.expensetracker.core.utils.CurrencyUtils;
import com.expensetracker.core.utils.DateUtils;
import com.expensetracker.data.local.entity.ExpenseEntity;
import com.expensetracker.databinding.FragmentDashboardBinding;
import com.expensetracker.presentation.transactions.TransactionAdapter;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private TransactionAdapter transactionAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupUI();
        setupRecyclerView();
        observeData();
    }

    private void setupUI() {
        binding.tvGreeting.setText(getGreeting() + ", Abhinav🙏");
        binding.tvDate.setText(DateUtils.getCurrentDateFormatted());

        binding.ivProfile.setOnClickListener(v -> {
            AnimationUtils.scalePress(v);
            showProfilePopup(v);
        });

        binding.tvSeeAll.setOnClickListener(v -> {
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView bnv = getActivity().findViewById(R.id.bottom_navigation);
                if (bnv != null) {
                    bnv.setSelectedItemId(R.id.nav_transactions);
                } else {
                    Navigation.findNavController(v).navigate(R.id.nav_transactions);
                }
            }
        });

        // Animate cards
        AnimationUtils.slideUpFadeIn(binding.cardBalance, 100);
    }

    private String getGreeting() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return getString(R.string.greeting_morning);
        } else if (hour >= 12 && hour < 17) {
            return getString(R.string.greeting_afternoon);
        } else if (hour >= 17 && hour < 21) {
            return getString(R.string.greeting_evening);
        } else {
            return getString(R.string.greeting_night);
        }
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter();
        binding.rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentTransactions.setAdapter(transactionAdapter);
        binding.rvRecentTransactions.setNestedScrollingEnabled(false);

        // Tap → open detail bottom sheet with delete option
        transactionAdapter.setOnTransactionClickListener(this::showTransactionDetailSheet);

        // Long-press → quick delete confirmation
        transactionAdapter.setOnTransactionLongClickListener(this::showDeleteConfirmation);
    }

    private void showProfilePopup(View anchor) {
        if (getContext() == null) return;

        View popupView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_profile_name, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setElevation(10f);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        // Show below the horizontal center of the profile icon
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int xOffset = -(popupView.getMeasuredWidth() - (anchor.getWidth() / 2));
        popupWindow.showAsDropDown(anchor, xOffset, 10);
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
            AnimationUtils.scalePress(v);
            bottomSheet.dismiss();
            Bundle args = new Bundle();
            args.putSerializable("transaction", expense);
            Navigation.findNavController(requireView()).navigate(R.id.action_dashboard_to_addExpense, args);
        });

        // Delete button
        btnDelete.setOnClickListener(v -> {
            AnimationUtils.scalePress(v);
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

    private void observeData() {
        // Observe total income & expense for balance
        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> {
            double incomeVal = income != null ? income : 0;
            viewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> {
                double expenseVal = expense != null ? expense : 0;
                double balance = incomeVal - expenseVal;
                AnimationUtils.animateCounter(binding.tvBalance, 0, balance, 1000);
            });
        });

        // Monthly income
        viewModel.getMonthlyIncome().observe(getViewLifecycleOwner(), income -> {
            double value = income != null ? income : 0;
            binding.tvIncome.setText(CurrencyUtils.formatAmount(value));
        });

        // Monthly expense
        viewModel.getMonthlyExpense().observe(getViewLifecycleOwner(), expense -> {
            double value = expense != null ? expense : 0;
            binding.tvExpenses.setText(CurrencyUtils.formatAmount(value));
        });

        // Recent transactions
        viewModel.getRecentTransactions().observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null && !expenses.isEmpty()) {
                List<com.expensetracker.presentation.transactions.TransactionListItem> groupedList = groupTransactionsByMonth(expenses);
                transactionAdapter.submitList(groupedList);
                binding.rvRecentTransactions.setVisibility(View.VISIBLE);
                binding.emptyState.setVisibility(View.GONE);
            } else {
                binding.rvRecentTransactions.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });

        // Pie chart data
        viewModel.getMonthlyExpenses().observe(getViewLifecycleOwner(), this::setupPieChart);
    }

    private List<com.expensetracker.presentation.transactions.TransactionListItem> groupTransactionsByMonth(List<ExpenseEntity> expenses) {
        List<com.expensetracker.presentation.transactions.TransactionListItem> groupedList = new ArrayList<>();
        String currentMonthYear = "";
        java.text.SimpleDateFormat monthYearFormat = new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault());

        for (ExpenseEntity expense : expenses) {
            String itemMonthYear = monthYearFormat.format(new java.util.Date(expense.getDate()));
            if (!itemMonthYear.equals(currentMonthYear)) {
                currentMonthYear = itemMonthYear;
                groupedList.add(new com.expensetracker.presentation.transactions.TransactionListItem(currentMonthYear));
            }
            groupedList.add(new com.expensetracker.presentation.transactions.TransactionListItem(expense));
        }
        return groupedList;
    }

    private void setupPieChart(List<ExpenseEntity> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            binding.pieChart.setVisibility(View.GONE);
            binding.legendContainer.setVisibility(View.GONE);
            return;
        }
        binding.pieChart.setVisibility(View.VISIBLE);
        binding.legendContainer.setVisibility(View.VISIBLE);

        // Group by category (expenses only)
        Map<String, Double> categoryMap = new HashMap<>();
        double totalExpense = 0;
        for (ExpenseEntity expense : expenses) {
            if ("expense".equalsIgnoreCase(expense.getType())) {
                String cat = expense.getCategory() != null ? expense.getCategory() : "Other";
                double current = categoryMap.getOrDefault(cat, 0.0);
                categoryMap.put(cat, current + expense.getAmount());
                totalExpense += expense.getAmount();
            }
        }

        if (categoryMap.isEmpty()) {
            binding.pieChart.setVisibility(View.GONE);
            binding.legendContainer.setVisibility(View.GONE);
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        List<String> sortedCategories = new ArrayList<>(categoryMap.keySet());
        Collections.sort(sortedCategories, (c1, c2) -> {
            Double v1 = categoryMap.get(c1);
            Double v2 = categoryMap.get(c2);
            return Double.compare(v2 != null ? v2 : 0, v1 != null ? v1 : 0);
        });

        for (String categoryName : sortedCategories) {
            Double amount = categoryMap.get(categoryName);
            if (amount != null) {
                entries.add(new PieEntry(amount.floatValue(), categoryName));
                colors.add(CategoryConstants.getCategoryColor(categoryName));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(4f);
        dataSet.setSelectionShift(8f);
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleColor(android.graphics.Color.TRANSPARENT);
        binding.pieChart.setHoleRadius(75f);
        binding.pieChart.setTransparentCircleRadius(80f);
        binding.pieChart.setDrawCenterText(true);
        
        final double total = totalExpense;
        binding.pieChart.setCenterText(CurrencyUtils.formatAmountShort(total) + "\nTotal");
        binding.pieChart.setCenterTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_primary_dark));
        binding.pieChart.setCenterTextSize(14f);
        
        binding.pieChart.getLegend().setEnabled(false);
        binding.pieChart.setDrawEntryLabels(false);

        binding.pieChart.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                binding.pieChart.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                PieEntry pe = (PieEntry) e;
                float percentage = (pe.getY() / (float) total) * 100f;
                binding.pieChart.setCenterText(pe.getLabel() + "\n" + String.format("%.1f%%", percentage));
            }

            @Override
            public void onNothingSelected() {
                binding.pieChart.setCenterText(CurrencyUtils.formatAmountShort(total) + "\nTotal");
            }
        });

        binding.pieChart.animateY(1000, Easing.EaseInOutQuad);
        binding.pieChart.invalidate();

        setupDashboardLegend(categoryMap, totalExpense);
    }

    private void setupDashboardLegend(Map<String, Double> categoryMap, double totalExpense) {
        binding.legendContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        List<String> sortedCategories = new ArrayList<>(categoryMap.keySet());
        Collections.sort(sortedCategories, (c1, c2) -> {
            Double v1 = categoryMap.get(c1);
            Double v2 = categoryMap.get(c2);
            return Double.compare(v2 != null ? v2 : 0, v1 != null ? v1 : 0);
        });

        for (String categoryName : sortedCategories) {
            Double amount = categoryMap.get(categoryName);
            if (amount == null) continue;

            View legendItem = inflater.inflate(R.layout.item_chart_legend, binding.legendContainer, false);
            View colorView = legendItem.findViewById(R.id.view_color);
            TextView tvCategory = legendItem.findViewById(R.id.tv_category);
            TextView tvAmount = legendItem.findViewById(R.id.tv_amount);

            int color = CategoryConstants.getCategoryColor(categoryName);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(10f);
            shape.setColor(color);
            colorView.setBackground(shape);

            tvCategory.setText(categoryName);

            double percentage = (amount / totalExpense) * 100;
            tvAmount.setText(String.format("%.1f%%", percentage));

            legendItem.setOnClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                for (int i = 0; i < binding.pieChart.getData().getDataSet().getEntryCount(); i++) {
                    PieEntry e = (PieEntry) binding.pieChart.getData().getDataSet().getEntryForIndex(i);
                    if (e.getLabel().equals(categoryName)) {
                        binding.pieChart.highlightValue(i, 0);
                        break;
                    }
                }
            });

            binding.legendContainer.addView(legendItem);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
