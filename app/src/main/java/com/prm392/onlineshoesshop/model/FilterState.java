package com.prm392.onlineshoesshop.model;

import androidx.annotation.NonNull;

/**
 * Model class representing the current filter state
 * Follows the Immutable Object pattern for better thread safety
 */
public class FilterState {

    private final boolean isInStockSelected;
    private final boolean isPriceRangeSelected;
    private final SortType sortType;
    private final double minPrice;
    private final double maxPrice;
    private final PriceRangeType priceRangeType;

    public FilterState() {
        this(false, false, SortType.NONE, 0.0, 0.0, PriceRangeType.NONE);
    }

    private FilterState(boolean isInStockSelected, boolean isPriceRangeSelected, SortType sortType,
            double minPrice, double maxPrice, PriceRangeType priceRangeType) {
        this.isInStockSelected = isInStockSelected;
        this.isPriceRangeSelected = isPriceRangeSelected;
        this.sortType = sortType;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.priceRangeType = priceRangeType;
    }

    public boolean isInStockSelected() {
        return isInStockSelected;
    }

    public boolean isPriceRangeSelected() {
        return isPriceRangeSelected;
    }

    public SortType getSortType() {
        return sortType;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public PriceRangeType getPriceRangeType() {
        return priceRangeType;
    }

    /**
     * Creates a new FilterState with toggled in-stock selection
     * 
     * @return New FilterState instance
     */
    @NonNull
    public FilterState toggleInStock() {
        return new FilterState(!isInStockSelected, isPriceRangeSelected, sortType, minPrice, maxPrice, priceRangeType);
    }

    /**
     * Creates a new FilterState with toggled price range selection
     * 
     * @return New FilterState instance
     */
    @NonNull
    public FilterState togglePriceRange() {
        return new FilterState(isInStockSelected, !isPriceRangeSelected, sortType, minPrice, maxPrice, priceRangeType);
    }

    /**
     * Creates a new FilterState with specific price range
     * 
     * @param minPrice       The minimum price
     * @param maxPrice       The maximum price
     * @param priceRangeType The type of price range
     * @return New FilterState instance
     */
    @NonNull
    public FilterState setPriceRange(double minPrice, double maxPrice, PriceRangeType priceRangeType) {
        return new FilterState(isInStockSelected, true, sortType, minPrice, maxPrice, priceRangeType);
    }

    /**
     * Creates a new FilterState with updated sort type
     * 
     * @param newSortType The new sort type
     * @return New FilterState instance
     */
    @NonNull
    public FilterState setSortType(SortType newSortType) {
        SortType finalSortType = (sortType == newSortType) ? SortType.NONE : newSortType;
        return new FilterState(isInStockSelected, isPriceRangeSelected, finalSortType, minPrice, maxPrice,
                priceRangeType);
    }

    /**
     * Creates a new FilterState with all filters reset
     * 
     * @return New FilterState instance
     */
    @NonNull
    public FilterState reset() {
        return new FilterState(false, false, SortType.NONE, 0.0, 0.0, PriceRangeType.NONE);
    }

    /**
     * Checks if any filter is currently active
     * 
     * @return true if any filter is selected
     */
    public boolean hasActiveFilters() {
        return isInStockSelected || isPriceRangeSelected || sortType != SortType.NONE;
    }

    /**
     * Enum representing different price range types
     */
    public enum PriceRangeType {
        NONE,
        UNDER_50,
        FIFTY_TO_100,
        HUNDRED_TO_200,
        TWO_HUNDRED_TO_500,
        OVER_500,
        CUSTOM
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        FilterState that = (FilterState) obj;
        return isInStockSelected == that.isInStockSelected &&
                isPriceRangeSelected == that.isPriceRangeSelected &&
                sortType == that.sortType &&
                Double.compare(that.minPrice, minPrice) == 0 &&
                Double.compare(that.maxPrice, maxPrice) == 0 &&
                priceRangeType == that.priceRangeType;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (isInStockSelected ? 1 : 0);
        result = 31 * result + (isPriceRangeSelected ? 1 : 0);
        result = 31 * result + sortType.hashCode();
        result = 31 * result + Double.hashCode(minPrice);
        result = 31 * result + Double.hashCode(maxPrice);
        result = 31 * result + priceRangeType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FilterState{" +
                "isInStockSelected=" + isInStockSelected +
                ", isPriceRangeSelected=" + isPriceRangeSelected +
                ", sortType=" + sortType +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", priceRangeType=" + priceRangeType +
                '}';
    }

    /**
     * Enum representing different sort types
     */
    public enum SortType {
        NONE,
        PRICE_LOW,
        PRICE_HIGH,
        POPULAR
    }
}