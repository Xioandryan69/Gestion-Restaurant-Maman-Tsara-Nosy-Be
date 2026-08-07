package com.gestion.restaurant.service.recette;

import com.gestion.restaurant.dto.recette.RecetteRequestDto;
import com.gestion.restaurant.entity.ingredients.Ingredients;
import com.gestion.restaurant.entity.plats.Plats;
import com.gestion.restaurant.entity.plats.RecettePlats;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.ingredients.IngredientsRepository;
import com.gestion.restaurant.repository.plats.PlatsRepository;
import com.gestion.restaurant.repository.recettes.RecettePlatsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecetteServiceTest {

    @Mock RecettePlatsRepository recettePlatsRepository;
    @Mock PlatsRepository platsRepository;
    @Mock IngredientsRepository ingredientsRepository;
    @InjectMocks RecetteService recetteService;

    @Test
    void ajouterIngredient_ok() {
        Plats plat = new Plats();
        plat.setId(1L);
        Ingredients ing = new Ingredients();
        ing.setId(2L);
        when(platsRepository.findById(1L)).thenReturn(Optional.of(plat));
        when(ingredientsRepository.findById(2L)).thenReturn(Optional.of(ing));
        when(recettePlatsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RecetteRequestDto dto = new RecetteRequestDto();
        dto.setIdPlat(1L);
        dto.setIdIngredient(2L);
        dto.setQuantiteRequise(1.5);

        recetteService.ajouterIngredientARecette(dto);

        ArgumentCaptor<RecettePlats> captor = ArgumentCaptor.forClass(RecettePlats.class);
        verify(recettePlatsRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantiteRequise()).isEqualByComparingTo("1.5");
        assertThat(captor.getValue().getPlat()).isEqualTo(plat);
    }

    @Test
    void ajouterIngredient_quantiteInvalide() {
        RecetteRequestDto dto = new RecetteRequestDto();
        dto.setIdPlat(1L);
        dto.setIdIngredient(2L);
        dto.setQuantiteRequise(0.0);

        assertThatThrownBy(() -> recetteService.ajouterIngredientARecette(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("quantité");
    }

    @Test
    void ajouterIngredient_platOuIngredientNull() {
        RecetteRequestDto dto = new RecetteRequestDto();
        dto.setQuantiteRequise(1.0);
        assertThatThrownBy(() -> recetteService.ajouterIngredientARecette(dto))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void supprimer_introuvable() {
        when(recettePlatsRepository.existsById(5L)).thenReturn(false);
        assertThatThrownBy(() -> recetteService.supprimerIngredientDeRecette(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getIngredientsParPlat() {
        when(recettePlatsRepository.findByPlatIdWithIngredient(1L)).thenReturn(List.of(new RecettePlats()));
        assertThat(recetteService.getIngredientsParPlat(1L)).hasSize(1);
    }
}
