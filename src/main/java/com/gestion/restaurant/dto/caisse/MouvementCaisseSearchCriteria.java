package com.gestion.restaurant.dto.caisse;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

/** Critères conservés dans l'URL pour filtrer les mouvements de caisse. */
@Data
public class MouvementCaisseSearchCriteria {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateDebut;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFin;
    private Long idTypeMouvement;
}
