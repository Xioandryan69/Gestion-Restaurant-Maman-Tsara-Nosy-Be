package com.gestion.restaurant.repository.ingredients;

import com.gestion.restaurant.entity.ingredients.CategorieIngredients;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategorieIngredientsRepository extends JpaRepository<CategorieIngredients, Long> {
    Optional<CategorieIngredients> findByLibelle(String libelle);
}
