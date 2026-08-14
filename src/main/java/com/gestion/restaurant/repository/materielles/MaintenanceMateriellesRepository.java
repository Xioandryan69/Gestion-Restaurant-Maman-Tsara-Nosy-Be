package com.gestion.restaurant.repository.materielles;

import com.gestion.restaurant.entity.materielles.MaintenanceMaterielles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MaintenanceMateriellesRepository extends JpaRepository<MaintenanceMaterielles, Long> {
    List<MaintenanceMaterielles> findByMateriel_IdOrderByDateMaintenanceDesc(Long idMateriel);
    List<MaintenanceMaterielles> findByMaterielId(Long idMateriel);
    List<MaintenanceMaterielles> findByDateMaintenanceBetween(LocalDate start, LocalDate end);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(m.cout), 0) FROM MaintenanceMaterielles m WHERE m.dateMaintenance BETWEEN :debut AND :fin")
    BigDecimal sumCoutBetween(@org.springframework.data.repository.query.Param("debut") LocalDate debut,
                              @org.springframework.data.repository.query.Param("fin") LocalDate fin);
}
