package com.gestion.restaurant.repository.caisse;

import com.gestion.restaurant.entity.caisse.MouvementCaisse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MouvementCaisseRepository extends JpaRepository<MouvementCaisse, Long> {

    @EntityGraph(attributePaths = {"typeMouvement"})
    @Query("SELECT m FROM MouvementCaisse m")
    Page<MouvementCaisse> findAllWithType(Pageable pageable);

    @Query("SELECT m FROM MouvementCaisse m LEFT JOIN FETCH m.typeMouvement WHERE m.id = :id")
    Optional<MouvementCaisse> findByIdWithType(@Param("id") Long id);
    List<MouvementCaisse> findByDateMouvementBetween(LocalDate start, LocalDate end);

    @Query("SELECT m FROM MouvementCaisse m WHERE YEAR(m.dateMouvement) = :year")
    List<MouvementCaisse> findByYear(@Param("year") int year);
}
