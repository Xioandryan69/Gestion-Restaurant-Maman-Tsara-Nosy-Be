package com.gestion.restaurant.service.materielles;

import com.gestion.restaurant.dto.materielles.*;
import com.gestion.restaurant.entity.fournisseurs.Fournisseurs;
import com.gestion.restaurant.entity.materielles.*;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.materielles.*;
import com.gestion.restaurant.service.caisse.CaisseService;
import com.gestion.restaurant.service.fournisseurs.FournisseursService;
import com.gestion.restaurant.specification.materielles.MateriellesSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class MateriellesService {

    public static final String STATUT_HORS_SERVICE   = "Hors Service";
    public static final String STATUT_EN_MAINTENANCE = "En maintenance";
    public static final String MVT_ENTREE            = "Entree";
    public static final String MVT_MAINTENANCE       = "Maintenance";
    public static final String MVT_HORS_SERVICE      = "HorsService";

    private final MateriellesRepository materiellesRepository;
    private final CategorieMateriellesRepository categorieMateriellesRepository;
    private final StatutMateriellesRepository statutMateriellesRepository;
    private final HistoriqueMateriellesRepository historiqueMateriellesRepository;
    private final MaintenanceMateriellesRepository maintenanceMateriellesRepository;
    private final InventairesMateriellesRepository inventairesMateriellesRepository;
    private final EtatStockMateriellesRepository etatStockMateriellesRepository;
    private final TypeMvtMateriellesRepository typeMvtMateriellesRepository;
    private final CaisseService caisseService;
    private final FournisseursService fournisseursService;

    public MateriellesService(MateriellesRepository materiellesRepository,
                               CategorieMateriellesRepository categorieMateriellesRepository,
                               StatutMateriellesRepository statutMateriellesRepository,
                               HistoriqueMateriellesRepository historiqueMateriellesRepository,
                               MaintenanceMateriellesRepository maintenanceMateriellesRepository,
                               InventairesMateriellesRepository inventairesMateriellesRepository,
                               EtatStockMateriellesRepository etatStockMateriellesRepository,
                               TypeMvtMateriellesRepository typeMvtMateriellesRepository,
                               CaisseService caisseService,
                               FournisseursService fournisseursService) {
        this.materiellesRepository = materiellesRepository;
        this.categorieMateriellesRepository = categorieMateriellesRepository;
        this.statutMateriellesRepository = statutMateriellesRepository;
        this.historiqueMateriellesRepository = historiqueMateriellesRepository;
        this.maintenanceMateriellesRepository = maintenanceMateriellesRepository;
        this.inventairesMateriellesRepository = inventairesMateriellesRepository;
        this.etatStockMateriellesRepository = etatStockMateriellesRepository;
        this.typeMvtMateriellesRepository = typeMvtMateriellesRepository;
        this.caisseService = caisseService;
        this.fournisseursService = fournisseursService;
    }

    @Transactional(readOnly = true)
    public Page<MaterielResponseDto> search(MaterielSearchCriteria criteria, Pageable pageable) {
        Specification<Materielles> spec = MateriellesSpecification.withFilters(criteria);
        return materiellesRepository.findAll(spec, pageable).map(MaterielMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Materielles findById(Long id) {
        return materiellesRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matériel introuvable avec l'ID : " + id));
    }

    @Transactional(readOnly = true)
    public MaterielRequestDto toRequestDto(Long id) {
        Materielles m = findById(id);
        MaterielRequestDto dto = new MaterielRequestDto();
        dto.setId(m.getId());
        dto.setNom(m.getNom());
        dto.setDateEntree(m.getDateEntree());
        dto.setIdCategorie(m.getCategorieMaterielles() != null ? m.getCategorieMaterielles().getId() : null);
        dto.setIdStatut(m.getStatutMaterielles() != null ? m.getStatutMaterielles().getId() : null);
        return dto;
    }

    @Transactional(readOnly = true)
    public MaterielResponseDto findDtoById(Long id) {
        return MaterielMapper.toDto(findById(id));
    }

    @Transactional(readOnly = true)
    public List<CategorieMaterielles> findAllCategories() {
        return categorieMateriellesRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StatutMaterielles> findAllStatuts() {
        return statutMateriellesRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Fournisseurs> findAllFournisseurs() {
        return fournisseursService.findAllForSelect();
    }

    @Transactional
    public MaterielResponseDto saveFromDto(MaterielRequestDto dto) {
        if (dto.getNom() == null || dto.getNom().isBlank()) {
            throw new BusinessRuleException("Le nom du matériel est obligatoire.");
        }
        if (dto.getIdCategorie() == null) {
            throw new BusinessRuleException("La catégorie du matériel est obligatoire.");
        }
        if (dto.getIdStatut() == null) {
            throw new BusinessRuleException("Le statut du matériel est obligatoire.");
        }

        Materielles materiel = (dto.getId() != null) ? findById(dto.getId()) : new Materielles();
        
        CategorieMaterielles categorie = categorieMateriellesRepository.findById(dto.getIdCategorie())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable avec l'ID : " + dto.getIdCategorie()));
        
        StatutMaterielles statut = statutMateriellesRepository.findById(dto.getIdStatut())
                .orElseThrow(() -> new ResourceNotFoundException("Statut introuvable avec l'ID : " + dto.getIdStatut()));

        materiel.setNom(dto.getNom());
        materiel.setCategorieMaterielles(categorie);
        materiel.setStatutMaterielles(statut);
        materiel.setDateEntree(dto.getDateEntree() != null ? dto.getDateEntree() : LocalDate.now());

        return MaterielMapper.toDto(materiellesRepository.save(materiel));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!materiellesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Impossible de supprimer : Matériel introuvable avec l'ID : " + id);
        }
        materiellesRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal getStockActuel(Long idMateriel) {
        return etatStockMateriellesRepository.findTopByMateriel_IdOrderByDateEtatStockDescIdDesc(idMateriel)
                .map(EtatStockMaterielles::getQuantite)
                .orElse(BigDecimal.ZERO);
    }

    private void enregistrerSnapshotStock(Materielles materiel, BigDecimal nouvelleQuantite, LocalDate date) {
        EtatStockMaterielles snapshot = new EtatStockMaterielles();
        snapshot.setMateriel(materiel);
        snapshot.setDateEtatStock(date != null ? date : LocalDate.now());
        snapshot.setQuantite(nouvelleQuantite);
        etatStockMateriellesRepository.save(snapshot);
    }

    @Transactional(readOnly = true)
    public List<HistoriqueMaterielles> findHistorique(Long idMateriel) {
        return historiqueMateriellesRepository.findByMateriel_IdOrderByDateEntreeDesc(idMateriel);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceMaterielles> findMaintenances(Long idMateriel) {
        return maintenanceMateriellesRepository.findByMateriel_IdOrderByDateMaintenanceDesc(idMateriel);
    }

    @Transactional(readOnly = true)
    public List<InventairesMaterielles> findInventaire(Long idMateriel) {
        return inventairesMateriellesRepository.findByMateriel_IdOrderByDateInventaireDesc(idMateriel);
    }

        // ---------- New reporting methods for charts (no ORM changes) ----------
        @Transactional(readOnly = true)
        public List<HistoriqueMaterielles> getAchatMateriellesBetween(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null) return List.of();
        return historiqueMateriellesRepository.findAll().stream()
            .filter(h -> (h.getDateEntree() != null &&
                (!h.getDateEntree().isBefore(debut) && !h.getDateEntree().isAfter(fin))))
            .toList();
        }

        @Transactional(readOnly = true)
        public List<MaintenanceMaterielles> getMaintenancesBetween(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null) return List.of();
        return maintenanceMateriellesRepository.findAll().stream()
            .filter(m -> (m.getDateMaintenance() != null &&
                (!m.getDateMaintenance().isBefore(debut) && !m.getDateMaintenance().isAfter(fin))))
            .toList();
        }

        @Transactional(readOnly = true)
        public long countMateriellesHorsServiceForYear(int annee) {
        return materiellesRepository.findAll().stream()
            .filter(m -> m.getStatutMaterielles() != null &&
                ("Hors Service".equalsIgnoreCase(m.getStatutMaterielles().getLibelle())) &&
                m.getDateEntree() != null && m.getDateEntree().getYear() == annee)
            .count();
        }

    @Transactional
    public void enregistrerAchat(Long idMateriel, LocalDate dateEntree, BigDecimal quantite, BigDecimal prixAchat, Long idFournisseur) {
        Materielles mat = findById(idMateriel);
        
        HistoriqueMaterielles histo = new HistoriqueMaterielles();
        histo.setMateriel(mat);
        histo.setDateEntree(dateEntree != null ? dateEntree : LocalDate.now());
        histo.setQuantite(quantite);
        histo.setPrixAchat(prixAchat);
        if (idFournisseur != null) {
            histo.setFournisseur(fournisseursService.findById(idFournisseur));
        }
        historiqueMateriellesRepository.save(histo);

        // Mise à jour du stock
        BigDecimal nouveauStock = getStockActuel(idMateriel).add(quantite);
        enregistrerSnapshotStock(mat, nouveauStock, dateEntree);

        // Sortie de caisse
        BigDecimal montantTotal = quantite.multiply(prixAchat);
        if (montantTotal.compareTo(BigDecimal.ZERO) > 0) {
            caisseService.enregistrerSortie(montantTotal, dateEntree);
        }
    }

    @Transactional
    public void enregistrerMaintenance(Long idMateriel, LocalDate dateMaintenance, String description, BigDecimal cout, String technicien) {
        Materielles mat = findById(idMateriel);

        MaintenanceMaterielles maint = new MaintenanceMaterielles();
        maint.setMateriel(mat);
        maint.setDateMaintenance(dateMaintenance != null ? dateMaintenance : LocalDate.now());
        maint.setDescription(description);
        maint.setCout(cout);
        maint.setTechnicien(technicien);
        maintenanceMateriellesRepository.save(maint);

        // Mise à jour du statut
        statutMateriellesRepository.findByLibelle(STATUT_EN_MAINTENANCE)
                .ifPresent(mat::setStatutMaterielles);

        // Sortie de caisse pour frais de maintenance
        if (cout != null && cout.compareTo(BigDecimal.ZERO) > 0) {
            caisseService.enregistrerSortie(cout, dateMaintenance);
        }
    }

    @Transactional
    public void mettreHorsService(Long idMateriel) {
        Materielles mat = findById(idMateriel);
        statutMateriellesRepository.findByLibelle(STATUT_HORS_SERVICE)
                .ifPresent(mat::setStatutMaterielles);
    }
}