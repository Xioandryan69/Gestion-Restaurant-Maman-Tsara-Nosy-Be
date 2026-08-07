package com.gestion.restaurant.service.personnels;

import com.gestion.restaurant.dto.personnels.*;
import com.gestion.restaurant.entity.personnels.*;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.personnels.*;
import com.gestion.restaurant.service.caisse.CaisseService;
import com.gestion.restaurant.specification.personnels.PersonnelsSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PersonnelsService {

    private final PersonnelsRepository personnelsRepository;
    private final RolePersonnelsRepository roleRepo;
    private final FichePaieRepository fichePaieRepo;
    private final AbsencePersonnelsRepository absenceRepo;
    private final RaisonAbsenceRepository raisonAbsenceRepo;
    private final CaisseService caisseService;

    public PersonnelsService(PersonnelsRepository personnelsRepository,
                             RolePersonnelsRepository roleRepo,
                             FichePaieRepository fichePaieRepo,
                             AbsencePersonnelsRepository absenceRepo,
                             RaisonAbsenceRepository raisonAbsenceRepo,
                             CaisseService caisseService) {
        this.personnelsRepository = personnelsRepository;
        this.roleRepo = roleRepo;
        this.fichePaieRepo = fichePaieRepo;
        this.absenceRepo = absenceRepo;
        this.raisonAbsenceRepo = raisonAbsenceRepo;
        this.caisseService = caisseService;
    }

    @Transactional(readOnly = true)
    public Page<PersonnelResponseDto> search(PersonnelSearchCriteria criteria, Pageable pageable) {
        Specification<Personnels> spec = PersonnelsSpecification.withFilters(criteria);
        return personnelsRepository.findAll(spec, pageable).map(PersonnelMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Personnels findById(Long id) {
        return personnelsRepository.findByIdWithRole(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable avec l'ID : " + id));
    }

    @Transactional(readOnly = true)
    public PersonnelRequestDto findDtoById(Long id) {
        Personnels p = findById(id);
        PersonnelRequestDto dto = new PersonnelRequestDto();
        dto.setId(p.getId());
        dto.setNom(p.getNom());
        dto.setPrenom(p.getPrenom());
        dto.setContact(p.getContact());
        dto.setDateEmbauche(p.getDateEmbauche());
        if (p.getRolePersonnels() != null) dto.setIdRole(p.getRolePersonnels().getId());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<RolePersonnels> findAllRoles() {
        return roleRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<RaisonAbsence> findAllRaisonsAbsence() {
        return raisonAbsenceRepo.findAll();
    }

    @Transactional
    public PersonnelResponseDto saveFromDto(PersonnelRequestDto dto) {
        if (dto.getNom() == null || dto.getNom().isBlank()) {
            throw new BusinessRuleException("Le nom du personnel est obligatoire.");
        }
        if (dto.getIdRole() == null) {
            throw new BusinessRuleException("Le rôle est obligatoire.");
        }

        Personnels p = (dto.getId() != null) ? findById(dto.getId()) : new Personnels();
        RolePersonnels role = roleRepo.findById(dto.getIdRole())
                .orElseThrow(() -> new ResourceNotFoundException("Rôle introuvable : " + dto.getIdRole()));

        p.setNom(dto.getNom());
        p.setPrenom(dto.getPrenom());
        p.setContact(dto.getContact());
        p.setDateEmbauche(dto.getDateEmbauche() != null ? dto.getDateEmbauche() : LocalDate.now());
        p.setRolePersonnels(role);

        return PersonnelMapper.toDto(personnelsRepository.save(p));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!personnelsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employé introuvable avec l'ID : " + id);
        }
        personnelsRepository.deleteById(id);
    }

    // ───────────────────────── Paie & Salaire ─────────────────────────

    @Transactional
    public FichePaie genererFichePaie(Long idPersonnel, BigDecimal salaire, BigDecimal prime, LocalDate datePaie) {
        Personnels p = findById(idPersonnel);
        
        if (salaire == null || salaire.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Le salaire de base doit être supérieur à zéro.");
        }

        BigDecimal total = salaire.add(prime != null ? prime : BigDecimal.ZERO);
        LocalDate date = (datePaie != null) ? datePaie : LocalDate.now();

        FichePaie fp = new FichePaie();
        fp.setPersonnel(p);
        fp.setDatePaie(date);
        fp.setSalaire(salaire);
        fp.setMontantTotal(total);

        FichePaie saved = fichePaieRepo.save(fp);

        // Sortie d'argent automatique en Caisse (Paiement des salaires)
        caisseService.enregistrerSortie(total, date);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<FichePaie> findHistoriquePaie(Long idPersonnel) {
        return fichePaieRepo.findByPersonnel_IdOrderByDatePaieDesc(idPersonnel);
    }

    // ───────────────────────── Absences ─────────────────────────

    @Transactional
    public AbsencePersonnels enregistrerAbsence(Long idPersonnel, LocalDate dateDebut, LocalDate dateFin, Long idRaison, String commentaire) {
        Personnels p = findById(idPersonnel);

        if (dateDebut == null || dateFin == null) {
            throw new BusinessRuleException("Les dates de début et de fin d'absence sont requises.");
        }
        if (dateFin.isBefore(dateDebut)) {
            throw new BusinessRuleException("La date de fin ne peut pas être antérieure à la date de début.");
        }

        RaisonAbsence raison = raisonAbsenceRepo.findById(idRaison)
                .orElseThrow(() -> new ResourceNotFoundException("Motif d'absence introuvable : " + idRaison));

        AbsencePersonnels abs = new AbsencePersonnels();
        abs.setPersonnel(p);
        abs.setDateDebut(dateDebut);
        abs.setDateFin(dateFin);
        abs.setRaisonAbsence(raison);
        abs.setCommentaire(commentaire);

        return absenceRepo.save(abs);
    }

    @Transactional(readOnly = true)
    public List<AbsencePersonnels> findAbsences(Long idPersonnel) {
        return absenceRepo.findByPersonnel_IdOrderByDateDebutDesc(idPersonnel);
    }
}