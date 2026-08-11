package com.gestion.restaurant.repository.clients;

import com.gestion.restaurant.entity.clients.TypeClient;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeClientRepository extends JpaRepository<TypeClient, Long> {
    Optional<TypeClient> findByLibelle(String libelle);
}
