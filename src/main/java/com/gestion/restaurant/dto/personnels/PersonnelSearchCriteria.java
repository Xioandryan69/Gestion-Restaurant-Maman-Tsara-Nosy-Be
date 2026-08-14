package com.gestion.restaurant.dto.personnels;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class PersonnelSearchCriteria {
    private String nom;
    private String prenom;
    private Long idRole;
    private String contact;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateEmbaucheDebut;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateEmbaucheFin;
}
