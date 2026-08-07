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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RecetteService {

    private final RecettePlatsRepository recettePlatsRepository;
    private final PlatsRepository platsRepository;
    private final IngredientsRepository ingredientsRepository;

    public RecetteService(RecettePlatsRepository recettePlatsRepository,
                          PlatsRepository platsRepository,
                          IngredientsRepository ingredientsRepository) {
        this.recettePlatsRepository = recettePlatsRepository;
        this.platsRepository = platsRepository;
        this.ingredientsRepository = ingredientsRepository;
    }

    @Transactional(readOnly = true)
    public List<RecettePlats> getIngredientsParPlat(Long idPlat) {
        return recettePlatsRepository.findByPlatIdWithIngredient(idPlat);
    }

    @Transactional
    public void ajouterIngredientARecette(RecetteRequestDto dto) {
        if (dto.getIdPlat() == null || dto.getIdIngredient() == null) {
            throw new BusinessRuleException("Veuillez sélectionner un plat et un ingrédient.");
        }
        if (dto.getQuantiteRequise() == null || dto.getQuantiteRequise() <= 0) {
            throw new BusinessRuleException("La quantité requise doit être supérieure à 0.");
        }

        Plats plat = platsRepository.findById(dto.getIdPlat())
                .orElseThrow(() -> new ResourceNotFoundException("Plat introuvable"));
        Ingredients ing = ingredientsRepository.findById(dto.getIdIngredient())
                .orElseThrow(() -> new ResourceNotFoundException("Ingrédient introuvable"));

        RecettePlats recette = new RecettePlats();
        recette.setPlat(plat);
        recette.setIngredient(ing);
        recette.setQuantiteRequise(BigDecimal.valueOf(dto.getQuantiteRequise()));

        recettePlatsRepository.save(recette);
    }

    @Transactional
    public void supprimerIngredientDeRecette(Long idRecette) {
        if (!recettePlatsRepository.existsById(idRecette)) {
            throw new ResourceNotFoundException("Ligne de recette introuvable");
        }
        recettePlatsRepository.deleteById(idRecette);
    }
}