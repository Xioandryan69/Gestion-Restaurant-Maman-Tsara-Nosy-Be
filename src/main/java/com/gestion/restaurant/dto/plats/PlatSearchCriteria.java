package com.gestion.restaurant.dto.plats;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PlatSearchCriteria {
    private String nom;
    private Long idCategorie;
    private BigDecimal prixVenteMin;
    private BigDecimal prixVenteMax;
}
