package com.gestion.restaurant.repository.ingredients;

import com.gestion.restaurant.entity.ingredients.StatutIngredient;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatutIngredientRepository extends JpaRepository<StatutIngredient, Long> {
    Optional<StatutIngredient> findByLibelle(String libelle);
}
