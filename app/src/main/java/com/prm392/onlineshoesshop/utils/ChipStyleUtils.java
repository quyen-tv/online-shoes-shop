package com.prm392.onlineshoesshop.utils;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.prm392.onlineshoesshop.R;

/**
 * Utility class for managing chip styling and animations
 * Follows the Single Responsibility Principle
 */
public class ChipStyleUtils {

    private ChipStyleUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Applies selected style to a chip with animation
     * 
     * @param context The context
     * @param chip    The chip to style
     */
    public static void applySelectedStyle(@NonNull Context context, @NonNull Chip chip) {
        applyAnimation(context, chip);
        chip.setChipBackgroundColorResource(R.color.purple);
        chip.setTextColor(ContextCompat.getColor(context, R.color.white));
        chip.setChipIconTint(ContextCompat.getColorStateList(context, R.color.white_color));
    }

    /**
     * Applies unselected style to a chip with animation
     * 
     * @param context The context
     * @param chip    The chip to style
     */
    public static void applyUnselectedStyle(@NonNull Context context, @NonNull Chip chip) {
        applyAnimation(context, chip);
        chip.setChipBackgroundColorResource(R.color.light_grey);
        chip.setTextColor(ContextCompat.getColor(context, R.color.black));
        chip.setChipIconTint(ContextCompat.getColorStateList(context, R.color.black_color));
    }

    /**
     * Applies style to a chip based on selection state
     * 
     * @param context    The context
     * @param chip       The chip to style
     * @param isSelected Whether the chip is selected
     */
    public static void applyStyle(@NonNull Context context, @NonNull Chip chip, boolean isSelected) {
        if (isSelected) {
            applySelectedStyle(context, chip);
        } else {
            applyUnselectedStyle(context, chip);
        }
    }

    /**
     * Applies fade-in animation to a chip
     * 
     * @param context The context
     * @param chip    The chip to animate
     */
    private static void applyAnimation(@NonNull Context context, @NonNull Chip chip) {
        Animation fadeAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        chip.startAnimation(fadeAnimation);
    }
}