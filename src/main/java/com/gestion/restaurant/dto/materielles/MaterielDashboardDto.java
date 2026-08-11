package com.gestion.restaurant.dto.materielles;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MaterielDashboardDto {
    private long totalMateriels;
    private long modulesEnLigne;
    private long modulesHorsLigne;
    private double tauxErreur;
    private String etatTempsReel;
    private String derniereSynchronisation;
    private String niveauAlimentation;
    private List<String> alerts = new ArrayList<>();
    private List<String> activities = new ArrayList<>();
    private List<String> quickActions = new ArrayList<>();
}
