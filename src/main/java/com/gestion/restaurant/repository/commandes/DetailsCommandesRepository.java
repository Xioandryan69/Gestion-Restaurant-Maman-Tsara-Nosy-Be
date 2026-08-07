package com.gestion.restaurant.repository.commandes;

import com.gestion.restaurant.entity.commandes.DetailsCommandes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailsCommandesRepository extends JpaRepository<DetailsCommandes, Long> {

    @Query("SELECT d FROM DetailsCommandes d LEFT JOIN FETCH d.plat WHERE d.commande.id = :commandeId")
    List<DetailsCommandes> findByCommandeIdWithPlat(@Param("commandeId") Long commandeId);

    List<DetailsCommandes> findByCommandeId(Long commandeId);

    void deleteByCommandeId(Long commandeId);
}
