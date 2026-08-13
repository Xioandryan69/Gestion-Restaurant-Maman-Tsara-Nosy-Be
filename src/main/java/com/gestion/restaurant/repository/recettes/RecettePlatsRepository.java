package com.gestion.restaurant.repository.recettes;

import com.gestion.restaurant.entity.plats.RecettePlats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecettePlatsRepository extends JpaRepository<RecettePlats, Long> {

    @Query("SELECT DISTINCT r FROM RecettePlats r "
            + "LEFT JOIN FETCH r.ingredient i "
            + "LEFT JOIN FETCH i.unite "
            + "WHERE r.plat.id = :idPlat")
    List<RecettePlats> findByPlatIdWithIngredient(@Param("idPlat") Long idPlat);

    List<RecettePlats> findByPlatId(Long idPlat);

    List<RecettePlats> findByIngredientId(Long idIngredient);

    // Ajout : évite les doublons plat+ingrédient lors de l'import Excel.
    Optional<RecettePlats> findByPlatIdAndIngredientId(Long idPlat, Long idIngredient);

    void deleteByPlatId(Long idPlat);

    void deleteByIngredientId(Long idIngredient);
}