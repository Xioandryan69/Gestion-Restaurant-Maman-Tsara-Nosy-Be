package com.gestion.restaurant.specification.personnels;

import com.gestion.restaurant.dto.personnels.PersonnelSearchCriteria;
import com.gestion.restaurant.entity.personnels.Personnels;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PersonnelsSpecification {

    public static Specification<Personnels> withFilters(PersonnelSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria == null) {
                return cb.conjunction();
            }

            if (criteria.getNom() != null && !criteria.getNom().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nom")), "%" + criteria.getNom().toLowerCase() + "%"));
            }
            if (criteria.getPrenom() != null && !criteria.getPrenom().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("prenom")), "%" + criteria.getPrenom().toLowerCase() + "%"));
            }
            if (criteria.getIdRole() != null) {
                predicates.add(cb.equal(root.get("rolePersonnels").get("id"), criteria.getIdRole()));
            }
            if (criteria.getContact() != null && !criteria.getContact().isBlank()) predicates.add(cb.like(root.get("contact"), "%" + criteria.getContact().trim() + "%"));
            if (criteria.getDateEmbaucheDebut() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("dateEmbauche"), criteria.getDateEmbaucheDebut()));
            if (criteria.getDateEmbaucheFin() != null) predicates.add(cb.lessThanOrEqualTo(root.get("dateEmbauche"), criteria.getDateEmbaucheFin()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
