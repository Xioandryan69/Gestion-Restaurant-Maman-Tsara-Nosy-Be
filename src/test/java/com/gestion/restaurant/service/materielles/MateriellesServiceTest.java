package com.gestion.restaurant.service.materielles;

import com.gestion.restaurant.dto.materielles.MaterielRequestDto;
import com.gestion.restaurant.entity.materielles.*;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.materielles.*;
import com.gestion.restaurant.service.caisse.CaisseService;
import com.gestion.restaurant.service.fournisseurs.FournisseursService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MateriellesServiceTest {

    @Mock MateriellesRepository materiellesRepository;
    @Mock CategorieMateriellesRepository categorieMateriellesRepository;
    @Mock StatutMateriellesRepository statutMateriellesRepository;
    @Mock HistoriqueMateriellesRepository historiqueMateriellesRepository;
    @Mock MaintenanceMateriellesRepository maintenanceMateriellesRepository;
    @Mock InventairesMateriellesRepository inventairesMateriellesRepository;
    @Mock EtatStockMateriellesRepository etatStockMateriellesRepository;
    @Mock TypeMvtMateriellesRepository typeMvtMateriellesRepository;
    @Mock CaisseService caisseService;
    @Mock FournisseursService fournisseursService;
    @InjectMocks MateriellesService service;

    @Test
    void saveFromDto_nomObligatoire() {
        assertThatThrownBy(() -> service.saveFromDto(new MaterielRequestDto()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("nom");
    }

    @Test
    void enregistrerAchat_majStockEtCaisse() {
        Materielles mat = new Materielles();
        mat.setId(1L);
        when(materiellesRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(mat));
        when(historiqueMateriellesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(etatStockMateriellesRepository.findTopByMateriel_IdOrderByDateEtatStockDescIdDesc(1L))
                .thenReturn(Optional.of(etat(new BigDecimal("2"))));
        when(etatStockMateriellesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LocalDate date = LocalDate.now();
        service.enregistrerAchat(1L, date, new BigDecimal("3"), new BigDecimal("1000"), null);

        verify(caisseService).enregistrerSortie(eq(new BigDecimal("3000")), eq(date));
        verify(etatStockMateriellesRepository).save(argThat(e -> e.getQuantite().compareTo(new BigDecimal("5")) == 0));
    }

    @Test
    void enregistrerMaintenance_sortieCaisseEtStatut() {
        Materielles mat = new Materielles();
        mat.setId(1L);
        StatutMaterielles statut = new StatutMaterielles();
        statut.setLibelle(MateriellesService.STATUT_EN_MAINTENANCE);
        when(materiellesRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(mat));
        when(maintenanceMateriellesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(statutMateriellesRepository.findByLibelle(MateriellesService.STATUT_EN_MAINTENANCE))
                .thenReturn(Optional.of(statut));

        LocalDate date = LocalDate.now();
        service.enregistrerMaintenance(1L, date, "Panne", new BigDecimal("50000"), "Tech");

        assertThat(mat.getStatutMaterielles()).isEqualTo(statut);
        verify(caisseService).enregistrerSortie(eq(new BigDecimal("50000")), eq(date));
    }

    @Test
    void mettreHorsService() {
        Materielles mat = new Materielles();
        mat.setId(1L);
        StatutMaterielles statut = new StatutMaterielles();
        statut.setLibelle(MateriellesService.STATUT_HORS_SERVICE);
        when(materiellesRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(mat));
        when(statutMateriellesRepository.findByLibelle(MateriellesService.STATUT_HORS_SERVICE))
                .thenReturn(Optional.of(statut));

        service.mettreHorsService(1L);
        assertThat(mat.getStatutMaterielles().getLibelle()).isEqualTo("Hors Service");
    }

    @Test
    void deleteById_introuvable() {
        when(materiellesRepository.existsById(3L)).thenReturn(false);
        assertThatThrownBy(() -> service.deleteById(3L)).isInstanceOf(ResourceNotFoundException.class);
    }

    private static EtatStockMaterielles etat(BigDecimal qty) {
        EtatStockMaterielles e = new EtatStockMaterielles();
        e.setQuantite(qty);
        return e;
    }
}
