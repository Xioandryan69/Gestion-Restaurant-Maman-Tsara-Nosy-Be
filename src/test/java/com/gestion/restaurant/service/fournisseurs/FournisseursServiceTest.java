package com.gestion.restaurant.service.fournisseurs;

import com.gestion.restaurant.dto.fournisseurs.FournisseurRequestDto;
import com.gestion.restaurant.entity.fournisseurs.Fournisseurs;
import com.gestion.restaurant.entity.fournisseurs.TypeFournisseurs;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.fournisseur.FournisseursRepository;
import com.gestion.restaurant.repository.fournisseur.TypeFournisseursRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FournisseursServiceTest {

    @Mock FournisseursRepository fournisseursRepository;
    @Mock TypeFournisseursRepository typeFournisseursRepository;
    @InjectMocks FournisseursService service;

    @Test
    void saveFromDto_typeObligatoire() {
        FournisseurRequestDto dto = new FournisseurRequestDto();
        dto.setNom("ABC");
        assertThatThrownBy(() -> service.saveFromDto(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("type");
    }

    @Test
    void saveFromDto_ok() {
        TypeFournisseurs type = new TypeFournisseurs();
        type.setId(1L);
        when(typeFournisseursRepository.findById(1L)).thenReturn(Optional.of(type));
        when(fournisseursRepository.save(any())).thenAnswer(inv -> {
            Fournisseurs f = inv.getArgument(0);
            f.setId(2L);
            return f;
        });

        FournisseurRequestDto dto = new FournisseurRequestDto();
        dto.setNom("ABC");
        dto.setPrenom("X");
        dto.setContact("033");
        dto.setIdTypeFournisseur(1L);

        assertThat(service.saveFromDto(dto).getNom()).isEqualTo("ABC");
    }

    @Test
    void findById_introuvable() {
        when(fournisseursRepository.findByIdWithType(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(9L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
