package com.gestion.restaurant.dto.ingredients;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IngredientMovementSummaryDTO {
    private Long ingredientId;
    private String nom;
    private BigDecimal entreeTotal = BigDecimal.ZERO;
    private BigDecimal sortieTotal = BigDecimal.ZERO;
    private BigDecimal stockCurrent = BigDecimal.ZERO;

    public IngredientMovementSummaryDTO() {}
}
