package com.expensetracker.presentation.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.expensetracker.databinding.ActivitySplashBinding;
import com.expensetracker.presentation.MainActivity;
import android.widget.TextView;
import android.view.View;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Start Premium Animations
        startTextAnimation(binding.loaderTextContainer);

        // Hide old static elements as we're using the animated text loader
        binding.splashTitle.setVisibility(View.GONE);
        binding.splashTagline.setVisibility(View.GONE);

        // Navigate to main after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }

    /**
     * Creates a premium letter-by-letter animation for "Expense Tracker"
     */
    private void startTextAnimation(LinearLayout container) {
        String text = "EXPENSE";
        String text2 = "TRACKER";
        
        // Add "EXPENSE" in Primary Color
        animateWords(container, text, Color.parseColor("#6C63FF"), 0);
        
        // Add a space
        View space = new View(this);
        space.setLayoutParams(new LinearLayout.LayoutParams(30, 1));
        container.addView(space);

        // Add "TRACKER" in White
        animateWords(container, text2, Color.WHITE, text.length() * 100);
    }

    private void animateWords(LinearLayout container, String word, int color, int startDelay) {
        for (int i = 0; i < word.length(); i++) {
            TextView tv = new TextView(this);
            tv.setText(String.valueOf(word.charAt(i)));
            tv.setTextSize(32);
            tv.setTextColor(color);
            tv.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
            tv.setAlpha(0f);
            tv.setTranslationY(50f);
            
            container.addView(tv);
            
            tv.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(startDelay + (i * 100))
                .setInterpolator(new DecelerateInterpolator())
                .start();
        }
    }
}
