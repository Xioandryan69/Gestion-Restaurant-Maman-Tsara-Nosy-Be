package com.gestion.restaurant.functional;

import com.gestion.restaurant.entity.caisse.TypeMouvementCaisse;
import com.gestion.restaurant.entity.clients.TypeClient;
import com.gestion.restaurant.entity.fournisseurs.Fournisseurs;
import com.gestion.restaurant.entity.fournisseurs.TypeFournisseurs;
import com.gestion.restaurant.entity.ingredients.Ingredients;
import com.gestion.restaurant.entity.livraisons.ZonesLivraison;
import com.gestion.restaurant.entity.materielles.Materielles;
import com.gestion.restaurant.entity.personnels.Personnels;
import com.gestion.restaurant.entity.plats.Plats;
import com.gestion.restaurant.entity.plats.RecettePlats;
import com.gestion.restaurant.repository.caisse.TypeMouvementCaisseRepository;
import com.gestion.restaurant.repository.commandes.CommandesRepository;
import com.gestion.restaurant.service.caisse.CaisseService;
import com.gestion.restaurant.service.recette.RecetteService;
import com.gestion.restaurant.support.AbstractPostgresIT;
import com.gestion.restaurant.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "admin", roles = "ADMIN")
@Transactional
class ModulesFunctionalTest extends AbstractPostgresIT {

    @Autowired MockMvc mockMvc;
    @Autowired TestDataFactory factory;
    @Autowired TypeMouvementCaisseRepository typeMouvementCaisseRepository;
    @Autowired CommandesRepository commandesRepository;
    @Autowired CaisseService caisseService;
    @Autowired RecetteService recetteService;

    private TypeClient typeClient;
    private TypeFournisseurs typeFournisseur;
    private Fournisseurs fournisseur;
    private Ingredients ingredient;
    private Plats plat;
    private ZonesLivraison zone;
    private Personnels personnel;
    private Materielles materiel;

    @BeforeEach
    void setUp() {
        factory.ensureLookups();
        typeClient = factory.typeClient("Part-" + System.nanoTime());
        typeFournisseur = factory.typeFournisseur("TF-" + System.nanoTime());
        fournisseur = factory.fournisseur("Fourn", typeFournisseur);
        var unite = factory.unite("u-" + System.nanoTime(), "g");
        var catI = factory.categorieIngredient("CI-" + System.nanoTime());
        var stI = factory.statutIngredient("SI-" + System.nanoTime());
        ingredient = factory.ingredient("IngF", catI, stI, fournisseur, unite);
        factory.avecStock(ingredient, new BigDecimal("50"));
        var catP = factory.categoriePlat("CP-" + System.nanoTime());
        plat = factory.plat("PlatF", new BigDecimal("10000"), catP);
        factory.recette(plat, ingredient, BigDecimal.ONE);
        zone = factory.zone("Zone-" + System.nanoTime(), new BigDecimal("1500"));
        personnel = factory.personnel("PersF", factory.role("Role-" + System.nanoTime()));
        factory.raisonAbsence("Raison-" + System.nanoTime());
        materiel = factory.materiel("MatF", factory.categorieMateriel("CM-" + System.nanoTime()),
                factory.statutMateriel("En service"));
    }

    @Test
    void dashboard() throws Exception {
        mockMvc.perform(get("/dashboard")).andExpect(status().isOk());
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    void clients_crud() throws Exception {
        mockMvc.perform(get("/clients")).andExpect(status().isOk());
        mockMvc.perform(get("/clients/new")).andExpect(status().isOk());
        mockMvc.perform(post("/clients/save").with(csrf())
                        .param("nom", "ClientFunc")
                        .param("prenom", "Test")
                        .param("contact", "032")
                        .param("idTypeClient", typeClient.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"))
                .andExpect(flash().attributeExists("successMessage"));

        mockMvc.perform(post("/clients/save").with(csrf())
                        .param("nom", "")
                        .param("idTypeClient", typeClient.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("clients/form"));
    }

    @Test
    void fournisseurs_crud() throws Exception {
        mockMvc.perform(get("/fournisseurs")).andExpect(status().isOk());
        mockMvc.perform(get("/fournisseurs/new")).andExpect(status().isOk());
        mockMvc.perform(post("/fournisseurs/save").with(csrf())
                        .param("nom", "FourFunc")
                        .param("prenom", "Y")
                        .param("contact", "033")
                        .param("idTypeFournisseur", typeFournisseur.getId().toString()))
                .andExpect(redirectedUrl("/fournisseurs"));
        mockMvc.perform(get("/fournisseurs/edit/" + fournisseur.getId())).andExpect(status().isOk());
    }

    @Test
    void zones_crud() throws Exception {
        mockMvc.perform(get("/zones-livraison")).andExpect(status().isOk());
        mockMvc.perform(get("/zones-livraison/creer")).andExpect(status().isOk());
        mockMvc.perform(post("/zones-livraison/enregistrer").with(csrf())
                        .param("libelle", "ZoneFunc-" + System.nanoTime())
                        .param("min", "0")
                        .param("max", "10")
                        .param("prix", "2000"))
                .andExpect(redirectedUrl("/zones-livraison"));
        mockMvc.perform(get("/zones-livraison/modifier/" + zone.getId())).andExpect(status().isOk());
        mockMvc.perform(post("/zones-livraison/supprimer/" + zone.getId()).with(csrf()))
                .andExpect(redirectedUrl("/zones-livraison"));
    }

    @Test
    void ingredients_stock_achat_sortie() throws Exception {
        mockMvc.perform(get("/ingredients")).andExpect(status().isOk());
        mockMvc.perform(get("/ingredients/new")).andExpect(status().isOk());
        mockMvc.perform(get("/ingredients/" + ingredient.getId() + "/detail")).andExpect(status().isOk());
        mockMvc.perform(get("/ingredients/stock")).andExpect(status().isOk());
        mockMvc.perform(get("/stocks")).andExpect(redirectedUrl("/ingredients/stock"));

        mockMvc.perform(post("/ingredients/" + ingredient.getId() + "/achat/save").with(csrf())
                        .param("quantite", "5")
                        .param("prixAchat", "100")
                        .param("dateEntree", LocalDate.now().toString()))
                .andExpect(redirectedUrl("/ingredients/" + ingredient.getId() + "/detail"));

        mockMvc.perform(post("/ingredients/" + ingredient.getId() + "/sortie/save").with(csrf())
                        .param("quantite", "1")
                        .param("typeMouvement", "Perte / Périmé")
                        .param("dateMvt", LocalDate.now().toString()))
                .andExpect(redirectedUrl("/ingredients/" + ingredient.getId() + "/detail"));
    }

    @Test
    void plats_et_recettes() throws Exception {
        mockMvc.perform(get("/plats")).andExpect(status().isOk());
        mockMvc.perform(get("/plats/new")).andExpect(status().isOk());
        mockMvc.perform(post("/plats/save-multiple").with(csrf())
                        .param("plats[0].nom", "NouveauPlat")
                        .param("plats[0].idCategorie", plat.getCategoriePlats().getId().toString())
                        .param("plats[0].prixVente", "9000")
                        .param("plats[0].ingredients[0].idIngredient", ingredient.getId().toString())
                        .param("plats[0].ingredients[0].quantiteRequise", "2"))
                .andExpect(redirectedUrl("/plats"));

        mockMvc.perform(get("/recettes/plat/" + plat.getId())).andExpect(status().isOk());
        mockMvc.perform(post("/recettes/ajouter").with(csrf())
                        .param("idPlat", plat.getId().toString())
                        .param("idIngredient", ingredient.getId().toString())
                        .param("quantiteRequise", "0.25"))
                .andExpect(redirectedUrl("/recettes/plat/" + plat.getId()));

        List<RecettePlats> lignes = recetteService.getIngredientsParPlat(plat.getId());
        Long idRecette = lignes.getLast().getId();
        mockMvc.perform(post("/recettes/supprimer/" + idRecette).with(csrf())
                        .param("idPlat", plat.getId().toString()))
                .andExpect(redirectedUrl("/recettes/plat/" + plat.getId()));
    }

    @Test
    void commandes_parcours() throws Exception {
        mockMvc.perform(get("/commandes")).andExpect(status().isOk());
        mockMvc.perform(get("/commandes/new")).andExpect(status().isOk());

        var client = factory.client("CmdClient", typeClient);
        mockMvc.perform(post("/commandes/save").with(csrf())
                        .param("idClient", client.getId().toString())
                        .param("idZoneLivraison", zone.getId().toString())
                        .param("dateCommande", LocalDate.now().toString())
                        .param("lignes[0].idPlat", plat.getId().toString())
                        .param("lignes[0].quantite", "1"))
                .andExpect(redirectedUrl("/commandes"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void caisse_crud() throws Exception {
        factory.ensureLookups();
        TypeMouvementCaisse type = typeMouvementCaisseRepository.findByLibelle("Entree").orElseThrow();
        mockMvc.perform(get("/caisse")).andExpect(status().isOk());
        mockMvc.perform(get("/caisse/new")).andExpect(status().isOk());
        mockMvc.perform(post("/caisse/save").with(csrf())
                        .param("dateMouvement", LocalDate.now().toString())
                        .param("montant", "5000")
                        .param("idTypeMouvement", type.getId().toString()))
                .andExpect(redirectedUrl("/caisse"));

        var mvt = caisseService.findAll(
                org.springframework.data.domain.PageRequest.of(0, 1,
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")))
                .getContent().getFirst();
        mockMvc.perform(get("/caisse/edit/" + mvt.getId())).andExpect(status().isOk());
        mockMvc.perform(post("/caisse/delete/" + mvt.getId()).with(csrf()))
                .andExpect(redirectedUrl("/caisse"));
    }

    @Test
    void personnels_paie_absence() throws Exception {
        mockMvc.perform(get("/personnels")).andExpect(status().isOk());
        mockMvc.perform(get("/personnels/new")).andExpect(status().isOk());
        mockMvc.perform(get("/personnels/" + personnel.getId() + "/detail")).andExpect(status().isOk());

        mockMvc.perform(post("/personnels/" + personnel.getId() + "/paie/save").with(csrf())
                        .param("salaire", "300000")
                        .param("prime", "10000")
                        .param("datePaie", LocalDate.now().toString()))
                .andExpect(redirectedUrl("/personnels/" + personnel.getId() + "/detail"));

        var raison = factory.raisonAbsence("Abs-" + System.nanoTime());
        mockMvc.perform(post("/personnels/" + personnel.getId() + "/absence/save").with(csrf())
                        .param("dateDebut", LocalDate.now().toString())
                        .param("dateFin", LocalDate.now().plusDays(1).toString())
                        .param("idRaison", raison.getId().toString())
                        .param("commentaire", "test"))
                .andExpect(redirectedUrl("/personnels/" + personnel.getId() + "/detail"));
    }

    @Test
    void materielles_actions() throws Exception {
        mockMvc.perform(get("/materielles")).andExpect(status().isOk());
        mockMvc.perform(get("/materielles/" + materiel.getId() + "/detail")).andExpect(status().isOk());
        mockMvc.perform(post("/materielles/" + materiel.getId() + "/historique/save").with(csrf())
                        .param("quantite", "1")
                        .param("prixAchat", "50000")
                        .param("dateEntree", LocalDate.now().toString()))
                .andExpect(redirectedUrl("/materielles/" + materiel.getId() + "/detail"));
        mockMvc.perform(post("/materielles/" + materiel.getId() + "/maintenance/save").with(csrf())
                        .param("description", "Check")
                        .param("cout", "1000")
                        .param("technicien", "T")
                        .param("dateMaintenance", LocalDate.now().toString()))
                .andExpect(redirectedUrl("/materielles/" + materiel.getId() + "/detail"));
        mockMvc.perform(post("/materielles/" + materiel.getId() + "/hors-service").with(csrf()))
                .andExpect(redirectedUrl("/materielles/" + materiel.getId() + "/detail"));
    }

    @Test
    void commande_edit_bloque_et_detail() throws Exception {
        var scenario = factory.commandeScenario();
        mockMvc.perform(post("/commandes/save").with(csrf())
                        .param("idClient", scenario.client().getId().toString())
                        .param("idZoneLivraison", scenario.zone().getId().toString())
                        .param("lignes[0].idPlat", scenario.plat().getId().toString())
                        .param("lignes[0].quantite", "1"))
                .andExpect(redirectedUrl("/commandes"));

        Long commandeId = commandesRepository.findAll().getLast().getId();
        mockMvc.perform(get("/commandes/" + commandeId + "/detail")).andExpect(status().isOk());
        mockMvc.perform(get("/commandes/edit/" + commandeId))
                .andExpect(redirectedUrl("/commandes/" + commandeId + "/detail"))
                .andExpect(flash().attributeExists("errorMessage"));
        mockMvc.perform(post("/commandes/delete/" + commandeId).with(csrf()))
                .andExpect(redirectedUrl("/commandes"));
    }
}
