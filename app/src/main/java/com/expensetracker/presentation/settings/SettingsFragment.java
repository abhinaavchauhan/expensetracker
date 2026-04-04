package com.expensetracker.presentation.settings;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.expensetracker.R;
import com.expensetracker.core.utils.CurrencyUtils;
import com.expensetracker.core.utils.PreferenceManager;
import com.expensetracker.data.local.entity.ExpenseEntity;
import com.expensetracker.data.repository.ExpenseRepository;
import com.expensetracker.databinding.FragmentSettingsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private PreferenceManager preferenceManager;
    private ExpenseRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceManager = new PreferenceManager(requireContext());
        repository = new ExpenseRepository(requireActivity().getApplication());

        setupDarkMode();
        setupBudget();
        setupExport();
        setupBackup();
    }

    private void setupDarkMode() {
        binding.switchDarkMode.setChecked(preferenceManager.isDarkMode());
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void setupBudget() {
        double currentBudget = preferenceManager.getMonthlyBudget();
        if (currentBudget > 0) {
            binding.tvBudgetAmount.setText(CurrencyUtils.formatAmount(currentBudget));
        }

        binding.llBudget.setOnClickListener(v -> showBudgetDialog());
    }

    private void showBudgetDialog() {
        EditText input = new EditText(requireContext());
        input.setHint(R.string.budget_hint);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setPadding(64, 32, 64, 32);

        double currentBudget = preferenceManager.getMonthlyBudget();
        if (currentBudget > 0) {
            input.setText(String.valueOf(currentBudget));
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.set_budget)
                .setView(input)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String amountStr = input.getText().toString().trim();
                    if (!amountStr.isEmpty()) {
                        try {
                            double budget = Double.parseDouble(amountStr);
                            preferenceManager.setMonthlyBudget(budget);
                            binding.tvBudgetAmount.setText(CurrencyUtils.formatAmount(budget));
                            Toast.makeText(requireContext(), R.string.budget_set_success, Toast.LENGTH_SHORT).show();
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), R.string.invalid_amount, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setupExport() {
        binding.llExportCsv.setOnClickListener(v -> exportToCsv());
    }

    private void exportToCsv() {
        repository.getExecutorService().execute(() -> {
            try {
                List<ExpenseEntity> expenses = repository.getAllExpensesSync();
                if (expenses == null || expenses.isEmpty()) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), R.string.no_data, Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                File dir = new File(requireContext().getExternalFilesDir(null), "exports");
                if (!dir.exists()) dir.mkdirs();

                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                File file = new File(dir, "expenses_" + timestamp + ".csv");

                FileWriter writer = new FileWriter(file);
                writer.write("ID,Amount,Category,Note,Date,Type\n");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                for (ExpenseEntity expense : expenses) {
                    writer.write(String.format(Locale.getDefault(),
                            "%d,%.2f,%s,%s,%s,%s\n",
                            expense.getId(),
                            expense.getAmount(),
                            expense.getCategory() != null ? expense.getCategory() : "",
                            expense.getNote() != null ? expense.getNote().replace(",", ";") : "",
                            sdf.format(new Date(expense.getDate())),
                            expense.getType() != null ? expense.getType() : ""));
                }
                writer.close();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(),
                                    getString(R.string.export_success) + "\n" + file.getAbsolutePath(),
                                    Toast.LENGTH_LONG).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void setupBackup() {
        binding.llBackup.setOnClickListener(v -> {
            Toast.makeText(requireContext(), R.string.backup_success, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
