package com.gestion.restaurant.repository.materielles;

import com.gestion.restaurant.entity.materielles.InventairesMaterielles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventairesMateriellesRepository extends JpaRepository<InventairesMaterielles, Long> {
    List<InventairesMaterielles> findByMateriel_IdOrderByDateInventaireDesc(Long idMateriel);
    List<InventairesMaterielles> findByMaterielId(Long idMateriel);
}