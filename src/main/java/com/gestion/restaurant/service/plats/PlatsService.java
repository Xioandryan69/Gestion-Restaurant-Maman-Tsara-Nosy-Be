package com.gestion.restaurant.service.plats;

import com.gestion.restaurant.dto.plats.PlatMultipleRequestDto;
import com.gestion.restaurant.dto.plats.PlatSearchCriteria;
import com.gestion.restaurant.entity.ingredients.Ingredients;
import com.gestion.restaurant.entity.plats.CategoriePlats;
import com.gestion.restaurant.entity.plats.Plats;
import com.gestion.restaurant.entity.plats.RecettePlats;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.ingredients.IngredientsRepository;
import com.gestion.restaurant.repository.plats.CategoriePlatsRepository;
import com.gestion.restaurant.repository.plats.PlatsRepository;
import com.gestion.restaurant.repository.recettes.RecettePlatsRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlatsService {

    private final PlatsRepository platsRepository;
    private final CategoriePlatsRepository categoriePlatsRepository;
    private final RecettePlatsRepository recettePlatsRepository;
    private final IngredientsRepository ingredientsRepository;

    public PlatsService(PlatsRepository platsRepository,
                        CategoriePlatsRepository categoriePlatsRepository,
                        RecettePlatsRepository recettePlatsRepository,
                        IngredientsRepository ingredientsRepository) {
        this.platsRepository = platsRepository;
        this.categoriePlatsRepository = categoriePlatsRepository;
        this.recettePlatsRepository = recettePlatsRepository;
        this.ingredientsRepository = ingredientsRepository;
    }

    @Transactional(readOnly = true)
    public Page<Plats> search(PlatSearchCriteria criteria, Pageable pageable) {
        Specification<Plats> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria != null) {
                if (criteria.getNom() != null && !criteria.getNom().trim().isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("nom")), "%" + criteria.getNom().toLowerCase() + "%"));
                }
                if (criteria.getIdCategorie() != null) {
                    predicates.add(cb.equal(root.get("categoriePlats").get("id"), criteria.getIdCategorie()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return platsRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Plats findById(Long id) {
        return platsRepository.findByIdWithCategorie(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé avec l'ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<CategoriePlats> findAllCategories() {
        return categoriePlatsRepository.findAll();
    }

    @Transactional
    public void saveMultiplePlats(PlatMultipleRequestDto dto) {
        for (PlatMultipleRequestDto.PlatFormItem item : dto.getPlats()) {
            if (item.getNom() == null || item.getNom().trim().isEmpty()) continue;

            CategoriePlats cat = categoriePlatsRepository.findById(item.getIdCategorie())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + item.getIdCategorie()));

            Plats plat = new Plats();
            plat.setNom(item.getNom());
            plat.setCategoriePlats(cat);
            plat.setPrixVente(item.getPrixVente());
            Plats platSauvegarde = platsRepository.save(plat);

            if (item.getIngredients() != null) {
                for (PlatMultipleRequestDto.IngredientQuantiteDto ingDto : item.getIngredients()) {
                    if (ingDto.getIdIngredient() != null && ingDto.getQuantiteRequise() != null) {
                        Ingredients ing = ingredientsRepository.findById(ingDto.getIdIngredient())
                                .orElseThrow(() -> new ResourceNotFoundException("Ingrédient introuvable"));

                        RecettePlats rp = new RecettePlats();
                        rp.setPlat(platSauvegarde);
                        rp.setIngredient(ing);
                        rp.setQuantiteRequise(ingDto.getQuantiteRequise());
                        recettePlatsRepository.save(rp);
                    }
                }
            }
        }
    }

    @Transactional
    public void deleteById(Long id) {
        platsRepository.deleteById(id);
    }
}
