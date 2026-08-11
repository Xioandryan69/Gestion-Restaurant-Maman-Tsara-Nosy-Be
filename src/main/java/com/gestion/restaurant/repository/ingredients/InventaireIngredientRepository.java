package com.gestion.restaurant.repository.ingredients;

import com.gestion.restaurant.entity.ingredients.InventaireIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventaireIngredientRepository extends JpaRepository<InventaireIngredient, Long> {
    List<InventaireIngredient> findByIngredient_IdOrderByDateInventaireDesc(Long idIngredient);
    List<InventaireIngredient> findByIngredientId(Long idIngredient);
}