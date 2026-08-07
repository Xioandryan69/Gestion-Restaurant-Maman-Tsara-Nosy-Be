package com.gestion.restaurant.repository.materielles;

import com.gestion.restaurant.dto.materielles.MaintenanceStatDTO;
import com.gestion.restaurant.entity.materielles.Materielles;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MateriellesRepository extends JpaRepository<Materielles, Long>, JpaSpecificationExecutor<Materielles> {

    @Query("SELECT new com.gestion.restaurant.dto.materielles.MaintenanceStatDTO(" +
           "       FUNCTION('TO_CHAR', m.dateMaintenance, 'YYYY-MM'), " +
           "       COUNT(m.id), " +
           "       SUM(m.cout)) " +
           "FROM MaintenanceMaterielles m " +
           "WHERE m.dateMaintenance BETWEEN :dateDebut AND :dateFin " +
           "GROUP BY FUNCTION('TO_CHAR', m.dateMaintenance, 'YYYY-MM') " +
           "ORDER BY FUNCTION('TO_CHAR', m.dateMaintenance, 'YYYY-MM') ASC")
    List<MaintenanceStatDTO> findMaintenanceStatsByPeriod(
            @Param("dateDebut") LocalDate dateDebut, 
            @Param("dateFin") LocalDate dateFin);
}