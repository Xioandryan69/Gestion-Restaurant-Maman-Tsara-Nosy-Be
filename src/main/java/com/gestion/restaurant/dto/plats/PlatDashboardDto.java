package com.gestion.restaurant.dto.plats;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class PlatDashboardDto {
    private long totalPlats;
    private long platsAvecRecette;
    private long platsSansRecette;
    private BigDecimal chiffreAffairesPotentiel;
    private BigDecimal margePotentielle;
    private String tendance;
    private List<String> alerts = new ArrayList<>();
    private List<String> topPlats = new ArrayList<>();
    private List<String> quickActions = new ArrayList<>();
}
