package com.gestion.restaurant.repository.commandes;

import com.gestion.restaurant.entity.commandes.Commandes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommandesRepository extends JpaRepository<Commandes, Long> {

    @EntityGraph(attributePaths = {"client", "zoneLivraison"})
    @Query("SELECT c FROM Commandes c")
    Page<Commandes> findAllWithRelations(Pageable pageable);

    @Query("SELECT c FROM Commandes c "
            + "LEFT JOIN FETCH c.client "
            + "LEFT JOIN FETCH c.zoneLivraison "
            + "WHERE c.id = :id")
    Optional<Commandes> findByIdWithRelations(@Param("id") Long id);
}
