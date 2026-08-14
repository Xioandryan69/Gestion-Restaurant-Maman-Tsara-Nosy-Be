package com.gestion.restaurant.dto.commandes;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.math.BigDecimal;

/** Critères de recherche multicritère pour la liste des commandes. */
@Data
public class CommandeSearchCriteria {
    private Long id;
    private String client;
    private Long idZoneLivraison;
    private BigDecimal montantMin;
    private BigDecimal montantMax;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateDebut;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFin;
}
