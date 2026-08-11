package com.gestion.restaurant.repository.ingredients;

import com.gestion.restaurant.entity.ingredients.TypeMvtIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeMvtIngredientRepository extends JpaRepository<TypeMvtIngredient, Long> {
    Optional<TypeMvtIngredient> findByLibelle(String libelle);

    Optional<TypeMvtIngredient> findByLibelleIgnoreCase(String libelle);
    
}