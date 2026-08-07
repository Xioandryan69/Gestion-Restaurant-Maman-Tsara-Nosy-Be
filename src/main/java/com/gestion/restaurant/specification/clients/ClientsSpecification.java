package com.gestion.restaurant.specification.clients;

import com.gestion.restaurant.dto.clients.ClientSearchCriteria;
import com.gestion.restaurant.entity.clients.Clients;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ClientsSpecification {

    public static Specification<Clients> withFilters(ClientSearchCriteria criteria) {
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
            if (criteria.getIdTypeClient() != null) {
                predicates.add(cb.equal(root.get("typeClient").get("id"), criteria.getIdTypeClient()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
