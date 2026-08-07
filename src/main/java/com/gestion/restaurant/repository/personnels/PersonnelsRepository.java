package com.gestion.restaurant.repository.personnels;

import com.gestion.restaurant.entity.personnels.Personnels;
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
public interface PersonnelsRepository extends JpaRepository<Personnels, Long>, JpaSpecificationExecutor<Personnels> {

    @EntityGraph(attributePaths = {"rolePersonnels"})
    Page<Personnels> findAll(Specification<Personnels> spec, Pageable pageable);

    @Query("SELECT p FROM Personnels p LEFT JOIN FETCH p.rolePersonnels WHERE p.id = :id")
    Optional<Personnels> findByIdWithRole(@Param("id") Long id);
}
