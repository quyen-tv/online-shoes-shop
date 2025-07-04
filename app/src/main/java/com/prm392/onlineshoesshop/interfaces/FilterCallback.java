package com.prm392.onlineshoesshop.interfaces;

import com.prm392.onlineshoesshop.model.FilterState;

/**
 * Interface for handling filter state changes
 * Follows the Observer pattern for loose coupling
 */
public interface FilterCallback {

    /**
     * Called when filter state changes
     * 
     * @param filterState The new filter state
     */
    void onFilterChanged(FilterState filterState);

    /**
     * Called when filters are reset
     */
    default void onFiltersReset() {
        // Default implementation does nothing
    }
}