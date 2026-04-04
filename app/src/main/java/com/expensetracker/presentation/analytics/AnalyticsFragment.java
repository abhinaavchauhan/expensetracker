package com.expensetracker.presentation.analytics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.expensetracker.R;
import com.expensetracker.core.constants.CategoryConstants;
import com.expensetracker.core.utils.CurrencyUtils;
import com.expensetracker.core.utils.DateUtils;
import com.expensetracker.core.utils.PreferenceManager;
import com.expensetracker.data.local.entity.ExpenseEntity;
import com.expensetracker.databinding.FragmentAnalyticsBinding;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private AnalyticsViewModel viewModel;
    private PreferenceManager preferenceManager;
    private LiveData<List<ExpenseEntity>> currentExpensesData;

    private int currentPeriod = 1; // 0=weekly, 1=monthly, 2=yearly

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AnalyticsViewModel.class);
        preferenceManager = new PreferenceManager(requireContext());

        setupTabs();
        loadData(currentPeriod);
    }

    private void setupTabs() {
        binding.tabPeriod.addTab(binding.tabPeriod.newTab().setText(R.string.weekly));
        binding.tabPeriod.addTab(binding.tabPeriod.newTab().setText(R.string.monthly));
        binding.tabPeriod.addTab(binding.tabPeriod.newTab().setText(R.string.yearly));

        // Select monthly by default
        TabLayout.Tab monthlyTab = binding.tabPeriod.getTabAt(1);
        if (monthlyTab != null) monthlyTab.select();

        binding.tabPeriod.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentPeriod = tab.getPosition();
                loadData(currentPeriod);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadData(int period) {
        // Remove previous observer if any
        if (currentExpensesData != null) {
            currentExpensesData.removeObservers(getViewLifecycleOwner());
        }

        long startDate, endDate;
        switch (period) {
            case 0: // Weekly
                startDate = DateUtils.getStartOfWeek();
                endDate = DateUtils.getEndOfWeek();
                break;
            case 2: // Yearly
                startDate = DateUtils.getStartOfYear();
                endDate = DateUtils.getEndOfYear();
                break;
            default: // Monthly
                startDate = DateUtils.getStartOfMonth();
                endDate = DateUtils.getEndOfMonth();
                break;
        }

        currentExpensesData = viewModel.getExpensesByDateRange(startDate, endDate);
        currentExpensesData.observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null && !expenses.isEmpty()) {
                setupBarChart(expenses, period);
                setupPieChart(expenses);
                updateInsights(expenses, startDate, endDate);
            } else {
                clearCharts();
            }
        });
    }

    private void setupBarChart(List<ExpenseEntity> expenses, int period) {
        Map<String, Float> dayMap = new HashMap<>();
        List<String> labels = new ArrayList<>();

        if (period == 0) { // Weekly
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for (String day : days) {
                dayMap.put(day, 0f);
                labels.add(day);
            }
            for (ExpenseEntity expense : expenses) {
                if ("expense".equalsIgnoreCase(expense.getType())) {
                    String dayName = DateUtils.getDayName(expense.getDate());
                    float current = dayMap.containsKey(dayName) ? dayMap.get(dayName) : 0f;
                    dayMap.put(dayName, current + (float) expense.getAmount());
                }
            }
        } else if (period == 1) { // Monthly - group by week
            labels.add("Week 1");
            labels.add("Week 2");
            labels.add("Week 3");
            labels.add("Week 4");
            for (String label : labels) dayMap.put(label, 0f);

            for (ExpenseEntity expense : expenses) {
                if ("expense".equalsIgnoreCase(expense.getType())) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(expense.getDate());
                    int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
                    String key = "Week " + Math.min(weekOfMonth, 4);
                    float current = dayMap.containsKey(key) ? dayMap.get(key) : 0f;
                    dayMap.put(key, current + (float) expense.getAmount());
                }
            }
        } else { // Yearly - group by month
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            for (String month : months) {
                dayMap.put(month, 0f);
                labels.add(month);
            }
            for (ExpenseEntity expense : expenses) {
                if ("expense".equalsIgnoreCase(expense.getType())) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(expense.getDate());
                    String key = months[cal.get(Calendar.MONTH)];
                    float current = dayMap.containsKey(key) ? dayMap.get(key) : 0f;
                    dayMap.put(key, current + (float) expense.getAmount());
                }
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            float value = dayMap.containsKey(labels.get(i)) ? dayMap.get(labels.get(i)) : 0f;
            entries.add(new BarEntry(i, value));
        }

        int axisTextColor = getResources().getColor(R.color.chart_axis_text);
        int gridColor = getResources().getColor(R.color.chart_grid);

        BarDataSet dataSet = new BarDataSet(entries, "Expenses");
        dataSet.setColor(getResources().getColor(R.color.primary));
        dataSet.setValueTextColor(getResources().getColor(R.color.chart_value_text));
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        binding.barChart.setData(data);
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.getLegend().setEnabled(false);
        binding.barChart.setDrawGridBackground(false);
        binding.barChart.setDrawBarShadow(false);

        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(axisTextColor);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        binding.barChart.getAxisLeft().setTextColor(axisTextColor);
        binding.barChart.getAxisLeft().setDrawGridLines(true);
        binding.barChart.getAxisLeft().setGridColor(gridColor);
        binding.barChart.getAxisRight().setEnabled(false);

        binding.barChart.animateY(1000, Easing.EaseInOutQuad);
        binding.barChart.invalidate();
    }

    private void setupPieChart(List<ExpenseEntity> expenses) {
        Map<String, Double> categoryMap = new HashMap<>();
        for (ExpenseEntity expense : expenses) {
            if ("expense".equalsIgnoreCase(expense.getType())) {
                String cat = expense.getCategory() != null ? expense.getCategory() : "Other";
                double current = categoryMap.containsKey(cat) ? categoryMap.get(cat) : 0;
                categoryMap.put(cat, current + expense.getAmount());
            }
        }

        if (categoryMap.isEmpty()) {
            binding.pieChart.setVisibility(View.GONE);
            return;
        }

        binding.pieChart.setVisibility(View.VISIBLE);

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
        int labelColor = getResources().getColor(R.color.text_primary_dark);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleColor(holeColor);
        binding.pieChart.setHoleRadius(45f);
        binding.pieChart.setTransparentCircleRadius(50f);
        binding.pieChart.setTransparentCircleColor(holeColor);
        binding.pieChart.setDrawCenterText(false);
        binding.pieChart.getLegend().setEnabled(false);
        binding.pieChart.setEntryLabelColor(labelColor);
        binding.pieChart.setEntryLabelTextSize(10f);
        binding.pieChart.animateY(1000, Easing.EaseInOutQuad);
        binding.pieChart.invalidate();
    }

    private void updateInsights(List<ExpenseEntity> expenses, long startDate, long endDate) {
        // Calculate top category
        Map<String, Double> categoryTotals = new HashMap<>();
        double totalExpenses = 0;
        int expenseCount = 0;

        for (ExpenseEntity expense : expenses) {
            if ("expense".equalsIgnoreCase(expense.getType())) {
                totalExpenses += expense.getAmount();
                expenseCount++;
                String cat = expense.getCategory() != null ? expense.getCategory() : "Other";
                double current = categoryTotals.containsKey(cat) ? categoryTotals.get(cat) : 0;
                categoryTotals.put(cat, current + expense.getAmount());
            }
        }

        // Top category
        String topCategory = "—";
        double topAmount = 0;
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > topAmount) {
                topAmount = entry.getValue();
                topCategory = entry.getKey();
            }
        }
        binding.tvTopCategory.setText(topCategory);

        // Average daily
        long days = Math.max(1, (endDate - startDate) / (24 * 60 * 60 * 1000));
        double avgDaily = totalExpenses / days;
        binding.tvAvgDaily.setText(CurrencyUtils.formatAmount(avgDaily));

        // Total transactions
        binding.tvTotalTransactions.setText(String.valueOf(expenses.size()));

        // Budget used
        double budget = preferenceManager.getMonthlyBudget();
        if (budget > 0) {
            int percent = (int) ((totalExpenses / budget) * 100);
            binding.tvBudgetUsed.setText(percent + "%");
        } else {
            binding.tvBudgetUsed.setText("N/A");
        }
    }

    private void clearCharts() {
        binding.barChart.clear();
        binding.pieChart.clear();
        binding.tvTopCategory.setText("—");
        binding.tvAvgDaily.setText(CurrencyUtils.formatAmount(0));
        binding.tvTotalTransactions.setText("0");
        binding.tvBudgetUsed.setText("0%");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
