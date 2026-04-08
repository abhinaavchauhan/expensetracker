package com.expensetracker.presentation.analytics;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private AnalyticsViewModel viewModel;
    private PreferenceManager preferenceManager;
    private LiveData<List<ExpenseEntity>> currentExpensesData;

    private int currentPeriod = 1; // 0=weekly, 1=monthly, 2=yearly
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat periodFormatter;

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

        setupUI();
        setupTabs();
        loadData(currentPeriod);
    }

    private void setupUI() {
        binding.btnPrev.setOnClickListener(v -> {
            updatePeriod(-1);
            com.expensetracker.core.animations.AnimationUtils.scalePress(v);
        });

        binding.btnNext.setOnClickListener(v -> {
            updatePeriod(1);
            com.expensetracker.core.animations.AnimationUtils.scalePress(v);
        });

        binding.btnExport.setOnClickListener(v -> {
            com.expensetracker.core.animations.AnimationUtils.scalePress(v);
            showExportBottomSheet();
        });
    }

    private long fromExportDate, toExportDate;

    private void showExportBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext(), R.style.BottomSheetDialog);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_export_filter, null);
        dialog.setContentView(view);

        android.widget.TextView tvFromDate = view.findViewById(R.id.tv_from_date);
        android.widget.TextView tvToDate = view.findViewById(R.id.tv_to_date);
        com.google.android.material.chip.ChipGroup cgQuick = view.findViewById(R.id.cg_quick_filters);
        com.google.android.material.button.MaterialButton btnGenerate = view.findViewById(R.id.btn_generate_pdf);
        android.widget.ProgressBar progressBar = view.findViewById(R.id.pb_exporting);

        // Default: Last 30 Days
        java.util.Calendar cal = java.util.Calendar.getInstance();
        toExportDate = cal.getTimeInMillis();
        cal.add(java.util.Calendar.DAY_OF_YEAR, -30);
        fromExportDate = cal.getTimeInMillis();

        updateDateDisplays(tvFromDate, tvToDate);

        // Click listeners for manual date pickers
        view.findViewById(R.id.ll_from_date).setOnClickListener(v -> showDatePicker("Select Start Date", fromExportDate, timestamp -> {
            if (timestamp > toExportDate) {
                Toast.makeText(requireContext(), "Start date cannot be after end date", Toast.LENGTH_SHORT).show();
                return;
            }
            fromExportDate = timestamp;
            updateDateDisplays(tvFromDate, tvToDate);
            cgQuick.clearCheck();
        }));

        view.findViewById(R.id.ll_to_date).setOnClickListener(v -> showDatePicker("Select End Date", toExportDate, timestamp -> {
            if (timestamp < fromExportDate) {
                Toast.makeText(requireContext(), "End date cannot be before start date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (timestamp > System.currentTimeMillis()) {
                Toast.makeText(requireContext(), "Future dates not allowed", Toast.LENGTH_SHORT).show();
                return;
            }
            toExportDate = timestamp;
            updateDateDisplays(tvFromDate, tvToDate);
            cgQuick.clearCheck();
        }));

        // Quick filter logic
        cgQuick.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            java.util.Calendar c = java.util.Calendar.getInstance();
            toExportDate = c.getTimeInMillis();

            if (id == R.id.chip_last_7_days) {
                c.add(java.util.Calendar.DAY_OF_YEAR, -7);
                fromExportDate = c.getTimeInMillis();
            } else if (id == R.id.chip_last_30_days) {
                c.add(java.util.Calendar.DAY_OF_YEAR, -30);
                fromExportDate = c.getTimeInMillis();
            } else if (id == R.id.chip_this_month) {
                c.set(java.util.Calendar.DAY_OF_MONTH, 1);
                fromExportDate = c.getTimeInMillis();
            } else if (id == R.id.chip_last_month) {
                c.add(java.util.Calendar.MONTH, -1);
                c.set(java.util.Calendar.DAY_OF_MONTH, 1);
                fromExportDate = c.getTimeInMillis();
                c.set(java.util.Calendar.DAY_OF_MONTH, c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
                toExportDate = c.getTimeInMillis();
            }
            updateDateDisplays(tvFromDate, tvToDate);
        });

        btnGenerate.setOnClickListener(v -> {
            btnGenerate.setEnabled(false);
            btnGenerate.setAlpha(0.5f);
            progressBar.setVisibility(View.VISIBLE);

            new Thread(() -> {
                List<ExpenseEntity> transactions = viewModel.getExpensesByDateRangeSync(fromExportDate, toExportDate);
                
                if (transactions.isEmpty()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "No data found for selected range", Toast.LENGTH_SHORT).show();
                        btnGenerate.setEnabled(true);
                        btnGenerate.setAlpha(1.0f);
                        progressBar.setVisibility(View.GONE);
                    });
                    return;
                }

                String reportType = "Custom";
                int checkedChip = cgQuick.getCheckedChipId();
                if (checkedChip == R.id.chip_last_7_days) reportType = "Weekly";
                else if (checkedChip == R.id.chip_last_30_days) reportType = "Monthly";
                else if (checkedChip == R.id.chip_this_month) reportType = "Current_Month";

                com.expensetracker.core.export.PdfGenerator generator = new com.expensetracker.core.export.PdfGenerator(requireContext());
                generator.generateReport(reportType, fromExportDate, toExportDate, transactions, new com.expensetracker.core.export.PdfGenerator.ExportCallback() {
                    @Override
                    public void onSuccess(String fileName) {
                        Toast.makeText(requireContext(), "Report Downloaded: " + fileName, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnGenerate.setEnabled(true);
                        btnGenerate.setAlpha(1.0f);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }).start();
        });

        dialog.show();
    }

    private void updateDateDisplays(android.widget.TextView from, android.widget.TextView to) {
        from.setText(DateUtils.formatDateShort(fromExportDate));
        to.setText(DateUtils.formatDateShort(toExportDate));
    }

    private void showDatePicker(String title, long selection, OnDateSelectedListener listener) {
        com.google.android.material.datepicker.MaterialDatePicker<Long> picker = 
            com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setSelection(selection)
                .build();

        picker.addOnPositiveButtonClickListener(listener::onDateSelected);
        picker.show(getChildFragmentManager(), "DATE_PICKER");
    }

    private interface OnDateSelectedListener {
        void onDateSelected(long timestamp);
    }

    private void updatePeriod(int delta) {
        switch (currentPeriod) {
            case 0: // Weekly
                calendar.add(Calendar.WEEK_OF_YEAR, delta);
                break;
            case 1: // Monthly
                calendar.add(Calendar.MONTH, delta);
                break;
            case 2: // Yearly
                calendar.add(Calendar.YEAR, delta);
                break;
        }
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
                // Reset calendar to current date when switching tabs? 
                // Or keep the relative period? Usually better to keep relative or reset.
                // Resetting to current period for simplicity.
                calendar = Calendar.getInstance();
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
        String rangeText;

        Calendar tempCal = (Calendar) calendar.clone();

        switch (period) {
            case 0: // Weekly
                tempCal.set(Calendar.DAY_OF_WEEK, tempCal.getFirstDayOfWeek());
                tempCal.set(Calendar.HOUR_OF_DAY, 0);
                tempCal.set(Calendar.MINUTE, 0);
                tempCal.set(Calendar.SECOND, 0);
                tempCal.set(Calendar.MILLISECOND, 0);
                startDate = tempCal.getTimeInMillis();

                tempCal.add(Calendar.DAY_OF_WEEK, 6);
                tempCal.set(Calendar.HOUR_OF_DAY, 23);
                tempCal.set(Calendar.MINUTE, 59);
                tempCal.set(Calendar.SECOND, 59);
                tempCal.set(Calendar.MILLISECOND, 999);
                endDate = tempCal.getTimeInMillis();

                rangeText = DateUtils.formatDateShort(startDate) + " - " + DateUtils.formatDateShort(endDate);
                break;

            case 2: // Yearly
                tempCal.set(Calendar.MONTH, Calendar.JANUARY);
                tempCal.set(Calendar.DAY_OF_MONTH, 1);
                tempCal.set(Calendar.HOUR_OF_DAY, 0);
                tempCal.set(Calendar.MINUTE, 0);
                tempCal.set(Calendar.SECOND, 0);
                tempCal.set(Calendar.MILLISECOND, 0);
                startDate = tempCal.getTimeInMillis();

                tempCal.set(Calendar.MONTH, Calendar.DECEMBER);
                tempCal.set(Calendar.DAY_OF_MONTH, 31);
                tempCal.set(Calendar.HOUR_OF_DAY, 23);
                tempCal.set(Calendar.MINUTE, 59);
                tempCal.set(Calendar.SECOND, 59);
                tempCal.set(Calendar.MILLISECOND, 999);
                endDate = tempCal.getTimeInMillis();

                rangeText = String.valueOf(tempCal.get(Calendar.YEAR));
                break;

            default: // Monthly
                tempCal.set(Calendar.DAY_OF_MONTH, 1);
                tempCal.set(Calendar.HOUR_OF_DAY, 0);
                tempCal.set(Calendar.MINUTE, 0);
                tempCal.set(Calendar.SECOND, 0);
                tempCal.set(Calendar.MILLISECOND, 0);
                startDate = tempCal.getTimeInMillis();

                tempCal.set(Calendar.DAY_OF_MONTH, tempCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                tempCal.set(Calendar.HOUR_OF_DAY, 23);
                tempCal.set(Calendar.MINUTE, 59);
                tempCal.set(Calendar.SECOND, 59);
                tempCal.set(Calendar.MILLISECOND, 999);
                endDate = tempCal.getTimeInMillis();

                rangeText = DateUtils.formatMonthYear(startDate);
                break;
        }

        binding.tvPeriodRange.setText(rangeText);

        // Check if we can go next
        Calendar now = Calendar.getInstance();
        boolean canGoNext = true;
        switch (period) {
            case 0: // Weekly
                canGoNext = tempCal.getTimeInMillis() < now.getTimeInMillis();
                break;
            case 1: // Monthly
                canGoNext = tempCal.get(Calendar.MONTH) < now.get(Calendar.MONTH) || 
                            tempCal.get(Calendar.YEAR) < now.get(Calendar.YEAR);
                break;
            case 2: // Yearly
                canGoNext = tempCal.get(Calendar.YEAR) < now.get(Calendar.YEAR);
                break;
        }
        binding.btnNext.setEnabled(canGoNext);
        binding.btnNext.setAlpha(canGoNext ? 1.0f : 0.3f);

        currentExpensesData = viewModel.getExpensesByDateRange(startDate, endDate);
        currentExpensesData.observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null && !expenses.isEmpty()) {
                setupLineChart(expenses, period);
                setupPieChart(expenses);
                updateInsights(expenses, startDate, endDate);
            } else {
                clearCharts();
            }
        });
    }

    private void setupLineChart(List<ExpenseEntity> expenses, int period) {
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
                    float current = dayMap.getOrDefault(dayName, 0f);
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
                    float current = dayMap.getOrDefault(key, 0f);
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
                    float current = dayMap.getOrDefault(key, 0f);
                    dayMap.put(key, current + (float) expense.getAmount());
                }
            }
        }

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            float value = dayMap.getOrDefault(labels.get(i), 0f);
            entries.add(new Entry(i, value));
        }

        int axisTextColor = ContextCompat.getColor(requireContext(), R.color.chart_axis_text);
        int gridColor = ContextCompat.getColor(requireContext(), R.color.chart_grid);
        int primaryColor = ContextCompat.getColor(requireContext(), R.color.primary);

        LineDataSet dataSet = new LineDataSet(entries, "Expenses");
        dataSet.setColor(primaryColor);
        dataSet.setLineWidth(3f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(primaryColor);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setCircleHoleColor(ContextCompat.getColor(requireContext(), R.color.background_card_dark));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smooth curves
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(true);
        dataSet.setHighLightColor(primaryColor);
        dataSet.setDrawFilled(true);

        // Gradient Fill
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        ContextCompat.getColor(requireContext(), R.color.chart_gradient_start),
                        ContextCompat.getColor(requireContext(), R.color.chart_gradient_end)
                }
        );
        dataSet.setFillDrawable(gradient);

        LineData data = new LineData(dataSet);
        binding.lineChart.setData(data);
        binding.lineChart.getDescription().setEnabled(false);
        binding.lineChart.getLegend().setEnabled(false);
        binding.lineChart.setDrawGridBackground(false);
        binding.lineChart.setTouchEnabled(true);
        binding.lineChart.setDragEnabled(true);
        binding.lineChart.setScaleEnabled(false);
        binding.lineChart.setPinchZoom(false);

        // Tooltip
        CustomMarkerView marker = new CustomMarkerView(requireContext(), R.layout.layout_chart_tooltip, labels);
        marker.setChartView(binding.lineChart);
        binding.lineChart.setMarker(marker);

        XAxis xAxis = binding.lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(axisTextColor);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size());
        xAxis.setYOffset(10f);

        binding.lineChart.getAxisLeft().setTextColor(axisTextColor);
        binding.lineChart.getAxisLeft().setDrawGridLines(true);
        binding.lineChart.getAxisLeft().setGridColor(gridColor);
        binding.lineChart.getAxisLeft().setXOffset(10f);
        binding.lineChart.getAxisRight().setEnabled(false);

        binding.lineChart.animateX(800, Easing.EaseInOutQuad);
        binding.lineChart.invalidate();
    }

    private void setupPieChart(List<ExpenseEntity> expenses) {
        Map<String, Double> categoryMap = new HashMap<>();
        double totalAmount = 0;
        for (ExpenseEntity expense : expenses) {
            if ("expense".equalsIgnoreCase(expense.getType())) {
                String cat = expense.getCategory() != null ? expense.getCategory() : "Other";
                double current = categoryMap.getOrDefault(cat, 0.0);
                categoryMap.put(cat, current + expense.getAmount());
                totalAmount += expense.getAmount();
            }
        }

        if (categoryMap.isEmpty()) {
            binding.pieChart.setVisibility(View.GONE);
            binding.legendContainer.setVisibility(View.GONE);
            return;
        }

        binding.pieChart.setVisibility(View.VISIBLE);
        binding.legendContainer.setVisibility(View.VISIBLE);

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
        dataSet.setSelectionShift(10f);
        dataSet.setDrawValues(false); // Remove overlapping text

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleColor(Color.TRANSPARENT);
        binding.pieChart.setHoleRadius(75f); // Large hole for Donut feel
        binding.pieChart.setTransparentCircleRadius(80f);
        binding.pieChart.setDrawCenterText(true);
        
        // Dynamic Center Text
        final double total = totalAmount;
        binding.pieChart.setCenterText(CurrencyUtils.formatAmountShort(total) + "\nTotal");
        binding.pieChart.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_dark));
        binding.pieChart.setCenterTextSize(16f);
        
        binding.pieChart.getLegend().setEnabled(false);
        binding.pieChart.setDrawEntryLabels(false); // Remove labels from slices

        binding.pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
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

        setupCustomLegend(categoryMap, totalAmount);
    }

    private void setupCustomLegend(Map<String, Double> categoryMap, double totalAmount) {
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

            double percentage = (amount / totalAmount) * 100;
            tvAmount.setText(String.format("%.1f%%", percentage));

            legendItem.setOnClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                // Highlight corresponding slice in chart
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

    private void updateInsights(List<ExpenseEntity> expenses, long startDate, long endDate) {
        // Calculate top category
        Map<String, Double> categoryTotals = new HashMap<>();
        double totalExpenses = 0;

        for (ExpenseEntity expense : expenses) {
            if ("expense".equalsIgnoreCase(expense.getType())) {
                totalExpenses += expense.getAmount();
                String cat = expense.getCategory() != null ? expense.getCategory() : "Other";
                double current = categoryTotals.getOrDefault(cat, 0.0);
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
        long diff = endDate - startDate;
        long days = Math.max(1, diff / (24 * 60 * 60 * 1000));
        double avgDaily = totalExpenses / days;
        binding.tvAvgDaily.setText(CurrencyUtils.formatAmountShort(avgDaily));

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
        binding.lineChart.clear();
        binding.pieChart.clear();
        binding.legendContainer.removeAllViews();
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
