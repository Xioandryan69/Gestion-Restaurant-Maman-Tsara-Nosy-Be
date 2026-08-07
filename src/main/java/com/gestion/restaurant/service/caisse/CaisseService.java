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

    public CaisseService(MouvementCaisseRepository mouvementCaisseRepository,
                         TypeMouvementCaisseRepository typeMouvementCaisseRepository) {
        this.mouvementCaisseRepository = mouvementCaisseRepository;
        this.typeMouvementCaisseRepository = typeMouvementCaisseRepository;
    }

    @Transactional(readOnly = true)
    public Page<MouvementCaisse> findAll(Pageable pageable) {
        return mouvementCaisseRepository.findAllWithType(pageable);
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
