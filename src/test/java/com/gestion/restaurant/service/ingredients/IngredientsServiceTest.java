package com.gestion.restaurant.service.ingredients;

import com.gestion.restaurant.dto.ingredients.IngredientRequestDto;
import com.gestion.restaurant.entity.fournisseurs.Fournisseurs;
import com.gestion.restaurant.entity.ingredients.*;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.ingredients.*;
import com.gestion.restaurant.service.caisse.CaisseService;
import com.gestion.restaurant.service.fournisseurs.FournisseursService;
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
class IngredientsServiceTest {

    @Mock IngredientsRepository ingredientsRepository;
    @Mock CategorieIngredientsRepository categorieRepo;
    @Mock StatutIngredientRepository statutRepo;
    @Mock HistoriqueIngredientsRepository historiqueRepo;
    @Mock InventaireIngredientRepository inventaireRepo;
    @Mock EtatStockIngredientRepository etatStockRepo;
    @Mock TypeMvtIngredientRepository typeMvtRepo;
    @Mock UniteRepository uniteRepo;
    @Mock CaisseService caisseService;
    @Mock FournisseursService fournisseursService;

    @InjectMocks IngredientsService ingredientsService;

    private Ingredients ingredient;

    @BeforeEach
    void setUp() {
        ingredient = new Ingredients();
        ingredient.setId(1L);
        ingredient.setNom("Poulet");
    }

    @Test
    void getStockActuel_sansEtat_retourneZero() {
        when(etatStockRepo.findTopByIngredient_IdOrderByDateEtatStockDescIdDesc(1L))
                .thenReturn(Optional.empty());
        assertThat(ingredientsService.getStockActuel(1L)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void enregistrerAchatEntree_ok_etCaisse() {
        when(ingredientsRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(ingredient));
        when(historiqueRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(typeMvtRepo.findByLibelleIgnoreCase(anyString())).thenReturn(Optional.of(typeMvt("Entrée")));
        when(inventaireRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(etatStockRepo.findTopByIngredient_IdOrderByDateEtatStockDescIdDesc(1L))
                .thenReturn(Optional.of(etat(new BigDecimal("10"))));
        when(etatStockRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LocalDate today = LocalDate.now();
        ingredientsService.enregistrerAchatEntree(1L, today, today.plusDays(5),
                new BigDecimal("5"), new BigDecimal("1000"));

        verify(caisseService).enregistrerSortie(new BigDecimal("5000"), today);
        verify(etatStockRepo).save(argThat(e -> e.getQuantite().compareTo(new BigDecimal("15")) == 0));
    }

    @Test
    void enregistrerAchatEntree_peremptionAvantEntree() {
        when(ingredientsRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(ingredient));
        assertThatThrownBy(() -> ingredientsService.enregistrerAchatEntree(
                1L, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1),
                new BigDecimal("1"), BigDecimal.ZERO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("péremption");
    }

    @Test
    void enregistrerSortie_stockInsuffisant() {
        when(ingredientsRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(ingredient));
        when(etatStockRepo.findTopByIngredient_IdOrderByDateEtatStockDescIdDesc(1L))
                .thenReturn(Optional.of(etat(new BigDecimal("2"))));

        assertThatThrownBy(() -> ingredientsService.enregistrerSortieOuPerte(
                1L, new BigDecimal("5"), IngredientsService.MVT_SORTIE_CUISINE, LocalDate.now()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Stock insuffisant");
    }

    @Test
    void enregistrerSortie_ok() {
        when(ingredientsRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(ingredient));
        when(etatStockRepo.findTopByIngredient_IdOrderByDateEtatStockDescIdDesc(1L))
                .thenReturn(Optional.of(etat(new BigDecimal("10"))));
        when(typeMvtRepo.findByLibelleIgnoreCase(anyString())).thenReturn(Optional.of(typeMvt("Sortie")));
        when(inventaireRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(etatStockRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ingredientsService.enregistrerSortieOuPerte(1L, new BigDecimal("4"),
                IngredientsService.MVT_SORTIE_CUISINE, LocalDate.now());

        verify(etatStockRepo).save(argThat(e -> e.getQuantite().compareTo(new BigDecimal("6")) == 0));
        verifyNoInteractions(caisseService);
    }

    @Test
    void reintegrerStock_sansCaisse() {
        when(ingredientsRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(ingredient));
        when(typeMvtRepo.findByLibelleIgnoreCase(anyString())).thenReturn(Optional.of(typeMvt("Entrée")));
        when(inventaireRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(etatStockRepo.findTopByIngredient_IdOrderByDateEtatStockDescIdDesc(1L))
                .thenReturn(Optional.of(etat(new BigDecimal("3"))));
        when(etatStockRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ingredientsService.reintegrerStock(1L, new BigDecimal("2"), LocalDate.now());

        verify(etatStockRepo).save(argThat(e -> e.getQuantite().compareTo(new BigDecimal("5")) == 0));
        verifyNoInteractions(caisseService);
    }

    @Test
    void getGlobalStockState_alerteSeuil() {
        when(ingredientsRepository.count()).thenReturn(1L);
        when(ingredientsRepository.findAllWithRelations(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(ingredient)));
        when(etatStockRepo.findLatestQuantiteByIngredient())
                .thenReturn(java.util.Collections.singletonList(new Object[]{1L, 3.0}));

        var stocks = ingredientsService.getGlobalStockState(org.springframework.data.domain.Pageable.unpaged());
        assertThat(stocks.page().getContent()).hasSize(1);
        assertThat(stocks.page().getContent().getFirst().getQuantiteActuelle())
                .isLessThan(IngredientsService.SEUIL_STOCK_FAIBLE);
        assertThat(stocks.nombreAlerteStock()).isEqualTo(1);
    }

    @Test
    void saveFromDto_nomObligatoire() {
        IngredientRequestDto dto = new IngredientRequestDto();
        assertThatThrownBy(() -> ingredientsService.saveFromDto(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("nom");
    }

    @Test
    void saveFromDto_ok() {
        IngredientRequestDto dto = new IngredientRequestDto();
        dto.setNom("Tomate");
        dto.setIdCategorie(1L);
        dto.setIdStatut(1L);
        dto.setIdFournisseur(1L);
        dto.setIdUnite(1L);

        CategorieIngredients cat = new CategorieIngredients();
        cat.setId(1L);
        StatutIngredient statut = new StatutIngredient();
        statut.setId(1L);
        Unite unite = new Unite();
        unite.setId(1L);
        Fournisseurs f = new Fournisseurs();
        f.setId(1L);

        when(categorieRepo.findById(1L)).thenReturn(Optional.of(cat));
        when(statutRepo.findById(1L)).thenReturn(Optional.of(statut));
        when(uniteRepo.findById(1L)).thenReturn(Optional.of(unite));
        when(fournisseursService.findById(1L)).thenReturn(f);
        when(ingredientsRepository.save(any())).thenAnswer(inv -> {
            Ingredients i = inv.getArgument(0);
            i.setId(10L);
            return i;
        });

        assertThat(ingredientsService.saveFromDto(dto).getNom()).isEqualTo("Tomate");
    }

    @Test
    void deleteById_introuvable() {
        when(ingredientsRepository.existsById(9L)).thenReturn(false);
        assertThatThrownBy(() -> ingredientsService.deleteById(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static EtatStockIngredient etat(BigDecimal qty) {
        EtatStockIngredient e = new EtatStockIngredient();
        e.setQuantite(qty);
        return e;
    }

    private static TypeMvtIngredient typeMvt(String libelle) {
        TypeMvtIngredient t = new TypeMvtIngredient();
        t.setLibelle(libelle);
        return t;
    }
}
