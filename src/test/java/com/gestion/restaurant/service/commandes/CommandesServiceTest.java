package com.gestion.restaurant.service.commandes;

import com.gestion.restaurant.dto.commandes.CommandeCreateRequestDto;
import com.gestion.restaurant.dto.commandes.CommandeLigneRequestDto;
import com.gestion.restaurant.entity.clients.Clients;
import com.gestion.restaurant.entity.commandes.Commandes;
import com.gestion.restaurant.entity.commandes.DetailsCommandes;
import com.gestion.restaurant.entity.ingredients.Ingredients;
import com.gestion.restaurant.entity.livraisons.ZonesLivraison;
import com.gestion.restaurant.entity.plats.Plats;
import com.gestion.restaurant.entity.plats.RecettePlats;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.clients.ClientsRepository;
import com.gestion.restaurant.repository.commandes.CommandesRepository;
import com.gestion.restaurant.repository.commandes.DetailsCommandesRepository;
import com.gestion.restaurant.repository.commandes.FacturesCommandesRepository;
import com.gestion.restaurant.repository.livraisons.ZoneLivraisonRepository;
import com.gestion.restaurant.repository.plats.PlatsRepository;
import com.gestion.restaurant.repository.recettes.RecettePlatsRepository;
import com.gestion.restaurant.service.caisse.CaisseService;
import com.gestion.restaurant.service.ingredients.IngredientsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandesServiceTest {

    @Mock CommandesRepository commandesRepository;
    @Mock DetailsCommandesRepository detailsCommandesRepository;
    @Mock FacturesCommandesRepository facturesCommandesRepository;
    @Mock ClientsRepository clientsRepository;
    @Mock ZoneLivraisonRepository zoneLivraisonRepository;
    @Mock PlatsRepository platsRepository;
    @Mock RecettePlatsRepository recettePlatsRepository;
    @Mock CaisseService caisseService;
    @Mock IngredientsService ingredientsService;

    @InjectMocks CommandesService commandesService;

    private Clients client;
    private ZonesLivraison zone;
    private Plats plat;
    private Ingredients ingredient;

    @BeforeEach
    void setUp() {
        client = new Clients();
        client.setId(1L);
        zone = new ZonesLivraison();
        zone.setId(2L);
        zone.setPrix(new BigDecimal("2000"));
        plat = new Plats();
        plat.setId(3L);
        plat.setPrixVente(new BigDecimal("10000"));
        ingredient = new Ingredients();
        ingredient.setId(4L);
    }

    @Test
    void creerCommande_ok_totalFactureCaisseEtStock() {
        when(clientsRepository.findById(1L)).thenReturn(Optional.of(client));
        when(zoneLivraisonRepository.findById(2L)).thenReturn(Optional.of(zone));
        when(platsRepository.findById(3L)).thenReturn(Optional.of(plat));
        when(commandesRepository.save(any())).thenAnswer(inv -> {
            Commandes c = inv.getArgument(0);
            if (c.getId() == null) c.setId(100L);
            return c;
        });
        when(detailsCommandesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(facturesCommandesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RecettePlats rp = new RecettePlats();
        rp.setIngredient(ingredient);
        rp.setQuantiteRequise(new BigDecimal("2"));
        when(recettePlatsRepository.findByPlatId(3L)).thenReturn(List.of(rp));

        CommandeCreateRequestDto dto = dtoBase();
        Commandes result = commandesService.creerCommande(dto);

        // 2000 + 10000 * 2 = 22000
        assertThat(result.getMontantTotal()).isEqualByComparingTo("22000");
        verify(ingredientsService).enregistrerSortieOuPerte(eq(4L), eq(new BigDecimal("4")),
                eq(IngredientsService.MVT_SORTIE_CUISINE), any());
        verify(caisseService).enregistrerEntree(eq(new BigDecimal("22000")), any());
        verify(facturesCommandesRepository).save(any());
    }

    @Test
    void creerCommande_refusModification() {
        CommandeCreateRequestDto dto = dtoBase();
        dto.setId(1L);
        assertThatThrownBy(() -> commandesService.creerCommande(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("modifier");
    }

    @Test
    void creerCommande_clientObligatoire() {
        CommandeCreateRequestDto dto = dtoBase();
        dto.setIdClient(null);
        assertThatThrownBy(() -> commandesService.creerCommande(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("client");
    }

    @Test
    void creerCommande_lignesVides() {
        CommandeCreateRequestDto dto = dtoBase();
        dto.setLignes(List.of());
        assertThatThrownBy(() -> commandesService.creerCommande(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("plat");
    }

    @Test
    void creerCommande_platSansRecette_pasDeSortieStock() {
        when(clientsRepository.findById(1L)).thenReturn(Optional.of(client));
        when(zoneLivraisonRepository.findById(2L)).thenReturn(Optional.of(zone));
        when(platsRepository.findById(3L)).thenReturn(Optional.of(plat));
        when(commandesRepository.save(any())).thenAnswer(inv -> {
            Commandes c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });
        when(detailsCommandesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(facturesCommandesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(recettePlatsRepository.findByPlatId(3L)).thenReturn(List.of());

        commandesService.creerCommande(dtoBase());

        verify(ingredientsService, never()).enregistrerSortieOuPerte(any(), any(), any(), any());
        verify(caisseService).enregistrerEntree(any(), any());
    }

    @Test
    void creerCommande_stockInsuffisant_propage() {
        when(clientsRepository.findById(1L)).thenReturn(Optional.of(client));
        when(zoneLivraisonRepository.findById(2L)).thenReturn(Optional.of(zone));
        when(platsRepository.findById(3L)).thenReturn(Optional.of(plat));
        when(commandesRepository.save(any())).thenAnswer(inv -> {
            Commandes c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });
        when(detailsCommandesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        RecettePlats rp = new RecettePlats();
        rp.setIngredient(ingredient);
        rp.setQuantiteRequise(BigDecimal.ONE);
        when(recettePlatsRepository.findByPlatId(3L)).thenReturn(List.of(rp));
        doThrow(new BusinessRuleException("Stock insuffisant"))
                .when(ingredientsService).enregistrerSortieOuPerte(any(), any(), any(), any());

        assertThatThrownBy(() -> commandesService.creerCommande(dtoBase()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Stock insuffisant");
        verify(caisseService, never()).enregistrerEntree(any(), any());
    }

    @Test
    void deleteById_reintegreEtSortieCaisse() {
        Commandes commande = new Commandes();
        commande.setId(10L);
        commande.setDateCommande(LocalDate.of(2026, 3, 1));
        commande.setMontantTotal(new BigDecimal("22000"));
        when(commandesRepository.findByIdWithRelations(10L)).thenReturn(Optional.of(commande));

        DetailsCommandes detail = new DetailsCommandes();
        detail.setPlat(plat);
        detail.setQuantite(new BigDecimal("2"));
        when(detailsCommandesRepository.findByCommandeIdWithPlat(10L)).thenReturn(List.of(detail));

        RecettePlats rp = new RecettePlats();
        rp.setIngredient(ingredient);
        rp.setQuantiteRequise(new BigDecimal("2"));
        when(recettePlatsRepository.findByPlatId(3L)).thenReturn(List.of(rp));

        commandesService.deleteById(10L);

        verify(ingredientsService).reintegrerStock(eq(4L), eq(new BigDecimal("4")), eq(LocalDate.of(2026, 3, 1)));
        verify(caisseService).enregistrerSortie(eq(new BigDecimal("22000")), eq(LocalDate.of(2026, 3, 1)));
        verify(facturesCommandesRepository).deleteByCommande_Id(10L);
        verify(detailsCommandesRepository).deleteByCommandeId(10L);
        verify(commandesRepository).deleteById(10L);
    }

    @Test
    void findById_introuvable() {
        when(commandesRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> commandesService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private CommandeCreateRequestDto dtoBase() {
        CommandeCreateRequestDto dto = new CommandeCreateRequestDto();
        dto.setIdClient(1L);
        dto.setIdZoneLivraison(2L);
        dto.setDateCommande(LocalDate.of(2026, 1, 15));
        CommandeLigneRequestDto ligne = new CommandeLigneRequestDto();
        ligne.setIdPlat(3L);
        ligne.setQuantite(new BigDecimal("2"));
        dto.setLignes(List.of(ligne));
        return dto;
    }
}
