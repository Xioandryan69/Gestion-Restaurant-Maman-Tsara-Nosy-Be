package com.gestion.restaurant.repository.materielles;

import com.gestion.restaurant.entity.materielles.MaintenanceMaterielles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenanceMateriellesRepository extends JpaRepository<MaintenanceMaterielles, Long> {
    List<MaintenanceMaterielles> findByMateriel_IdOrderByDateMaintenanceDesc(Long idMateriel);
    List<MaintenanceMaterielles> findByMaterielId(Long idMateriel);
    List<MaintenanceMaterielles> findByDateMaintenanceBetween(LocalDate start, LocalDate end);
}