package com.gestion.restaurant.repository.livraisons;

import com.gestion.restaurant.entity.livraisons.ZonesLivraison;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneLivraisonRepository extends JpaRepository<ZonesLivraison, Long>, JpaSpecificationExecutor<ZonesLivraison> {
    boolean existsByLibelleIgnoreCase(String libelle);
    boolean existsByLibelleIgnoreCaseAndIdNot(String libelle, Long id);
    Optional<ZonesLivraison> findByLibelle(String libelle);
}