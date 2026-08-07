package com.gestion.restaurant.service.plats;

import com.gestion.restaurant.dto.plats.PlatMultipleRequestDto;
import com.gestion.restaurant.entity.ingredients.Ingredients;
import com.gestion.restaurant.entity.plats.CategoriePlats;
import com.gestion.restaurant.entity.plats.Plats;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.ingredients.IngredientsRepository;
import com.gestion.restaurant.repository.plats.CategoriePlatsRepository;
import com.gestion.restaurant.repository.plats.PlatsRepository;
import com.gestion.restaurant.repository.recettes.RecettePlatsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatsServiceTest {

    @Mock PlatsRepository platsRepository;
    @Mock CategoriePlatsRepository categoriePlatsRepository;
    @Mock RecettePlatsRepository recettePlatsRepository;
    @Mock IngredientsRepository ingredientsRepository;
    @InjectMocks PlatsService service;

    @Test
    void saveMultiplePlats_avecRecette() {
        CategoriePlats cat = new CategoriePlats();
        cat.setId(1L);
        Ingredients ing = new Ingredients();
        ing.setId(2L);
        when(categoriePlatsRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(ingredientsRepository.findById(2L)).thenReturn(Optional.of(ing));
        when(platsRepository.save(any())).thenAnswer(inv -> {
            Plats p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });
        when(recettePlatsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PlatMultipleRequestDto.IngredientQuantiteDto ingDto = new PlatMultipleRequestDto.IngredientQuantiteDto();
        ingDto.setIdIngredient(2L);
        ingDto.setQuantiteRequise(new BigDecimal("1.5"));

        PlatMultipleRequestDto.PlatFormItem item = new PlatMultipleRequestDto.PlatFormItem();
        item.setNom("Ravitoto");
        item.setIdCategorie(1L);
        item.setPrixVente(new BigDecimal("12000"));
        item.setIngredients(List.of(ingDto));

        PlatMultipleRequestDto dto = new PlatMultipleRequestDto();
        dto.setPlats(List.of(item));

        service.saveMultiplePlats(dto);

        verify(platsRepository).save(any());
        verify(recettePlatsRepository).save(any());
    }

    @Test
    void findById_introuvable() {
        when(platsRepository.findByIdWithCategorie(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteById() {
        service.deleteById(5L);
        verify(platsRepository).deleteById(5L);
    }
}
