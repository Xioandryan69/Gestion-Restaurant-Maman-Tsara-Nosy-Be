package com.gestion.restaurant.repository.commandes;

import com.gestion.restaurant.entity.commandes.Commandes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CommandesRepository extends JpaRepository<Commandes, Long> {

    @EntityGraph(attributePaths = {"client", "zoneLivraison"})
    @Query("SELECT c FROM Commandes c")
    Page<Commandes> findAllWithRelations(Pageable pageable);

    @EntityGraph(attributePaths = {"client", "zoneLivraison"})
    @Query("SELECT c FROM Commandes c WHERE (:id IS NULL OR c.id = :id) "
            + "AND (:client = '' OR LOWER(c.client.nom) LIKE CONCAT('%', :client, '%') "
            + "OR LOWER(c.client.prenom) LIKE CONCAT('%', :client, '%')) "
            + "AND (:idZone IS NULL OR c.zoneLivraison.id = :idZone) "
            + "AND (:montantMin IS NULL OR c.montantTotal >= :montantMin) "
            + "AND (:montantMax IS NULL OR c.montantTotal <= :montantMax) "
            + "AND (:dateDebut IS NULL OR c.dateCommande >= :dateDebut) AND (:dateFin IS NULL OR c.dateCommande <= :dateFin)")
    Page<Commandes> search(@Param("id") Long id, @Param("client") String client, @Param("idZone") Long idZone,
                           @Param("montantMin") java.math.BigDecimal montantMin,
                           @Param("montantMax") java.math.BigDecimal montantMax,
                           @Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin, Pageable pageable);

    @Query("SELECT COALESCE(SUM(c.montantTotal), 0) FROM Commandes c WHERE c.dateCommande BETWEEN :debut AND :fin")
    java.math.BigDecimal sumMontantTotalBetween(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT c FROM Commandes c "
            + "LEFT JOIN FETCH c.client "
            + "LEFT JOIN FETCH c.zoneLivraison "
            + "WHERE c.id = :id")
    Optional<Commandes> findByIdWithRelations(@Param("id") Long id);
}
