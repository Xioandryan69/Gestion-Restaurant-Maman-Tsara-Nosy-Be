package com.gestion.restaurant.dto.materielles;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
@Data
@NoArgsConstructor
@AllArgsConstructor // Génère le constructeur avec (String, Long, Double)
public class MaintenanceStatDTO {
    private String mois; // Ex: "2026-01" ou "Janvier 2026"
    private Long nombreMaintenances;
    private Double coutTotal;


}