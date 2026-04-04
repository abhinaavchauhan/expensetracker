package com.expensetracker;

import android.app.Application;

import com.expensetracker.core.utils.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;

public class ExpenseTrackerApp extends Application {

    private PreferenceManager preferenceManager;

    @Override
    public void onCreate() {
        super.onCreate();

        preferenceManager = new PreferenceManager(this);

        // Apply theme based on preference
        if (preferenceManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }
}
