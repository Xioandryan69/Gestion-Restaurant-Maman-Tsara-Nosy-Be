package com.gestion.restaurant.repository.ingredients;

import com.gestion.restaurant.entity.ingredients.EtatStockIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtatStockIngredientRepository extends JpaRepository<EtatStockIngredient, Long> {
    Optional<EtatStockIngredient> findTopByIngredient_IdOrderByDateEtatStockDescIdDesc(Long idIngredient);
    Optional<EtatStockIngredient> findByIngredient_Id(Long idIngredient);

    /** Dernier stock par ingrédient (Postgres DISTINCT ON) — évite le N+1 de la page stock. */
    @Query(value = """
            SELECT DISTINCT ON (idingredient) idingredient, quantite
            FROM etatstockingredient
            ORDER BY idingredient, dateetatstock DESC, id DESC
            """, nativeQuery = true)
    List<Object[]> findLatestQuantiteByIngredient();
    List<EtatStockIngredient> findByIngredientId(Long idIngredient);
}