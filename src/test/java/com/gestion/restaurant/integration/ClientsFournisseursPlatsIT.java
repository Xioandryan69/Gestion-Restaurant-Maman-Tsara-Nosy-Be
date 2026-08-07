package com.gestion.restaurant.integration;

import com.gestion.restaurant.dto.clients.ClientRequestDto;
import com.gestion.restaurant.dto.fournisseurs.FournisseurRequestDto;
import com.gestion.restaurant.dto.plats.PlatMultipleRequestDto;
import com.gestion.restaurant.entity.clients.TypeClient;
import com.gestion.restaurant.entity.fournisseurs.TypeFournisseurs;
import com.gestion.restaurant.entity.ingredients.Ingredients;
import com.gestion.restaurant.entity.plats.CategoriePlats;
import com.gestion.restaurant.service.clients.ClientsService;
import com.gestion.restaurant.service.fournisseurs.FournisseursService;
import com.gestion.restaurant.service.plats.PlatsService;
import com.gestion.restaurant.support.AbstractPostgresIT;
import com.gestion.restaurant.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ClientsFournisseursPlatsIT extends AbstractPostgresIT {

    @Autowired ClientsService clientsService;
    @Autowired FournisseursService fournisseursService;
    @Autowired PlatsService platsService;
    @Autowired TestDataFactory factory;

    @Test
    void persistClientsFournisseursPlats() {
        factory.ensureLookups();
        TypeClient typeClient = factory.typeClient("VIP");
        ClientRequestDto clientDto = new ClientRequestDto();
        clientDto.setNom("ClientIT");
        clientDto.setPrenom("Test");
        clientDto.setContact("03211");
        clientDto.setIdTypeClient(typeClient.getId());
        Long clientId = clientsService.saveFromDto(clientDto).getId();
        assertThat(clientsService.findById(clientId).getNom()).isEqualTo("ClientIT");

        TypeFournisseurs typeF = factory.typeFournisseur("Grossiste");
        FournisseurRequestDto fDto = new FournisseurRequestDto();
        fDto.setNom("FourIT");
        fDto.setPrenom("X");
        fDto.setContact("03311");
        fDto.setIdTypeFournisseur(typeF.getId());
        Long fId = fournisseursService.saveFromDto(fDto).getId();
        assertThat(fournisseursService.findById(fId).getNom()).isEqualTo("FourIT");

        var unite = factory.unite("pcs-" + System.nanoTime(), "pcs");
        var catI = factory.categorieIngredient("Divers");
        var st = factory.statutIngredient("Actif");
        Ingredients ing = factory.ingredient("Oignon", catI, st, fournisseursService.findById(fId), unite);
        CategoriePlats catP = factory.categoriePlat("Entrée");

        PlatMultipleRequestDto.IngredientQuantiteDto iq = new PlatMultipleRequestDto.IngredientQuantiteDto();
        iq.setIdIngredient(ing.getId());
        iq.setQuantiteRequise(new BigDecimal("1"));
        PlatMultipleRequestDto.PlatFormItem item = new PlatMultipleRequestDto.PlatFormItem();
        item.setNom("Salade");
        item.setIdCategorie(catP.getId());
        item.setPrixVente(new BigDecimal("8000"));
        item.setIngredients(List.of(iq));
        PlatMultipleRequestDto dto = new PlatMultipleRequestDto();
        dto.setPlats(List.of(item));
        platsService.saveMultiplePlats(dto);

        assertThat(platsService.search(
                        new com.gestion.restaurant.dto.plats.PlatSearchCriteria(),
                        org.springframework.data.domain.Pageable.unpaged()).getContent())
                .anyMatch(p -> "Salade".equals(p.getNom()));
        assertThat(clientId).isNotNull();
        assertThat(fId).isNotNull();
    }
}
