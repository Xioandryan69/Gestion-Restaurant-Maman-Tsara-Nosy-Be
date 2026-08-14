package com.gestion.restaurant.service.ingredients;

import com.gestion.restaurant.dto.ingredients.*;
import com.gestion.restaurant.entity.ingredients.*;
import com.gestion.restaurant.entity.fournisseurs.Fournisseurs;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.ingredients.*;
import com.gestion.restaurant.service.caisse.CaisseService;
import com.gestion.restaurant.service.fournisseurs.FournisseursService;
import com.gestion.restaurant.specification.ingredients.IngredientsSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import com.gestion.restaurant.dto.ingredients.IngredientMovementSummaryDTO;

@Service
public class IngredientsService {

    public static final String MVT_ENTREE = "Entrée";
    public static final String MVT_SORTIE_PERIME = "Perte / Périmé";
    public static final String MVT_SORTIE_CUISINE = "Sortie (Cuisine)";

    private final IngredientsRepository ingredientsRepository;
    private final CategorieIngredientsRepository categorieRepo;
    private final StatutIngredientRepository statutRepo;
    private final HistoriqueIngredientsRepository historiqueRepo;
    private final InventaireIngredientRepository inventaireRepo;
    private final EtatStockIngredientRepository etatStockRepo;
    private final TypeMvtIngredientRepository typeMvtRepo;
    private final UniteRepository uniteRepo;
    private final CaisseService caisseService;
    private final FournisseursService fournisseursService;

    public IngredientsService(IngredientsRepository ingredientsRepository,
                              CategorieIngredientsRepository categorieRepo,
                              StatutIngredientRepository statutRepo,
                              HistoriqueIngredientsRepository historiqueRepo,
                              InventaireIngredientRepository inventaireRepo,
                              EtatStockIngredientRepository etatStockRepo,
                              TypeMvtIngredientRepository typeMvtRepo,
                              UniteRepository uniteRepo,
                              CaisseService caisseService,
                              FournisseursService fournisseursService) {
        this.ingredientsRepository = ingredientsRepository;
        this.categorieRepo = categorieRepo;
        this.statutRepo = statutRepo;
        this.historiqueRepo = historiqueRepo;
        this.inventaireRepo = inventaireRepo;
        this.etatStockRepo = etatStockRepo;
        this.typeMvtRepo = typeMvtRepo;
        this.uniteRepo = uniteRepo;
        this.caisseService = caisseService;
        this.fournisseursService = fournisseursService;
    }

    // ───────────────────────── Recherche Multicritère ─────────────────────────

    @Transactional(readOnly = true)
    public Page<IngredientResponseDto> search(IngredientSearchCriteria criteria, Pageable pageable) {
        Specification<Ingredients> spec = IngredientsSpecification.withFilters(criteria);
        return ingredientsRepository.findAll(spec, pageable).map(IngredientMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Ingredients findById(Long id) {
        return ingredientsRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrédient introuvable avec l'ID : " + id));
    }

    @Transactional(readOnly = true)
    public IngredientResponseDto findDtoById(Long id) {
        return IngredientMapper.toDto(findById(id));
    }

    @Transactional(readOnly = true)
    public IngredientRequestDto toRequestDto(Long id) {
        Ingredients entity = findById(id);
        IngredientRequestDto dto = new IngredientRequestDto();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        if (entity.getCategorieIngredients() != null) {
            dto.setIdCategorie(entity.getCategorieIngredients().getId());
        }
        if (entity.getStatutIngredient() != null) {
            dto.setIdStatut(entity.getStatutIngredient().getId());
        }
        if (entity.getFournisseur() != null) {
            dto.setIdFournisseur(entity.getFournisseur().getId());
        }
        if (entity.getUnite() != null) {
            dto.setIdUnite(entity.getUnite().getId());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<CategorieIngredients> findAllCategories() {
        return categorieRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<StatutIngredient> findAllStatuts() {
        return statutRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Unite> findAllUnites() {
        return uniteRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Fournisseurs> findAllFournisseurs() {
        return fournisseursService.findAllForSelect();
    }

    @Transactional(readOnly = true)
    public IngredientDashboardDto getDashboardData() {
        long totalIngredients = ingredientsRepository.count();
        BigDecimal stockTotal = etatStockRepo.findAll().stream()
                .map(EtatStockIngredient::getQuantite)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long ingredientsEnStock = etatStockRepo.findAll().stream()
                .filter(e -> e.getQuantite() != null && e.getQuantite().compareTo(BigDecimal.ZERO) > 0)
                .count();
        long ingredientsEnAlerte = totalIngredients - ingredientsEnStock;

        IngredientDashboardDto dto = new IngredientDashboardDto();
        dto.setTotalIngredients(totalIngredients);
        dto.setIngredientsEnStock(ingredientsEnStock);
        dto.setIngredientsEnAlerte(ingredientsEnAlerte);
        dto.setStockTotal(stockTotal);
        dto.setValeurTotale(stockTotal.multiply(BigDecimal.valueOf(1000)));
        dto.setEtatStock(ingredientsEnAlerte == 0 ? "Optimal" : "À surveiller");
        dto.setDerniereMiseAJour(LocalDate.now().toString());

        dto.getAlerts().add(ingredientsEnAlerte == 0 ? "Aucune alerte de stock" : ingredientsEnAlerte + " ingrédient(s) nécessitent un réapprovisionnement");
        dto.getAlerts().add("Suivi des achats et sorties activé");

        dto.getMovements().add("Entrées enregistrées ce mois : " + historiqueRepo.count());
        dto.getMovements().add("Mouvements de stock : " + inventaireRepo.count());
        dto.getMovements().add("Seuil d’alerte : " + SEUIL_STOCK_FAIBLE);

        dto.getQuickActions().add("Consulter le stock global");
        dto.getQuickActions().add("Enregistrer un achat");
        dto.getQuickActions().add("Planifier une sortie");
        return dto;
    }

    // ───────────────────────── C.R.U.D Base ─────────────────────────

    @Transactional
    public IngredientResponseDto saveFromDto(IngredientRequestDto dto) {
        if (dto.getNom() == null || dto.getNom().isBlank()) {
            throw new BusinessRuleException("Le nom de l'ingrédient est obligatoire.");
        }
        if (dto.getIdCategorie() == null) {
            throw new BusinessRuleException("La catégorie est obligatoire.");
        }
        if (dto.getIdStatut() == null) {
            throw new BusinessRuleException("Le statut est obligatoire.");
        }
        if (dto.getIdFournisseur() == null) {
            throw new BusinessRuleException("Le fournisseur est obligatoire.");
        }
        if (dto.getIdUnite() == null) {
            throw new BusinessRuleException("L'unité est obligatoire.");
        }

        Ingredients ing = (dto.getId() != null) ? findById(dto.getId()) : new Ingredients();

        ing.setNom(dto.getNom());
        ing.setCategorieIngredients(categorieRepo.findById(dto.getIdCategorie())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + dto.getIdCategorie())));
        ing.setStatutIngredient(statutRepo.findById(dto.getIdStatut())
                .orElseThrow(() -> new ResourceNotFoundException("Statut introuvable : " + dto.getIdStatut())));
        ing.setFournisseur(fournisseursService.findById(dto.getIdFournisseur()));
        ing.setUnite(uniteRepo.findById(dto.getIdUnite())
                .orElseThrow(() -> new ResourceNotFoundException("Unité introuvable : " + dto.getIdUnite())));

        return IngredientMapper.toDto(ingredientsRepository.save(ing));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!ingredientsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ingrédient introuvable avec l'ID : " + id);
        }
        ingredientsRepository.deleteById(id);
    }

    // ───────────────────────── Gestion des Stocks & Mouvements ─────────────────────────

    @Transactional(readOnly = true)
    public BigDecimal getStockActuel(Long idIngredient) {
        return etatStockRepo.findTopByIngredient_IdOrderByDateEtatStockDescIdDesc(idIngredient)
                .map(EtatStockIngredient::getQuantite)
                .orElse(BigDecimal.ZERO);
    }

    private void enregistrerSnapshotStock(Ingredients ingredient, BigDecimal nouvelleQuantite, LocalDate date) {
        EtatStockIngredient snapshot = new EtatStockIngredient();
        snapshot.setIngredient(ingredient);
        snapshot.setDateEtatStock(date != null ? date : LocalDate.now());
        snapshot.setQuantite(nouvelleQuantite);
        etatStockRepo.save(snapshot);
    }

    private void enregistrerMouvementInventaire(Ingredients ing, String typeLibelle, BigDecimal quantite, LocalDate date) {
        InventaireIngredient inv = new InventaireIngredient();
        inv.setIngredient(ing);
        inv.setDateInventaire(date != null ? date : LocalDate.now());
        inv.setQuantite(quantite);
        inv.setTypeMvtIngredient(findOrCreateTypeMvt(typeLibelle));
        inventaireRepo.save(inv);
    }

    @Transactional
    public HistoriqueIngredients enregistrerAchatEntree(Long idIngredient, LocalDate dateEntree, LocalDate datePeremption,
                                                        BigDecimal quantite, BigDecimal prixAchat) {
        Ingredients ing = findById(idIngredient);

        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("La quantité doit être strictement supérieure à zéro.");
        }
        if (prixAchat == null || prixAchat.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Le prix d'achat doit être positif ou nul.");
        }
        if (datePeremption != null && dateEntree != null && datePeremption.isBefore(dateEntree)) {
            throw new BusinessRuleException("La date de péremption ne peut pas être antérieure à la date d'entrée.");
        }

        LocalDate date = (dateEntree != null) ? dateEntree : LocalDate.now();

        // 1. Sauvegarde de l'historique d'achat
        HistoriqueIngredients histo = new HistoriqueIngredients();
        histo.setIngredient(ing);
        histo.setDateEntree(date);
        histo.setDatePeremption(datePeremption);
        histo.setQuantite(quantite);
        histo.setPrixAchat(prixAchat);
        HistoriqueIngredients histoSaved = historiqueRepo.save(histo);

        // 2. Traitement Inventaire + Mis à jour de l'état de stock
        enregistrerMouvementInventaire(ing, MVT_ENTREE, quantite, date);
        BigDecimal nouveauStock = getStockActuel(idIngredient).add(quantite);
        enregistrerSnapshotStock(ing, nouveauStock, date);

        // 3. Traitement Caisse : Sortie de caisse (Achat ingrédient)
        BigDecimal montantTotal = quantite.multiply(prixAchat);
        if (montantTotal.compareTo(BigDecimal.ZERO) > 0) {
            caisseService.enregistrerSortie(montantTotal, date);
        }

        return histoSaved;
    }

    @Transactional
    public void enregistrerSortieOuPerte(Long idIngredient, BigDecimal quantiteSortie, String raisonLibelle, LocalDate dateMvt) {
        Ingredients ing = findById(idIngredient);

        if (quantiteSortie == null || quantiteSortie.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("La quantité sortie doit être supérieure à zéro.");
        }

        BigDecimal stockActuel = getStockActuel(idIngredient);
        if (stockActuel.compareTo(quantiteSortie) < 0) {
            throw new BusinessRuleException("Stock insuffisant pour effectuer cette sortie/perte.");
        }

        LocalDate date = (dateMvt != null) ? dateMvt : LocalDate.now();

        // Enregistrer inventaire
        enregistrerMouvementInventaire(ing, raisonLibelle, quantiteSortie, date);

        // Mise à jour de l'état de stock
        BigDecimal nouveauStock = stockActuel.subtract(quantiteSortie);
        enregistrerSnapshotStock(ing, nouveauStock, date);
    }

    @Transactional(readOnly = true)
    public List<HistoriqueIngredients> findHistorique(Long idIngredient) {
        return historiqueRepo.findByIngredient_IdOrderByDateEntreeDesc(idIngredient);
    }

    @Transactional(readOnly = true)
    public List<InventaireIngredient> findInventaire(Long idIngredient) {
        return inventaireRepo.findByIngredient_IdOrderByDateInventaireDesc(idIngredient);
    }

        // ---------- New reporting methods for charts (no ORM changes) ----------
        @Transactional(readOnly = true)
        public List<HistoriqueIngredients> getAchatIngredientsBetween(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null) return List.of();
        return historiqueRepo.findAll().stream()
            .filter(h -> (h.getDateEntree() != null &&
                (!h.getDateEntree().isBefore(debut) && !h.getDateEntree().isAfter(fin))))
            .toList();
        }

        @Transactional(readOnly = true)
        public List<IngredientMovementSummaryDTO> getIngredientMovementSummaryForYear(int annee) {
        List<Ingredients> all = ingredientsRepository.findAllWithRelationsList();
        List<HistoriqueIngredients> histos = historiqueRepo.findAll();
        List<InventaireIngredient> invs = inventaireRepo.findAll();

        return all.stream().map(ing -> {
            IngredientMovementSummaryDTO s = new IngredientMovementSummaryDTO();
            s.setIngredientId(ing.getId());
            s.setNom(ing.getNom());

            BigDecimal entree = histos.stream()
                .filter(h -> h.getIngredient() != null && h.getIngredient().getId().equals(ing.getId())
                    && h.getDateEntree() != null && h.getDateEntree().getYear() == annee)
                .map(HistoriqueIngredients::getQuantite)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sortie = invs.stream()
                .filter(i -> i.getIngredient() != null && i.getIngredient().getId().equals(ing.getId())
                    && i.getDateInventaire() != null && i.getDateInventaire().getYear() == annee
                    && (i.getTypeMvtIngredient() == null || !"Entrée".equalsIgnoreCase(i.getTypeMvtIngredient().getLibelle())))
                .map(InventaireIngredient::getQuantite)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            s.setEntreeTotal(entree);
            s.setSortieTotal(sortie);
            s.setStockCurrent(getStockActuel(ing.getId()));
            return s;
        }).toList();
        }

    private TypeMvtIngredient findOrCreateTypeMvt(String libelle) {
        return typeMvtRepo.findByLibelleIgnoreCase(libelle)
                .orElseGet(() -> {
                    TypeMvtIngredient t = new TypeMvtIngredient();
                    t.setLibelle(libelle);
                    return typeMvtRepo.save(t);
                });
    }

    /** Seuil en dessous duquel le stock est considéré comme faible (alerte dashboard stock). */
    public static final double SEUIL_STOCK_FAIBLE = 5.0;

    @Transactional(readOnly = true)
    public StockPageView getGlobalStockState(Pageable pageable) {
        return getGlobalStockState(null, pageable);
    }

    @Transactional(readOnly = true)
    public StockPageView getGlobalStockState(com.gestion.restaurant.dto.ingredients.StockSearchCriteria criteria, Pageable pageable) {
        Map<Long, Double> stocks = etatStockRepo.findLatestQuantiteByIngredient().stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).doubleValue(),
                        (a, b) -> a
                ));

        long total = ingredientsRepository.count();
        long stockOk = stocks.values().stream().filter(q -> q >= SEUIL_STOCK_FAIBLE).count();
        // Ingrédients sans ligne d'état = quantité 0 → alerte
        long nombreAlerte = total - stockOk;
        long nombreOk = stockOk;

        org.springframework.data.jpa.domain.Specification<Ingredients> filter = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (criteria != null && criteria.getNom() != null && !criteria.getNom().isBlank()) predicates.add(cb.like(cb.lower(root.get("nom")), "%" + criteria.getNom().trim().toLowerCase() + "%"));
            if (criteria != null && criteria.getIdCategorie() != null) predicates.add(cb.equal(root.get("categorieIngredients").get("id"), criteria.getIdCategorie()));
            if (criteria != null && criteria.getIdUnite() != null) predicates.add(cb.equal(root.get("unite").get("id"), criteria.getIdUnite()));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<IngredientStockDTO> page = ingredientsRepository.findAll(filter, pageable)
                .map(ing -> new IngredientStockDTO(ing, stocks.getOrDefault(ing.getId(), 0.0)));

        return new StockPageView(page, nombreAlerte, nombreOk, SEUIL_STOCK_FAIBLE);
    }

    /**
     * Réintégration de stock (ex. annulation de commande) sans mouvement de caisse.
     */
    @Transactional
    public void reintegrerStock(Long idIngredient, BigDecimal quantite, LocalDate dateMvt) {
        Ingredients ing = findById(idIngredient);
        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("La quantité à réintégrer doit être supérieure à zéro.");
        }
        LocalDate date = dateMvt != null ? dateMvt : LocalDate.now();
        enregistrerMouvementInventaire(ing, MVT_ENTREE, quantite, date);
        BigDecimal nouveauStock = getStockActuel(idIngredient).add(quantite);
        enregistrerSnapshotStock(ing, nouveauStock, date);
    }

    /** Listes déroulantes (formulaires). */
    @Transactional(readOnly = true)
    public List<Ingredients> findAll() {
        return ingredientsRepository.findAllWithRelationsList();
    }
}
