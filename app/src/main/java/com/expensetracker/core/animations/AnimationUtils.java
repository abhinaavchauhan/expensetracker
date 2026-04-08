package com.expensetracker.core.animations;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.expensetracker.core.utils.CurrencyUtils;

public class AnimationUtils {

    /**
     * Animate a number counter from startValue to endValue on a TextView
     */
    public static void animateCounter(TextView textView, double startValue, double endValue, long duration) {
        if (textView == null) return;

        ValueAnimator animator = ValueAnimator.ofFloat((float) startValue, (float) endValue);
        animator.setDuration(duration);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            textView.setText(CurrencyUtils.formatAmount(value));
        });
        animator.start();
    }

    /**
     * Fade in animation for a view
     */
    public static void fadeIn(View view, long duration) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Fade out animation
     */
    public static void fadeOut(View view, long duration) {
        if (view == null) return;
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    /**
     * Scale up animation (press effect)
     */
    public static void scalePress(View view) {
        if (view == null) return;
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }

    /**
     * Slide up + fade in
     */
    public static void slideUpFadeIn(View view, long delay) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Scale animation from 0 to 1
     */
    public static void scaleIn(View view, long delay) {
        if (view == null) return;
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(0f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
    /**
     * Scale bounce animation
     */
    public static void scaleBounce(View view) {
        if (view == null) return;
        view.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .setInterpolator(new android.view.animation.OvershootInterpolator())
                        .start())
                .start();
    }
}
