package com.gestion.restaurant.service.caisse;

import com.gestion.restaurant.dto.caisse.MouvementCaisseRequestDto;
import com.gestion.restaurant.entity.caisse.MouvementCaisse;
import com.gestion.restaurant.entity.caisse.TypeMouvementCaisse;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.caisse.MouvementCaisseRepository;
import com.gestion.restaurant.repository.caisse.TypeMouvementCaisseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.gestion.restaurant.repository.commandes.CommandesRepository;
import com.gestion.restaurant.repository.ingredients.HistoriqueIngredientsRepository;
import com.gestion.restaurant.repository.materielles.HistoriqueMateriellesRepository;
import com.gestion.restaurant.repository.materielles.MaintenanceMateriellesRepository;
import com.gestion.restaurant.dto.caisse.CaisseSummaryDTO;
import com.gestion.restaurant.dto.caisse.MouvementCaisseSearchCriteria;

/**
 * Service central pour la caisse. Utilisé par les autres modules
 * (ingrédients, matériels, commandes) et par l'écran de saisie manuelle.
 */
@Service
public class CaisseService {

    public static final String TYPE_ENTREE = "Entree";
    public static final String TYPE_SORTIE = "Sortie";

    private final MouvementCaisseRepository mouvementCaisseRepository;
    private final TypeMouvementCaisseRepository typeMouvementCaisseRepository;
    private final HistoriqueIngredientsRepository historiqueIngredientsRepository;
    private final HistoriqueMateriellesRepository historiqueMateriellesRepository;
    private final MaintenanceMateriellesRepository maintenanceMateriellesRepository;
    private final CommandesRepository commandesRepository;

    public CaisseService(MouvementCaisseRepository mouvementCaisseRepository,
                         TypeMouvementCaisseRepository typeMouvementCaisseRepository,
                         HistoriqueIngredientsRepository historiqueIngredientsRepository,
                         HistoriqueMateriellesRepository historiqueMateriellesRepository,
                         MaintenanceMateriellesRepository maintenanceMateriellesRepository,
                         CommandesRepository commandesRepository) {
        this.mouvementCaisseRepository = mouvementCaisseRepository;
        this.typeMouvementCaisseRepository = typeMouvementCaisseRepository;
        this.historiqueIngredientsRepository = historiqueIngredientsRepository;
        this.historiqueMateriellesRepository = historiqueMateriellesRepository;
        this.maintenanceMateriellesRepository = maintenanceMateriellesRepository;
        this.commandesRepository = commandesRepository;
    }

    @Transactional(readOnly = true)
    public CaisseSummaryDTO getSummaryBetween(LocalDate debut, LocalDate fin) {
        CaisseSummaryDTO s = new CaisseSummaryDTO();
        if (debut == null || fin == null) return s;

        s.setTotalEntrees(zeroIfNull(mouvementCaisseRepository.sumMontantByTypeBetween(debut, fin, TYPE_ENTREE)));
        s.setTotalSorties(zeroIfNull(mouvementCaisseRepository.sumMontantByTypeBetween(debut, fin, TYPE_SORTIE)));
        s.setAchatsIngredients(zeroIfNull(historiqueIngredientsRepository.sumAchatsBetween(debut, fin)));
        s.setAchatsMaterielles(zeroIfNull(historiqueMateriellesRepository.sumAchatsBetween(debut, fin)));
        s.setMaintenance(zeroIfNull(maintenanceMateriellesRepository.sumCoutBetween(debut, fin)));
        s.setVentes(zeroIfNull(commandesRepository.sumMontantTotalBetween(debut, fin)));

        return s;
    }

    @Transactional(readOnly = true)
    public CaisseSummaryDTO getSummaryForYear(int annee) {
        LocalDate debut = LocalDate.of(annee, 1, 1);
        LocalDate fin = LocalDate.of(annee, 12, 31);
        return getSummaryBetween(debut, fin);
    }

    @Transactional(readOnly = true)
    public Page<MouvementCaisse> findAll(Pageable pageable) {
        return mouvementCaisseRepository.findAllWithType(pageable);
    }

    @Transactional(readOnly = true)
    public Page<MouvementCaisse> search(MouvementCaisseSearchCriteria criteria, Pageable pageable) {
        if (criteria.getDateDebut() != null && criteria.getDateFin() != null
                && criteria.getDateDebut().isAfter(criteria.getDateFin())) {
            throw new BusinessRuleException("La date de début doit être antérieure ou égale à la date de fin.", "/caisse");
        }
        return mouvementCaisseRepository.search(criteria.getDateDebut(), criteria.getDateFin(),
                criteria.getIdTypeMouvement(), pageable);
    }

    private BigDecimal zeroIfNull(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    @Transactional(readOnly = true)
    public MouvementCaisse findById(Long id) {
        return mouvementCaisseRepository.findByIdWithType(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mouvement de caisse introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<TypeMouvementCaisse> findAllTypes() {
        return typeMouvementCaisseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MouvementCaisseRequestDto toRequestDto(Long id) {
        MouvementCaisse m = findById(id);
        MouvementCaisseRequestDto dto = new MouvementCaisseRequestDto();
        dto.setId(m.getId());
        dto.setDateMouvement(m.getDateMouvement());
        dto.setMontant(m.getMontant());
        dto.setIdTypeMouvement(m.getTypeMouvement() != null ? m.getTypeMouvement().getId() : null);
        return dto;
    }

    @Transactional
    public MouvementCaisse saveFromDto(MouvementCaisseRequestDto dto) {
        if (dto.getMontant() == null || dto.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Le montant du mouvement de caisse doit être positif", "/caisse");
        }
        if (dto.getIdTypeMouvement() == null) {
            throw new BusinessRuleException("Le type de mouvement est obligatoire", "/caisse");
        }

        TypeMouvementCaisse type = typeMouvementCaisseRepository.findById(dto.getIdTypeMouvement())
                .orElseThrow(() -> new ResourceNotFoundException("Type de mouvement introuvable : " + dto.getIdTypeMouvement()));

        MouvementCaisse mouvement = dto.getId() != null ? findById(dto.getId()) : new MouvementCaisse();
        mouvement.setDateMouvement(dto.getDateMouvement() != null ? dto.getDateMouvement() : LocalDate.now());
        mouvement.setMontant(dto.getMontant());
        mouvement.setTypeMouvement(type);
        return mouvementCaisseRepository.save(mouvement);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!mouvementCaisseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Mouvement de caisse introuvable : " + id);
        }
        mouvementCaisseRepository.deleteById(id);
    }

    @Transactional
    public MouvementCaisse enregistrerSortie(BigDecimal montant, LocalDate date) {
        return enregistrerMouvement(TYPE_SORTIE, montant, date);
    }

    @Transactional
    public MouvementCaisse enregistrerEntree(BigDecimal montant, LocalDate date) {
        return enregistrerMouvement(TYPE_ENTREE, montant, date);
    }

    @Transactional
    public MouvementCaisse enregistrerMouvement(String typeLibelle, BigDecimal montant, LocalDate date) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Le montant du mouvement de caisse doit être positif");
        }
        TypeMouvementCaisse type = typeMouvementCaisseRepository.findByLibelle(typeLibelle)
                .orElseGet(() -> {
                    TypeMouvementCaisse t = new TypeMouvementCaisse();
                    t.setLibelle(typeLibelle);
                    return typeMouvementCaisseRepository.save(t);
                });

        MouvementCaisse mouvement = new MouvementCaisse();
        mouvement.setDateMouvement(date != null ? date : LocalDate.now());
        mouvement.setMontant(montant);
        mouvement.setTypeMouvement(type);
        return mouvementCaisseRepository.save(mouvement);
    }
}
