package com.gestion.restaurant.repository.materielles;

import com.gestion.restaurant.entity.materielles.HistoriqueMaterielles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface HistoriqueMateriellesRepository extends JpaRepository<HistoriqueMaterielles, Long> {
    List<HistoriqueMaterielles> findByMateriel_IdOrderByDateEntreeDesc(Long idMateriel);
    List<HistoriqueMaterielles> findByMaterielId(Long idMateriel);
    List<HistoriqueMaterielles> findByDateEntreeBetween(LocalDate start, LocalDate end);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(h.prixAchat * h.quantite), 0) FROM HistoriqueMaterielles h WHERE h.dateEntree BETWEEN :debut AND :fin")
    BigDecimal sumAchatsBetween(@org.springframework.data.repository.query.Param("debut") LocalDate debut,
                                @org.springframework.data.repository.query.Param("fin") LocalDate fin);
}
