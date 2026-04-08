package com.expensetracker.presentation;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.expensetracker.R;
import com.expensetracker.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("LIFECYCLE", "MainActivity Created");
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        setupFab();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            // Setup with NavController manually to add animations
            binding.bottomNavigation.setOnItemSelectedListener(item -> {
                if (item.getItemId() != binding.bottomNavigation.getSelectedItemId()) {
                    View itemView = binding.bottomNavigation.findViewById(item.getItemId());
                    if (itemView != null) {
                        com.expensetracker.core.animations.AnimationUtils.scaleBounce(itemView);
                    }
                    return NavigationUI.onNavDestinationSelected(item, navController);
                }
                return false;
            });

            // Keep selection in sync with back stack
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();
                android.view.Menu menu = binding.bottomNavigation.getMenu();
                for (int i = 0; i < menu.size(); i++) {
                    android.view.MenuItem item = menu.getItem(i);
                    if (item.getItemId() == destId) {
                        item.setChecked(true);
                        break;
                    }
                }
                
                // Bottom navigation visibility
                if (destId == R.id.nav_add_expense) {
                    binding.bottomNavigation.setVisibility(View.GONE);
                } else {
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                }
                
                // FAB visibility: Show only on Dashboard
                if (destId == R.id.nav_dashboard) {
                    binding.fabAdd.show();
                } else {
                    binding.fabAdd.hide();
                }
            });
        }
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            com.expensetracker.core.animations.AnimationUtils.scalePress(v);
            if (navController != null) {
                navController.navigate(R.id.nav_add_expense);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }
}
