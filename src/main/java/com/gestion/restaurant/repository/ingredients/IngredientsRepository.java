package com.gestion.restaurant.repository.ingredients;

import com.gestion.restaurant.entity.ingredients.Ingredients;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientsRepository extends JpaRepository<Ingredients, Long>, JpaSpecificationExecutor<Ingredients> {

    @EntityGraph(attributePaths = {"categorieIngredients", "statutIngredient", "fournisseur", "unite"})
    Page<Ingredients> findAll(Specification<Ingredients> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"categorieIngredients", "statutIngredient", "fournisseur", "unite"})
    @Query("SELECT i FROM Ingredients i")
    Page<Ingredients> findAllWithRelations(Pageable pageable);

    /** Listes déroulantes (formulaires) — volume attendu faible. */
    @Query("SELECT DISTINCT i FROM Ingredients i "
            + "LEFT JOIN FETCH i.categorieIngredients "
            + "LEFT JOIN FETCH i.statutIngredient "
            + "LEFT JOIN FETCH i.fournisseur "
            + "LEFT JOIN FETCH i.unite "
            + "ORDER BY i.nom")
    List<Ingredients> findAllWithRelationsList();

    @Query("SELECT i FROM Ingredients i "
            + "LEFT JOIN FETCH i.categorieIngredients "
            + "LEFT JOIN FETCH i.statutIngredient "
            + "LEFT JOIN FETCH i.fournisseur "
            + "LEFT JOIN FETCH i.unite "
            + "WHERE i.id = :id")
    Optional<Ingredients> findByIdWithRelations(@Param("id") Long id);
}
