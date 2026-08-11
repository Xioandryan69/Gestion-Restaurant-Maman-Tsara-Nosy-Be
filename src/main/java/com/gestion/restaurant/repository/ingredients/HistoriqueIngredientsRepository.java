package com.gestion.restaurant.repository.ingredients;

import com.gestion.restaurant.entity.ingredients.HistoriqueIngredients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HistoriqueIngredientsRepository extends JpaRepository<HistoriqueIngredients, Long> {
    List<HistoriqueIngredients> findByIngredient_IdOrderByDateEntreeDesc(Long idIngredient);

    @Query("SELECT h FROM HistoriqueIngredients h WHERE h.datePeremption IS NOT NULL AND h.datePeremption <= :date")
    List<HistoriqueIngredients> findLotsPerimes(@Param("date") LocalDate date);
    List<HistoriqueIngredients> findByIngredientId(Long idIngredient);
}