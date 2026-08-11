package com.gestion.restaurant.repository.materielles;

import com.gestion.restaurant.entity.materielles.EtatStockMaterielles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtatStockMateriellesRepository extends JpaRepository<EtatStockMaterielles, Long> {
    List<EtatStockMaterielles> findByMateriel_IdOrderByDateEtatStockDesc(Long idMateriel);
    Optional<EtatStockMaterielles> findTopByMateriel_IdOrderByDateEtatStockDescIdDesc(Long idMateriel);
    List<EtatStockMaterielles> findByMaterielId(Long idMateriel);
}