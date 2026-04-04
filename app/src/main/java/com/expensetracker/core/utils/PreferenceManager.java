package com.expensetracker.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREF_NAME = "expense_tracker_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_BUDGET = "monthly_budget";
    private static final String KEY_FIRST_LAUNCH = "first_launch";

    private final SharedPreferences preferences;

    public PreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isDarkMode() {
        return preferences.getBoolean(KEY_DARK_MODE, true); // Dark mode by default
    }

    public void setDarkMode(boolean darkMode) {
        preferences.edit().putBoolean(KEY_DARK_MODE, darkMode).apply();
    }

    public double getMonthlyBudget() {
        return Double.longBitsToDouble(preferences.getLong(KEY_BUDGET, Double.doubleToLongBits(0.0)));
    }

    public void setMonthlyBudget(double budget) {
        preferences.edit().putLong(KEY_BUDGET, Double.doubleToLongBits(budget)).apply();
    }

    public boolean isFirstLaunch() {
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunchDone() {
        preferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    }
}
