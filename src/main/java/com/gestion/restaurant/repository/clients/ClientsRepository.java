package com.gestion.restaurant.repository.clients;

import com.gestion.restaurant.entity.clients.Clients;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientsRepository extends JpaRepository<Clients, Long>, JpaSpecificationExecutor<Clients> {

    @EntityGraph(attributePaths = {"typeClient"})
    Page<Clients> findAll(Specification<Clients> spec, Pageable pageable);

    @Query("SELECT c FROM Clients c LEFT JOIN FETCH c.typeClient WHERE c.id = :id")
    Optional<Clients> findByIdWithType(@Param("id") Long id);
}
