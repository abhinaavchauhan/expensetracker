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
                Navigation.findNavController(v).navigate(R.id.nav_transactions);
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
                transactionAdapter.submitList(expenses);
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

    private void setupPieChart(List<ExpenseEntity> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            binding.pieChart.setVisibility(View.GONE);
            return;
        }
        binding.pieChart.setVisibility(View.VISIBLE);

        // Group by category
        Map<String, Double> categoryMap = new HashMap<>();
        for (ExpenseEntity expense : expenses) {
            String cat = expense.getCategory() != null ? expense.getCategory() : "Other";
            double current = categoryMap.containsKey(cat) ? categoryMap.get(cat) : 0;
            categoryMap.put(cat, current + expense.getAmount());
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            colors.add(CategoryConstants.getCategoryColor(entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(getResources().getColor(R.color.chart_value_text));
        dataSet.setValueFormatter(new PercentFormatter(binding.pieChart));

        int holeColor = getResources().getColor(R.color.chart_hole);
        int textColor = getResources().getColor(R.color.text_primary_dark);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleColor(holeColor);
        binding.pieChart.setHoleRadius(45f);
        binding.pieChart.setTransparentCircleRadius(50f);
        binding.pieChart.setTransparentCircleColor(holeColor);
        binding.pieChart.setTransparentCircleAlpha(100);
        binding.pieChart.setDrawCenterText(true);
        binding.pieChart.setCenterText("Expenses");
        binding.pieChart.setCenterTextColor(textColor);
        binding.pieChart.setCenterTextSize(14f);
        binding.pieChart.getLegend().setEnabled(false);
        binding.pieChart.setEntryLabelColor(textColor);
        binding.pieChart.setEntryLabelTextSize(10f);
        binding.pieChart.animateY(1000, Easing.EaseInOutQuad);
        binding.pieChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
