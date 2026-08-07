package com.gestion.restaurant.specification.ingredients;

import com.gestion.restaurant.dto.ingredients.IngredientSearchCriteria;
import com.gestion.restaurant.entity.ingredients.Ingredients;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class IngredientsSpecification {

    public static Specification<Ingredients> withFilters(IngredientSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria == null) {
                return cb.conjunction();
            }

            if (criteria.getNom() != null && !criteria.getNom().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nom")), "%" + criteria.getNom().toLowerCase().trim() + "%"));
            }

            if (criteria.getIdCategorie() != null) {
                predicates.add(cb.equal(root.get("categorieIngredients").get("id"), criteria.getIdCategorie()));
            }

            if (criteria.getIdStatut() != null) {
                predicates.add(cb.equal(root.get("statutIngredient").get("id"), criteria.getIdStatut()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
