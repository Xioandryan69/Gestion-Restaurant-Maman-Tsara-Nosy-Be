package com.gestion.restaurant.dto.plats;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlatUpdateRequestDto {

    private Long id;

    @NotBlank(message = "Le nom du plat est obligatoire")
    @Size(max = 255, message = "Le nom ne doit pas dépasser 255 caractères")
    private String nom;

    @NotNull(message = "La catégorie est obligatoire")
    private Long idCategorie;

    @NotNull(message = "Le prix de vente est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true,
            message = "Le prix de vente doit être positif ou nul")
    private BigDecimal prixVente;
}