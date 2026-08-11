package com.gestion.restaurant.repository.materielles;

import com.gestion.restaurant.entity.materielles.Materielles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MateriellesRepository extends JpaRepository<Materielles, Long>, JpaSpecificationExecutor<Materielles> {

    long countByStatutMateriellesLibelleIgnoreCase(String libelle);

    default long countByStatutMaterielles_LibelleIgnoreCase(String libelle) {
        return countByStatutMateriellesLibelleIgnoreCase(libelle);
    }

    @EntityGraph(attributePaths = {"categorieMaterielles", "statutMaterielles"})
    Page<Materielles> findAll(Specification<Materielles> spec, Pageable pageable);

    @Query("SELECT m FROM Materielles m "
            + "LEFT JOIN FETCH m.categorieMaterielles "
            + "LEFT JOIN FETCH m.statutMaterielles "
            + "WHERE m.id = :id")
    Optional<Materielles> findByIdWithRelations(@Param("id") Long id);
}
