package com.gestion.restaurant.repository.fournisseur;

import com.gestion.restaurant.entity.fournisseurs.TypeFournisseurs;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeFournisseursRepository extends JpaRepository<TypeFournisseurs, Long> {
    Optional<TypeFournisseurs> findByLibelle(String libelle);
}
