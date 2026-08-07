package com.gestion.restaurant.specification.materielles;

import com.gestion.restaurant.dto.materielles.MaterielSearchCriteria;
import com.gestion.restaurant.entity.materielles.Materielles;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MateriellesSpecification {

    public static Specification<Materielles> withFilters(MaterielSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria == null) {
                return cb.conjunction();
            }

            if (criteria.getNom() != null && !criteria.getNom().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nom")), "%" + criteria.getNom().toLowerCase().trim() + "%"));
            }

            if (criteria.getIdCategorie() != null) {
                predicates.add(cb.equal(root.get("categorieMaterielles").get("id"), criteria.getIdCategorie()));
            }

            if (criteria.getIdStatut() != null) {
                predicates.add(cb.equal(root.get("statutMaterielles").get("id"), criteria.getIdStatut()));
            }

            if (criteria.getDateEntreeDebut() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateEntree"), criteria.getDateEntreeDebut()));
            }

            if (criteria.getDateEntreeFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateEntree"), criteria.getDateEntreeFin()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
