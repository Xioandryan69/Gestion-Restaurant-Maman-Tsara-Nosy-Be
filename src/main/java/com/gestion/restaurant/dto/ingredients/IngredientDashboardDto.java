package com.gestion.restaurant.dto.ingredients;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class IngredientDashboardDto {
    private long totalIngredients;
    private long ingredientsEnStock;
    private long ingredientsEnAlerte;
    private BigDecimal stockTotal;
    private BigDecimal valeurTotale;
    private String etatStock;
    private String derniereMiseAJour;
    private List<String> alerts = new ArrayList<>();
    private List<String> movements = new ArrayList<>();
    private List<String> quickActions = new ArrayList<>();
}
