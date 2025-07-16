package com.prm392.onlineshoesshop.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.DialogPriceRangeBinding;
import com.prm392.onlineshoesshop.model.FilterState;

/**
 * Utility class for managing price range selection dialog
 */
public class PriceRangeDialog {

    private final Context context;
    private final OnPriceRangeSelectedListener listener;
    private Dialog dialog;
    private DialogPriceRangeBinding binding;
    private FilterState.PriceRangeType selectedRangeType = FilterState.PriceRangeType.NONE;

    public interface OnPriceRangeSelectedListener {
        void onPriceRangeSelected(double minPrice, double maxPrice, FilterState.PriceRangeType rangeType);
    }

    public PriceRangeDialog(@NonNull Context context, @NonNull OnPriceRangeSelectedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show() {
        if (dialog != null && dialog.isShowing()) {
            return;
        }

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        binding = DialogPriceRangeBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        setupViews();
        setupListeners();
    }

    public void show(FilterState.PriceRangeType currentRangeType, double currentMinPrice, double currentMaxPrice) {
        if (dialog != null && dialog.isShowing()) {
            return;
        }

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        binding = DialogPriceRangeBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        setupViews();
        setupListeners();

        // Restore previous selection
        if (currentRangeType != FilterState.PriceRangeType.NONE) {
            selectedRangeType = currentRangeType;
            highlightSelectedCard(currentRangeType);
        }

        // Set dialog width to 90% of screen width
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setAttributes(layoutParams);
        }

        dialog.show();
    }

    private void setupViews() {
        // Reset all cards to unselected state
        resetAllCards();
    }

    private void setupListeners() {
        // Cancel button
        binding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Apply button
        binding.btnApply.setOnClickListener(v -> applyPriceRange());

        // Price range cards
        binding.cardAll.setOnClickListener(v -> selectPriceRange(FilterState.PriceRangeType.NONE, 0, Double.MAX_VALUE));
        binding.cardUnder50.setOnClickListener(v -> selectPriceRange(FilterState.PriceRangeType.UNDER_50, 0, 500_000));
        binding.card50to100
                .setOnClickListener(v -> selectPriceRange(FilterState.PriceRangeType.FIFTY_TO_100, 500_000, 1_000_000));
        binding.card100to200
                .setOnClickListener(
                        v -> selectPriceRange(FilterState.PriceRangeType.HUNDRED_TO_200, 1_000_000, 2_000_000));
        binding.cardOver200.setOnClickListener(
                v -> selectPriceRange(FilterState.PriceRangeType.OVER_200, 2_000_000, Double.MAX_VALUE));
    }

    private void selectPriceRange(FilterState.PriceRangeType rangeType, double minPrice, double maxPrice) {
        selectedRangeType = rangeType;
        resetAllCards();
        highlightSelectedCard(rangeType);
    }

    private void resetAllCards() {
        binding.cardAll.setStrokeColor(ContextCompat.getColor(context, R.color.light_grey));
        binding.cardUnder50.setStrokeColor(ContextCompat.getColor(context, R.color.light_grey));
        binding.card50to100.setStrokeColor(ContextCompat.getColor(context, R.color.light_grey));
        binding.card100to200.setStrokeColor(ContextCompat.getColor(context, R.color.light_grey));
        binding.cardOver200.setStrokeColor(ContextCompat.getColor(context, R.color.light_grey));
    }

    private void highlightSelectedCard(FilterState.PriceRangeType rangeType) {
        MaterialCardView selectedCard = null;

        switch (rangeType) {
            case NONE:
                selectedCard = binding.cardAll;
                break;
            case UNDER_50:
                selectedCard = binding.cardUnder50;
                break;
            case FIFTY_TO_100:
                selectedCard = binding.card50to100;
                break;
            case HUNDRED_TO_200:
                selectedCard = binding.card100to200;
                break;
            case OVER_200:
                selectedCard = binding.cardOver200;
                break;
        }

        if (selectedCard != null) {
            selectedCard.setStrokeColor(ContextCompat.getColor(context, R.color.purple));
        }
    }

    private void applyPriceRange() {
        double minPrice = 0;
        double maxPrice = 0;

        // Use predefined ranges
        switch (selectedRangeType) {
            case NONE:
                minPrice = 0;
                maxPrice = Double.MAX_VALUE;
                break;
            case UNDER_50:
                minPrice = 0;
                maxPrice = 500_000;
                break;
            case FIFTY_TO_100:
                minPrice = 500_000;
                maxPrice = 1_000_000;
                break;
            case HUNDRED_TO_200:
                minPrice = 1_000_000;
                maxPrice = 2_000_000;
                break;
            case OVER_200:
                minPrice = 2_000_000;
                maxPrice = Double.MAX_VALUE;
                break;
        }

        if (selectedRangeType != FilterState.PriceRangeType.NONE || (minPrice == 0 && maxPrice == Double.MAX_VALUE)) {
            listener.onPriceRangeSelected(minPrice, maxPrice, selectedRangeType);
            dialog.dismiss();
        } else {
            Toast.makeText(context, "Vui lòng chọn khoảng giá (VNĐ)", Toast.LENGTH_SHORT).show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}