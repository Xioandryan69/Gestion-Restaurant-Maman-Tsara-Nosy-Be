package com.gestion.restaurant.repository.plats;

import com.gestion.restaurant.entity.plats.Plats;
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
public interface PlatsRepository extends JpaRepository<Plats, Long>, JpaSpecificationExecutor<Plats> {

    @EntityGraph(attributePaths = {"categoriePlats"})
    Page<Plats> findAll(Specification<Plats> spec, Pageable pageable);

    @Query("SELECT p FROM Plats p LEFT JOIN FETCH p.categoriePlats WHERE p.id = :id")
    Optional<Plats> findByIdWithCategorie(@Param("id") Long id);
}