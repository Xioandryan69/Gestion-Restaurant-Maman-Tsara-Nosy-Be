package com.gestion.restaurant.integration;

import com.gestion.restaurant.dto.caisse.MouvementCaisseRequestDto;
import com.gestion.restaurant.entity.caisse.MouvementCaisse;
import com.gestion.restaurant.entity.caisse.TypeMouvementCaisse;
import com.gestion.restaurant.service.caisse.CaisseService;
import com.gestion.restaurant.support.AbstractPostgresIT;
import com.gestion.restaurant.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CaisseIT extends AbstractPostgresIT {

    @Autowired CaisseService caisseService;
    @Autowired TestDataFactory factory;

    @Test
    void entreeSortieEtSaisieManuelle() {
        factory.ensureLookups();
        MouvementCaisse entree = caisseService.enregistrerEntree(new BigDecimal("10000"), LocalDate.now());
        MouvementCaisse sortie = caisseService.enregistrerSortie(new BigDecimal("2500"), LocalDate.now());
        assertThat(entree.getTypeMouvement().getLibelle()).isEqualTo("Entree");
        assertThat(sortie.getTypeMouvement().getLibelle()).isEqualTo("Sortie");

        TypeMouvementCaisse type = caisseService.findAllTypes().stream()
                .filter(t -> "Entree".equals(t.getLibelle()))
                .findFirst().orElseThrow();

        MouvementCaisseRequestDto dto = new MouvementCaisseRequestDto();
        dto.setDateMouvement(LocalDate.now());
        dto.setMontant(new BigDecimal("100"));
        dto.setIdTypeMouvement(type.getId());
        MouvementCaisse manuel = caisseService.saveFromDto(dto);
        assertThat(manuel.getId()).isNotNull();
        assertThat(caisseService.findAll(org.springframework.data.domain.Pageable.unpaged()).getTotalElements())
                .isGreaterThanOrEqualTo(3);
    }
}
