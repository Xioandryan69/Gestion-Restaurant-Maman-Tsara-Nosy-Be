package com.gestion.restaurant.service.plats;

import com.gestion.restaurant.dto.plats.PlatDashboardDto;
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
import java.math.BigDecimal;
import com.gestion.restaurant.entity.ingredients.HistoriqueIngredients;
import com.gestion.restaurant.repository.ingredients.HistoriqueIngredientsRepository;
import com.gestion.restaurant.dto.plats.PlatUpdateRequestDto;

@Service
public class PlatsService {

    private final PlatsRepository platsRepository;
    private final CategoriePlatsRepository categoriePlatsRepository;
    private final RecettePlatsRepository recettePlatsRepository;
    private final IngredientsRepository ingredientsRepository;
    private final HistoriqueIngredientsRepository historiqueIngredientsRepository;

    public PlatsService(PlatsRepository platsRepository,
                        CategoriePlatsRepository categoriePlatsRepository,
                        RecettePlatsRepository recettePlatsRepository,
                        IngredientsRepository ingredientsRepository,
                        HistoriqueIngredientsRepository historiqueIngredientsRepository) {
        this.platsRepository = platsRepository;
        this.categoriePlatsRepository = categoriePlatsRepository;
        this.recettePlatsRepository = recettePlatsRepository;
        this.ingredientsRepository = ingredientsRepository;
        this.historiqueIngredientsRepository = historiqueIngredientsRepository;
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

    @Transactional(readOnly = true)
    public PlatDashboardDto getDashboardData() {
        long totalPlats = platsRepository.count();
        long platsAvecRecette = platsRepository.findAll().stream()
                .filter(p -> p.getId() != null && recettePlatsRepository.findByPlatIdWithIngredient(p.getId()).size() > 0)
                .count();
        long platsSansRecette = totalPlats - platsAvecRecette;
        BigDecimal chiffreAffairesPotentiel = platsRepository.findAll().stream()
                .map(Plats::getPrixVente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal margePotentielle = chiffreAffairesPotentiel.subtract(chiffreAffairesPotentiel.multiply(BigDecimal.valueOf(0.25)));

        PlatDashboardDto dto = new PlatDashboardDto();
        dto.setTotalPlats(totalPlats);
        dto.setPlatsAvecRecette(platsAvecRecette);
        dto.setPlatsSansRecette(platsSansRecette);
        dto.setChiffreAffairesPotentiel(chiffreAffairesPotentiel);
        dto.setMargePotentielle(margePotentielle);
        dto.setTendance(platsSansRecette == 0 ? "Croissance" : "À compléter");

        dto.getAlerts().add(platsSansRecette == 0 ? "Toutes les recettes sont renseignées" : platsSansRecette + " plat(s) n’ont pas encore de recette");
        dto.getAlerts().add("Prix de vente et coût ingrédients suivis");

        dto.getTopPlats().add("Plats à forte marge : à définir");
        dto.getTopPlats().add("Évolution des ventes : à suivre");
        dto.getTopPlats().add("Recettes validées : " + platsAvecRecette);

        dto.getQuickActions().add("Créer un nouveau plat");
        dto.getQuickActions().add("Renseigner une recette");
        dto.getQuickActions().add("Évaluer la rentabilité");
        return dto;
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

    @Transactional(readOnly = true)
    public BigDecimal calculatePrixAchatPlat(Long idPlat) {
        List<RecettePlats> recettes = recettePlatsRepository.findByPlatIdWithIngredient(idPlat);
        return recettes.stream().map(rp -> {
            if (rp.getIngredient() == null || rp.getQuantiteRequise() == null) return BigDecimal.ZERO;

            Long idIng = rp.getIngredient().getId();
            List<HistoriqueIngredients> hist = historiqueIngredientsRepository.findByIngredient_IdOrderByDateEntreeDesc(idIng);

            if (hist.isEmpty()) {
                return BigDecimal.ZERO;
            }

            BigDecimal prixUnitaire = hist.get(0).getPrixAchat() != null ? hist.get(0).getPrixAchat() : BigDecimal.ZERO;
            return prixUnitaire.multiply(rp.getQuantiteRequise());
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateProfitPerUnit(Long idPlat) {
        Plats plat = findById(idPlat);
        BigDecimal prixAchat = calculatePrixAchatPlat(idPlat);
        BigDecimal prixVente = plat.getPrixVente() != null ? plat.getPrixVente() : BigDecimal.ZERO;
        return prixVente.subtract(prixAchat);
    }

    @Transactional(readOnly = true)
    public List<String> getCostEvolutionSummary() {
        List<String> summary = new ArrayList<>();
        summary.add("Coût moyen des recettes : calculé à partir des derniers achats ingrédients");
        summary.add("Bénéfice estimé : prix de vente - coût de recette");
        summary.add("Mise à jour automatique à chaque consultation");
        return summary;
    }
    @Transactional(readOnly = true)
public PlatUpdateRequestDto toUpdateDto(Long id) {

    Plats plat = findById(id);

    PlatUpdateRequestDto dto = new PlatUpdateRequestDto();

    dto.setId(plat.getId());
    dto.setNom(plat.getNom());
    dto.setPrixVente(plat.getPrixVente());

    if (plat.getCategoriePlats() != null) {
        dto.setIdCategorie(plat.getCategoriePlats().getId());
    }

    return dto;
}

@Transactional
public Plats updateFromDto(PlatUpdateRequestDto dto) {

    if (dto.getId() == null) {
        throw new ResourceNotFoundException("ID du plat obligatoire");
    }

    Plats plat = platsRepository.findById(dto.getId())
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Plat non trouvé avec l'ID : " + dto.getId()
                    ));

    CategoriePlats categorie = categoriePlatsRepository
            .findById(dto.getIdCategorie())
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Catégorie introuvable : " + dto.getIdCategorie()
                    ));

    plat.setNom(dto.getNom());
    plat.setCategoriePlats(categorie);
    plat.setPrixVente(dto.getPrixVente());

    return platsRepository.save(plat);
}
}
