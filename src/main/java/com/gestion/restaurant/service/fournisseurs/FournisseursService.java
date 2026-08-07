package com.gestion.restaurant.service.fournisseurs;

import com.gestion.restaurant.dto.fournisseurs.FournisseurRequestDto;
import com.gestion.restaurant.entity.fournisseurs.Fournisseurs;
import com.gestion.restaurant.entity.fournisseurs.TypeFournisseurs;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.fournisseur.FournisseursRepository;
import com.gestion.restaurant.repository.fournisseur.TypeFournisseursRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FournisseursService {

    private final FournisseursRepository fournisseursRepository;
    private final TypeFournisseursRepository typeFournisseursRepository;

    public FournisseursService(FournisseursRepository fournisseursRepository,
                               TypeFournisseursRepository typeFournisseursRepository) {
        this.fournisseursRepository = fournisseursRepository;
        this.typeFournisseursRepository = typeFournisseursRepository;
    }

    @Transactional(readOnly = true)
    public Page<Fournisseurs> findAll(Pageable pageable) {
        return fournisseursRepository.findAllWithType(pageable);
    }

    /** Listes déroulantes (formulaires). */
    @Transactional(readOnly = true)
    public List<Fournisseurs> findAllForSelect() {
        return fournisseursRepository.findAllWithTypeList();
    }

    @Transactional(readOnly = true)
    public Fournisseurs findById(Long id) {
        return fournisseursRepository.findByIdWithType(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<TypeFournisseurs> findAllTypes() {
        return typeFournisseursRepository.findAll();
    }

    @Transactional(readOnly = true)
    public FournisseurRequestDto toRequestDto(Long id) {
        Fournisseurs f = findById(id);
        FournisseurRequestDto dto = new FournisseurRequestDto();
        dto.setId(f.getId());
        dto.setNom(f.getNom());
        dto.setPrenom(f.getPrenom());
        dto.setContact(f.getContact());
        dto.setIdTypeFournisseur(f.getTypeFournisseurs() != null ? f.getTypeFournisseurs().getId() : null);
        return dto;
    }

    @Transactional
    public Fournisseurs saveFromDto(FournisseurRequestDto dto) {
        if (dto.getNom() == null || dto.getNom().isBlank()) {
            throw new BusinessRuleException("Le nom du fournisseur est obligatoire", "/fournisseurs");
        }
        if (dto.getIdTypeFournisseur() == null) {
            throw new BusinessRuleException("Le type de fournisseur est obligatoire", "/fournisseurs");
        }

        TypeFournisseurs type = typeFournisseursRepository.findById(dto.getIdTypeFournisseur())
                .orElseThrow(() -> new ResourceNotFoundException("Type de fournisseur introuvable : " + dto.getIdTypeFournisseur()));

        Fournisseurs fournisseur = dto.getId() != null ? findById(dto.getId()) : new Fournisseurs();
        fournisseur.setNom(dto.getNom().trim());
        fournisseur.setPrenom(dto.getPrenom() != null ? dto.getPrenom().trim() : null);
        fournisseur.setContact(dto.getContact() != null ? dto.getContact().trim() : null);
        fournisseur.setTypeFournisseurs(type);
        return fournisseursRepository.save(fournisseur);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!fournisseursRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fournisseur introuvable : " + id);
        }
        fournisseursRepository.deleteById(id);
    }
}
