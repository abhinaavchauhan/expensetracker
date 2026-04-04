package com.expensetracker.presentation.addexpense;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.expensetracker.R;
import com.expensetracker.core.animations.AnimationUtils;
import com.expensetracker.core.constants.CategoryConstants;
import com.expensetracker.core.utils.DateUtils;
import com.expensetracker.databinding.FragmentAddExpenseBinding;
import com.expensetracker.domain.model.Category;

import java.util.Calendar;

public class AddExpenseFragment extends Fragment {

    private FragmentAddExpenseBinding binding;
    private AddExpenseViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    private String selectedCategory = null;
    private long selectedDate = System.currentTimeMillis();
    private boolean isExpenseType = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddExpenseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AddExpenseViewModel.class);

        setupUI();
        setupCategoryGrid();
        observeViewModel();
    }

    private void setupUI() {
        // Back button
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        // Type toggle
        binding.btnExpenseType.setOnClickListener(v -> {
            isExpenseType = true;
            updateTypeToggle();
            categoryAdapter.setCategories(CategoryConstants.getExpenseCategories());
            selectedCategory = null;
        });

        binding.btnIncomeType.setOnClickListener(v -> {
            isExpenseType = false;
            updateTypeToggle();
            categoryAdapter.setCategories(CategoryConstants.getIncomeCategories());
            selectedCategory = null;
        });

        // Date picker
        binding.tvDate.setText(DateUtils.formatDate(selectedDate));
        binding.llDate.setOnClickListener(v -> showDatePicker());

        // Save button
        binding.btnSave.setOnClickListener(v -> {
            AnimationUtils.scalePress(v);
            saveExpense();
        });

        // Animate elements
        AnimationUtils.slideUpFadeIn(binding.etAmount, 100);
    }

    private void updateTypeToggle() {
        if (isExpenseType) {
            binding.btnExpenseType.setBackgroundResource(R.drawable.bg_gradient_card);
            binding.btnExpenseType.setTextColor(getResources().getColor(R.color.white));
            binding.btnIncomeType.setBackground(null);
            binding.btnIncomeType.setTextColor(getResources().getColor(R.color.text_secondary_dark));
        } else {
            binding.btnIncomeType.setBackgroundResource(R.drawable.bg_gradient_card);
            binding.btnIncomeType.setTextColor(getResources().getColor(R.color.white));
            binding.btnExpenseType.setBackground(null);
            binding.btnExpenseType.setTextColor(getResources().getColor(R.color.text_secondary_dark));
        }
    }

    private void setupCategoryGrid() {
        categoryAdapter = new CategoryAdapter();
        binding.rvCategories.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        binding.rvCategories.setAdapter(categoryAdapter);
        binding.rvCategories.setNestedScrollingEnabled(false);

        categoryAdapter.setCategories(CategoryConstants.getExpenseCategories());
        categoryAdapter.setOnCategorySelectedListener(category -> {
            selectedCategory = category.getName();
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDate);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    selectedDate = selected.getTimeInMillis();
                    binding.tvDate.setText(DateUtils.formatDate(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void saveExpense() {
        String amountStr = binding.etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), R.string.invalid_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), R.string.invalid_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(requireContext(), R.string.invalid_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategory == null || selectedCategory.isEmpty()) {
            Toast.makeText(requireContext(), R.string.select_category_error, Toast.LENGTH_SHORT).show();
            return;
        }

        String note = binding.etNote.getText().toString().trim();
        String type = isExpenseType ? "expense" : "income";

        viewModel.saveExpense(amount, selectedCategory, note, selectedDate, type);
    }

    private void observeViewModel() {
        viewModel.getSavingResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show();
                if (getView() != null) {
                    Navigation.findNavController(getView()).navigateUp();
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
