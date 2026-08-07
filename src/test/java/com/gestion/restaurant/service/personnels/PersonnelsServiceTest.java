package com.gestion.restaurant.service.personnels;

import com.gestion.restaurant.dto.personnels.PersonnelRequestDto;
import com.gestion.restaurant.entity.personnels.*;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.personnels.*;
import com.gestion.restaurant.service.caisse.CaisseService;
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
class PersonnelsServiceTest {

    @Mock PersonnelsRepository personnelsRepository;
    @Mock RolePersonnelsRepository roleRepo;
    @Mock FichePaieRepository fichePaieRepo;
    @Mock AbsencePersonnelsRepository absenceRepo;
    @Mock RaisonAbsenceRepository raisonAbsenceRepo;
    @Mock CaisseService caisseService;
    @InjectMocks PersonnelsService service;

    @Test
    void genererFichePaie_ok_sortieCaisse() {
        Personnels p = new Personnels();
        p.setId(1L);
        when(personnelsRepository.findByIdWithRole(1L)).thenReturn(Optional.of(p));
        when(fichePaieRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LocalDate date = LocalDate.of(2026, 1, 31);
        FichePaie fp = service.genererFichePaie(1L, new BigDecimal("300000"), new BigDecimal("20000"), date);

        assertThat(fp.getMontantTotal()).isEqualByComparingTo("320000");
        verify(caisseService).enregistrerSortie(eq(new BigDecimal("320000")), eq(date));
    }

    @Test
    void genererFichePaie_salaireInvalide() {
        when(personnelsRepository.findByIdWithRole(1L)).thenReturn(Optional.of(new Personnels()));
        assertThatThrownBy(() -> service.genererFichePaie(1L, BigDecimal.ZERO, null, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("salaire");
    }

    @Test
    void enregistrerAbsence_datesInvalides() {
        when(personnelsRepository.findByIdWithRole(1L)).thenReturn(Optional.of(new Personnels()));
        assertThatThrownBy(() -> service.enregistrerAbsence(1L,
                LocalDate.of(2026, 2, 10), LocalDate.of(2026, 2, 1), 1L, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("fin");
    }

    @Test
    void enregistrerAbsence_ok() {
        Personnels p = new Personnels();
        p.setId(1L);
        RaisonAbsence r = new RaisonAbsence();
        r.setId(2L);
        when(personnelsRepository.findByIdWithRole(1L)).thenReturn(Optional.of(p));
        when(raisonAbsenceRepo.findById(2L)).thenReturn(Optional.of(r));
        when(absenceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AbsencePersonnels abs = service.enregistrerAbsence(1L,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 3), 2L, "grippe");
        assertThat(abs.getCommentaire()).isEqualTo("grippe");
    }

    @Test
    void saveFromDto_roleObligatoire() {
        PersonnelRequestDto dto = new PersonnelRequestDto();
        dto.setNom("Jean");
        assertThatThrownBy(() -> service.saveFromDto(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("rôle");
    }

    @Test
    void saveFromDto_ok() {
        RolePersonnels role = new RolePersonnels();
        role.setId(1L);
        when(roleRepo.findById(1L)).thenReturn(Optional.of(role));
        when(personnelsRepository.save(any())).thenAnswer(inv -> {
            Personnels p = inv.getArgument(0);
            p.setId(5L);
            return p;
        });

        PersonnelRequestDto dto = new PersonnelRequestDto();
        dto.setNom("Marie");
        dto.setPrenom("Lala");
        dto.setIdRole(1L);

        assertThat(service.saveFromDto(dto).getNom()).isEqualTo("Marie");
    }

    @Test
    void deleteById_introuvable() {
        when(personnelsRepository.existsById(8L)).thenReturn(false);
        assertThatThrownBy(() -> service.deleteById(8L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
