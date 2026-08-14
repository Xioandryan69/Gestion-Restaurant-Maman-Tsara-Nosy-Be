package com.gestion.restaurant.dto.ingredients;

import lombok.Data;

@Data
public class StockSearchCriteria {
    private String nom;
    private Long idCategorie;
    private Long idUnite;
}
