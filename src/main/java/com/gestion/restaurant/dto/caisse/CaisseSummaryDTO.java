package com.gestion.restaurant.dto.caisse;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CaisseSummaryDTO {
    private BigDecimal totalEntrees = BigDecimal.ZERO;
    private BigDecimal totalSorties = BigDecimal.ZERO;
    private BigDecimal achatsIngredients = BigDecimal.ZERO;
    private BigDecimal achatsMaterielles = BigDecimal.ZERO;
    private BigDecimal maintenance = BigDecimal.ZERO;
    private BigDecimal ventes = BigDecimal.ZERO;
}
