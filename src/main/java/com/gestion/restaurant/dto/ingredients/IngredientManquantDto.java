package com.gestion.restaurant.dto.ingredients;

import java.math.BigDecimal;

/** Quantités nécessaires, disponibles et manquantes pour un ingrédient. */
public record IngredientManquantDto(Long id, String nom, String unite,
                                    BigDecimal quantiteRequise,
                                    BigDecimal quantiteActuelle,
                                    BigDecimal quantiteManquante) { }
