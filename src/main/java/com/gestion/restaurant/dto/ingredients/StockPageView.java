package com.gestion.restaurant.dto.ingredients;

import org.springframework.data.domain.Page;

/**
 * Page stock + KPI globaux (calculés hors pagination).
 */
public record StockPageView(
        Page<IngredientStockDTO> page,
        long nombreAlerteStock,
        long nombreStockOk,
        double seuilStockFaible
) {
}
