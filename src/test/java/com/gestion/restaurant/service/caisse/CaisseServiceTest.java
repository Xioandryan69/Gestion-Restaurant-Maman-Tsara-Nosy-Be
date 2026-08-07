package com.gestion.restaurant.service.caisse;

import com.gestion.restaurant.dto.caisse.MouvementCaisseRequestDto;
import com.gestion.restaurant.entity.caisse.MouvementCaisse;
import com.gestion.restaurant.entity.caisse.TypeMouvementCaisse;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.caisse.MouvementCaisseRepository;
import com.gestion.restaurant.repository.caisse.TypeMouvementCaisseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaisseServiceTest {

    @Mock MouvementCaisseRepository mouvementCaisseRepository;
    @Mock TypeMouvementCaisseRepository typeMouvementCaisseRepository;
    @InjectMocks CaisseService caisseService;

    @Test
    void enregistrerEntree_creeTypeSiAbsent() {
        when(typeMouvementCaisseRepository.findByLibelle("Entree")).thenReturn(Optional.empty());
        when(typeMouvementCaisseRepository.save(any())).thenAnswer(inv -> {
            TypeMouvementCaisse t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });
        when(mouvementCaisseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MouvementCaisse m = caisseService.enregistrerEntree(new BigDecimal("1000"), LocalDate.of(2026, 1, 1));

        assertThat(m.getMontant()).isEqualByComparingTo("1000");
        assertThat(m.getTypeMouvement().getLibelle()).isEqualTo("Entree");
        verify(typeMouvementCaisseRepository).save(any());
    }

    @Test
    void enregistrerSortie_montantNegatif_rejette() {
        assertThatThrownBy(() -> caisseService.enregistrerSortie(BigDecimal.ZERO, LocalDate.now()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("positif");
    }

    @Test
    void saveFromDto_typeObligatoire() {
        MouvementCaisseRequestDto dto = new MouvementCaisseRequestDto();
        dto.setMontant(new BigDecimal("10"));
        dto.setDateMouvement(LocalDate.now());

        assertThatThrownBy(() -> caisseService.saveFromDto(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("type");
    }

    @Test
    void saveFromDto_ok() {
        TypeMouvementCaisse type = new TypeMouvementCaisse();
        type.setId(2L);
        type.setLibelle("Sortie");
        when(typeMouvementCaisseRepository.findById(2L)).thenReturn(Optional.of(type));
        when(mouvementCaisseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MouvementCaisseRequestDto dto = new MouvementCaisseRequestDto();
        dto.setMontant(new BigDecimal("50"));
        dto.setDateMouvement(LocalDate.of(2026, 2, 1));
        dto.setIdTypeMouvement(2L);

        MouvementCaisse saved = caisseService.saveFromDto(dto);

        ArgumentCaptor<MouvementCaisse> captor = ArgumentCaptor.forClass(MouvementCaisse.class);
        verify(mouvementCaisseRepository).save(captor.capture());
        assertThat(captor.getValue().getTypeMouvement()).isEqualTo(type);
        assertThat(saved.getMontant()).isEqualByComparingTo("50");
    }

    @Test
    void deleteById_introuvable() {
        when(mouvementCaisseRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> caisseService.deleteById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_ok() {
        MouvementCaisse m = new MouvementCaisse();
        m.setId(1L);
        when(mouvementCaisseRepository.findByIdWithType(1L)).thenReturn(Optional.of(m));
        assertThat(caisseService.findById(1L).getId()).isEqualTo(1L);
    }
}
