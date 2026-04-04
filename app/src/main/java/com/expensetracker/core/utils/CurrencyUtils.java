package com.expensetracker.core.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {

    private static final String CURRENCY_SYMBOL = "₹";

    public static String formatAmount(double amount) {
        if (amount == 0) return CURRENCY_SYMBOL + "0.00";
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return CURRENCY_SYMBOL + nf.format(amount);
    }

    public static String formatAmountWithSign(double amount, boolean isExpense) {
        String formatted = formatAmount(amount);
        return isExpense ? "- " + formatted : "+ " + formatted;
    }

    public static String formatAmountShort(double amount) {
        if (amount >= 10000000) {
            return CURRENCY_SYMBOL + String.format(Locale.getDefault(), "%.1fCr", amount / 10000000);
        } else if (amount >= 100000) {
            return CURRENCY_SYMBOL + String.format(Locale.getDefault(), "%.1fL", amount / 100000);
        } else if (amount >= 1000) {
            return CURRENCY_SYMBOL + String.format(Locale.getDefault(), "%.1fK", amount / 1000);
        } else {
            return formatAmount(amount);
        }
    }
}
