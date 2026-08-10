package com.gestion.restaurant.service.commandes;

import com.gestion.restaurant.dto.commandes.CommandeCreateRequestDto;
import com.gestion.restaurant.dto.commandes.CommandeLigneRequestDto;
import com.gestion.restaurant.entity.clients.Clients;
import com.gestion.restaurant.entity.commandes.Commandes;
import com.gestion.restaurant.entity.commandes.DetailsCommandes;
import com.gestion.restaurant.entity.commandes.FacturesCommandes;
import com.gestion.restaurant.entity.ingredients.Ingredients;
import com.gestion.restaurant.entity.livraisons.ZonesLivraison;
import com.gestion.restaurant.entity.plats.Plats;
import com.gestion.restaurant.entity.plats.RecettePlats;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.clients.ClientsRepository;
import com.gestion.restaurant.repository.commandes.CommandesRepository;
import com.gestion.restaurant.repository.commandes.DetailsCommandesRepository;
import com.gestion.restaurant.repository.commandes.FacturesCommandesRepository;
import com.gestion.restaurant.repository.livraisons.ZoneLivraisonRepository;
import com.gestion.restaurant.repository.plats.PlatsRepository;
import com.gestion.restaurant.repository.recettes.RecettePlatsRepository;
import com.gestion.restaurant.service.caisse.CaisseService;
import com.gestion.restaurant.service.ingredients.IngredientsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommandesService {

    private final CommandesRepository commandesRepository;
    private final DetailsCommandesRepository detailsCommandesRepository;
    private final FacturesCommandesRepository facturesCommandesRepository;
    private final ClientsRepository clientsRepository;
    private final ZoneLivraisonRepository zoneLivraisonRepository;
    private final PlatsRepository platsRepository;
    private final com.gestion.restaurant.service.plats.PlatsService platsService;
    private final RecettePlatsRepository recettePlatsRepository;
    private final CaisseService caisseService;
    private final IngredientsService ingredientsService;

    public CommandesService(CommandesRepository commandesRepository,
                            DetailsCommandesRepository detailsCommandesRepository,
                            FacturesCommandesRepository facturesCommandesRepository,
                            ClientsRepository clientsRepository,
                            ZoneLivraisonRepository zoneLivraisonRepository,
                            PlatsRepository platsRepository,
                            RecettePlatsRepository recettePlatsRepository,
                            CaisseService caisseService,
                            IngredientsService ingredientsService,
                            com.gestion.restaurant.service.plats.PlatsService platsService) {
        this.commandesRepository = commandesRepository;
        this.detailsCommandesRepository = detailsCommandesRepository;
        this.facturesCommandesRepository = facturesCommandesRepository;
        this.clientsRepository = clientsRepository;
        this.zoneLivraisonRepository = zoneLivraisonRepository;
        this.platsRepository = platsRepository;
        this.recettePlatsRepository = recettePlatsRepository;
        this.caisseService = caisseService;
        this.ingredientsService = ingredientsService;
        this.platsService = platsService;
    }

    @Transactional(readOnly = true)
    public Page<Commandes> findAll(Pageable pageable) {
        return commandesRepository.findAllWithRelations(pageable);
    }

    @Transactional(readOnly = true)
    public Commandes findById(Long id) {
        return commandesRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec l'ID : " + id));
    }

    @Transactional(readOnly = true)
    public List<DetailsCommandes> findDetailsByCommandeId(Long commandeId) {
        return detailsCommandesRepository.findByCommandeIdWithPlat(commandeId);
    }

    @Transactional(readOnly = true)
    public List<Clients> findAllClients() {
        return clientsRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ZonesLivraison> findAllZones() {
        return zoneLivraisonRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Plats> findAllPlats() {
        return platsRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CommandeCreateRequestDto toCreateDto(Long id) {
        Commandes commande = findById(id);
        CommandeCreateRequestDto dto = new CommandeCreateRequestDto();
        dto.setId(commande.getId());
        dto.setIdClient(commande.getClient() != null ? commande.getClient().getId() : null);
        dto.setIdZoneLivraison(commande.getZoneLivraison() != null ? commande.getZoneLivraison().getId() : null);
        dto.setDateCommande(commande.getDateCommande());
        dto.setLignes(findDetailsByCommandeId(id).stream().map(d -> {
            CommandeLigneRequestDto l = new CommandeLigneRequestDto();
            l.setIdPlat(d.getPlat().getId());
            l.setQuantite(d.getQuantite());
            return l;
        }).collect(Collectors.toList()));
        return dto;
    }

    /**
     * Crée une commande (stock, facture, caisse).
     * La modification d'une commande déjà facturée est interdite pour éviter
     * les doubles déstockages / doubles encaissements.
     */
    @Transactional
    public Commandes creerCommande(CommandeCreateRequestDto dto) {
        if (dto.getId() != null) {
            throw new BusinessRuleException(
                    "Impossible de modifier une commande déjà enregistrée (stock et caisse déjà impactés). "
                            + "Supprimez-la puis créez-en une nouvelle.",
                    "/commandes");
        }
        if (dto.getIdClient() == null) {
            throw new BusinessRuleException("Le client est obligatoire.", "/commandes/new");
        }
        if (dto.getIdZoneLivraison() == null) {
            throw new BusinessRuleException("La zone de livraison est obligatoire.", "/commandes/new");
        }
        if (dto.getLignes() == null || dto.getLignes().isEmpty()) {
            throw new BusinessRuleException("La commande doit contenir au moins un plat.", "/commandes/new");
        }

        Clients client = clientsRepository.findById(dto.getIdClient())
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + dto.getIdClient()));

        ZonesLivraison zone = zoneLivraisonRepository.findById(dto.getIdZoneLivraison())
                .orElseThrow(() -> new ResourceNotFoundException("Zone de livraison introuvable : " + dto.getIdZoneLivraison()));

        LocalDate dateMvt = dto.getDateCommande() != null ? dto.getDateCommande() : LocalDate.now();

        Commandes commande = new Commandes();
        commande.setClient(client);
        commande.setZoneLivraison(zone);
        commande.setDateCommande(dateMvt);

        BigDecimal totalCumule = zone.getPrix() != null ? zone.getPrix() : BigDecimal.ZERO;
        commande.setMontantTotal(totalCumule);
        Commandes commandeSauvegardee = commandesRepository.save(commande);

        for (CommandeLigneRequestDto ligneDto : dto.getLignes()) {
            if (ligneDto.getIdPlat() == null || ligneDto.getQuantite() == null
                    || ligneDto.getQuantite().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            Plats plat = platsRepository.findById(ligneDto.getIdPlat())
                    .orElseThrow(() -> new ResourceNotFoundException("Plat introuvable : " + ligneDto.getIdPlat()));

            BigDecimal montantLigne = plat.getPrixVente().multiply(ligneDto.getQuantite());

            DetailsCommandes detail = new DetailsCommandes();
            detail.setCommande(commandeSauvegardee);
            detail.setPlat(plat);
            detail.setQuantite(ligneDto.getQuantite());
            detail.setPrixUnitaire(plat.getPrixVente());
            detail.setMontant(montantLigne);
            detailsCommandesRepository.save(detail);

            totalCumule = totalCumule.add(montantLigne);

            List<RecettePlats> recettes = recettePlatsRepository.findByPlatId(plat.getId());
            for (RecettePlats rp : recettes) {
                Ingredients ingredient = rp.getIngredient();
                BigDecimal quantiteIngredientTotale = rp.getQuantiteRequise().multiply(ligneDto.getQuantite());
                ingredientsService.enregistrerSortieOuPerte(
                        ingredient.getId(),
                        quantiteIngredientTotale,
                        IngredientsService.MVT_SORTIE_CUISINE,
                        dateMvt
                );
            }
        }

        commandeSauvegardee.setMontantTotal(totalCumule);
        Commandes commandeFinale = commandesRepository.save(commandeSauvegardee);

        FacturesCommandes facture = new FacturesCommandes();
        facture.setCommande(commandeFinale);
        facture.setDateFacture(dateMvt);
        facture.setMontantTotal(totalCumule);
        facturesCommandesRepository.save(facture);

        if (totalCumule.compareTo(BigDecimal.ZERO) > 0) {
            caisseService.enregistrerEntree(totalCumule, dateMvt);
        }

        return commandeFinale;
    }

    /** @deprecated utiliser {@link #creerCommande(CommandeCreateRequestDto)} */
    @Transactional
    public Commandes creereOuMettreAJourCommande(CommandeCreateRequestDto dto) {
        return creerCommande(dto);
    }

    @Transactional
    public void deleteById(Long id) {
        Commandes commande = findById(id);
        LocalDate dateMvt = commande.getDateCommande() != null ? commande.getDateCommande() : LocalDate.now();
        List<DetailsCommandes> details = detailsCommandesRepository.findByCommandeIdWithPlat(id);

        for (DetailsCommandes detail : details) {
            Plats plat = detail.getPlat();
            if (plat == null || detail.getQuantite() == null) {
                continue;
            }
            List<RecettePlats> recettes = recettePlatsRepository.findByPlatId(plat.getId());
            for (RecettePlats rp : recettes) {
                BigDecimal quantiteIngredientTotale = rp.getQuantiteRequise().multiply(detail.getQuantite());
                ingredientsService.reintegrerStock(rp.getIngredient().getId(), quantiteIngredientTotale, dateMvt);
            }
        }

        if (commande.getMontantTotal() != null && commande.getMontantTotal().compareTo(BigDecimal.ZERO) > 0) {
            caisseService.enregistrerSortie(commande.getMontantTotal(), dateMvt);
        }

        facturesCommandesRepository.deleteByCommande_Id(id);
        detailsCommandesRepository.deleteByCommandeId(id);
        commandesRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public java.math.BigDecimal computeProfitForCommande(Long commandeId) {
        Commandes commande = findById(commandeId);
        java.math.BigDecimal montant = commande.getMontantTotal() != null ? commande.getMontantTotal() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal coutTotal = java.math.BigDecimal.ZERO;
        List<DetailsCommandes> details = findDetailsByCommandeId(commandeId);
        for (DetailsCommandes d : details) {
            if (d.getPlat() == null || d.getQuantite() == null) continue;
            java.math.BigDecimal coutUnitaire = platsService.calculatePrixAchatPlat(d.getPlat().getId());
            coutTotal = coutTotal.add(coutUnitaire.multiply(d.getQuantite()));
        }
        return montant.subtract(coutTotal);
    }
}
