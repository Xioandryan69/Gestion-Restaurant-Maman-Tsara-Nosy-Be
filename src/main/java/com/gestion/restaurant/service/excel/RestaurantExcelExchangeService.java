package com.gestion.restaurant.service.excel;

import com.gestion.restaurant.entity.caisse.MouvementCaisse;
import com.gestion.restaurant.entity.caisse.TypeMouvementCaisse;
import com.gestion.restaurant.entity.clients.Clients;
import com.gestion.restaurant.entity.clients.TypeClient;
import com.gestion.restaurant.entity.fournisseurs.Fournisseurs;
import com.gestion.restaurant.entity.fournisseurs.TypeFournisseurs;
import com.gestion.restaurant.entity.ingredients.CategorieIngredients;
import com.gestion.restaurant.entity.ingredients.Ingredients;
import com.gestion.restaurant.entity.commandes.Commandes;
import com.gestion.restaurant.entity.livraisons.ZonesLivraison;
import com.gestion.restaurant.entity.materielles.CategorieMaterielles;
import com.gestion.restaurant.entity.materielles.Materielles;
import com.gestion.restaurant.entity.materielles.StatutMaterielles;

import com.gestion.restaurant.repository.commandes.CommandesRepository;
import com.gestion.restaurant.repository.livraisons.ZoneLivraisonRepository;
import com.gestion.restaurant.repository.materielles.CategorieMateriellesRepository;
import com.gestion.restaurant.repository.materielles.MateriellesRepository;
import com.gestion.restaurant.repository.materielles.StatutMateriellesRepository;
import com.gestion.restaurant.entity.personnels.Personnels;
import com.gestion.restaurant.entity.personnels.RolePersonnels;
import com.gestion.restaurant.entity.plats.CategoriePlats;
import com.gestion.restaurant.entity.plats.Plats;
import com.gestion.restaurant.repository.caisse.MouvementCaisseRepository;
import com.gestion.restaurant.repository.caisse.TypeMouvementCaisseRepository;
import com.gestion.restaurant.repository.clients.ClientsRepository;
import com.gestion.restaurant.repository.clients.TypeClientRepository;
import com.gestion.restaurant.repository.fournisseur.FournisseursRepository;
import com.gestion.restaurant.repository.fournisseur.TypeFournisseursRepository;
import com.gestion.restaurant.repository.ingredients.CategorieIngredientsRepository;
import com.gestion.restaurant.repository.ingredients.IngredientsRepository;
import com.gestion.restaurant.repository.personnels.PersonnelsRepository;
import com.gestion.restaurant.repository.personnels.RolePersonnelsRepository;
import com.gestion.restaurant.repository.plats.CategoriePlatsRepository;
import com.gestion.restaurant.repository.plats.PlatsRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import com.gestion.restaurant.entity.commandes.Commandes;
import com.gestion.restaurant.entity.commandes.DetailsCommandes;
import com.gestion.restaurant.entity.commandes.FacturesCommandes;
import com.gestion.restaurant.entity.livraisons.ZonesLivraison;

import com.gestion.restaurant.repository.commandes.CommandesRepository;
import com.gestion.restaurant.repository.commandes.DetailsCommandesRepository;
import com.gestion.restaurant.repository.commandes.FacturesCommandesRepository;
import com.gestion.restaurant.entity.commandes.FacturesCommandes;
import com.gestion.restaurant.repository.commandes.DetailsCommandesRepository;

@Service
@Transactional
public class RestaurantExcelExchangeService {

    public record ImportSummary(int importedRows, int duplicates, int sheetsProcessed) {
        public int total() {
            return importedRows + duplicates;
        }
    }

    private final ClientsRepository clientsRepository;
    private final TypeClientRepository typeClientRepository;
    private final FournisseursRepository fournisseursRepository;
    private final TypeFournisseursRepository typeFournisseursRepository;
    private final PersonnelsRepository personnelsRepository;
    private final RolePersonnelsRepository rolePersonnelsRepository;
    private final CategoriePlatsRepository categoriePlatsRepository;
    private final PlatsRepository platsRepository;
    private final MouvementCaisseRepository mouvementCaisseRepository;
    private final TypeMouvementCaisseRepository typeMouvementCaisseRepository;
    private final CategorieIngredientsRepository categorieIngredientsRepository;
    private final IngredientsRepository ingredientsRepository;
    private final CommandesRepository commandesRepository;
    private final ZoneLivraisonRepository zoneLivraisonRepository;

    private final CategorieMateriellesRepository categorieMateriellesRepository;
    private final MateriellesRepository materiellesRepository;
    private final StatutMateriellesRepository statutMateriellesRepository;
    private final FacturesCommandesRepository facturesCommandesRepository;
    private final DetailsCommandesRepository detailsCommandesRepository;

    public RestaurantExcelExchangeService(
            ClientsRepository clientsRepository,
            TypeClientRepository typeClientRepository,
            FournisseursRepository fournisseursRepository,
            TypeFournisseursRepository typeFournisseursRepository,
            PersonnelsRepository personnelsRepository,
            RolePersonnelsRepository rolePersonnelsRepository,
            CategoriePlatsRepository categoriePlatsRepository,
            PlatsRepository platsRepository,
            MouvementCaisseRepository mouvementCaisseRepository,
            TypeMouvementCaisseRepository typeMouvementCaisseRepository,
            CategorieIngredientsRepository categorieIngredientsRepository,
            IngredientsRepository ingredientsRepository,
            CommandesRepository commandesRepository,
            ZoneLivraisonRepository zoneLivraisonRepository,
            CategorieMateriellesRepository categorieMateriellesRepository,
            MateriellesRepository materiellesRepository,
            StatutMateriellesRepository statutMateriellesRepository,
            FacturesCommandesRepository facturesCommandesRepository,
            DetailsCommandesRepository detailsCommandesRepository) {

        this.clientsRepository = clientsRepository;
        this.typeClientRepository = typeClientRepository;
        this.fournisseursRepository = fournisseursRepository;
        this.typeFournisseursRepository = typeFournisseursRepository;
        this.personnelsRepository = personnelsRepository;
        this.rolePersonnelsRepository = rolePersonnelsRepository;
        this.categoriePlatsRepository = categoriePlatsRepository;
        this.platsRepository = platsRepository;
        this.mouvementCaisseRepository = mouvementCaisseRepository;
        this.typeMouvementCaisseRepository = typeMouvementCaisseRepository;
        this.categorieIngredientsRepository = categorieIngredientsRepository;
        this.ingredientsRepository = ingredientsRepository;

        this.commandesRepository = commandesRepository;
        this.zoneLivraisonRepository = zoneLivraisonRepository;

        this.categorieMateriellesRepository = categorieMateriellesRepository;
        this.materiellesRepository = materiellesRepository;
        this.statutMateriellesRepository = statutMateriellesRepository;
        this.facturesCommandesRepository = facturesCommandesRepository;
        this.detailsCommandesRepository = detailsCommandesRepository;
    }

    public void exportClients(HttpServletResponse response) throws IOException {
        writeWorkbook(response, "clients.xlsx", workbook -> {
            Sheet sheet = workbook.createSheet("Clients");
            writeHeader(sheet, "nom", "prenom", "contact", "typeClient");
            int rowIndex = 1;
            for (Clients client : clientsRepository.findAll()) {
                Row row = sheet.createRow(rowIndex++);
                writeRow(row, client.getNom(), client.getPrenom(), client.getContact(),
                        client.getTypeClient() != null ? client.getTypeClient().getLibelle() : "");
            }
        });
    }

    public void exportFournisseurs(HttpServletResponse response) throws IOException {
        writeWorkbook(response, "fournisseurs.xlsx", workbook -> {
            Sheet sheet = workbook.createSheet("Fournisseurs");
            writeHeader(sheet, "typeFournisseurs", "nom", "prenom", "contact");
            int rowIndex = 1;
            for (Fournisseurs fournisseur : fournisseursRepository.findAllWithTypeList()) {
                Row row = sheet.createRow(rowIndex++);
                writeRow(row,
                        fournisseur.getTypeFournisseurs() != null ? fournisseur.getTypeFournisseurs().getLibelle() : "",
                        fournisseur.getNom(),
                        fournisseur.getPrenom(),
                        fournisseur.getContact());
            }
        });
    }

    public void exportPersonnels(HttpServletResponse response) throws IOException {
        writeWorkbook(response, "personnels.xlsx", workbook -> {
            Sheet sheet = workbook.createSheet("Personnels");
            writeHeader(sheet, "rolePersonnel", "nom", "prenom", "contact", "dateEmbauche");
            int rowIndex = 1;
            for (Personnels personnel : personnelsRepository.findAll((root, query, cb) -> cb.conjunction(),
                    org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getContent()) {
                Row row = sheet.createRow(rowIndex++);
                writeRow(row,
                        personnel.getRolePersonnels() != null ? personnel.getRolePersonnels().getLibelle() : "",
                        personnel.getNom(),
                        personnel.getPrenom(),
                        personnel.getContact(),
                        personnel.getDateEmbauche() != null ? personnel.getDateEmbauche().toString() : "");
            }
        });
    }

    public void exportCaisse(HttpServletResponse response) throws IOException {
        writeWorkbook(response, "caisse.xlsx", workbook -> {
            Sheet sheet = workbook.createSheet("Caisse");
            writeHeader(sheet, "dateMouvement", "typeMouvement", "montant");
            int rowIndex = 1;
            for (MouvementCaisse mouvement : mouvementCaisseRepository
                    .findAllWithType(org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent()) {
                Row row = sheet.createRow(rowIndex++);
                writeRow(row,
                        mouvement.getDateMouvement() != null ? mouvement.getDateMouvement().toString() : "",
                        mouvement.getTypeMouvement() != null ? mouvement.getTypeMouvement().getLibelle() : "",
                        mouvement.getMontant() != null ? mouvement.getMontant().toPlainString() : "0");
            }
        });
    }

    public void exportPlats(HttpServletResponse response) throws IOException {
        writeWorkbook(response, "plats.xlsx", workbook -> {
            Sheet sheet = workbook.createSheet("Plats");
            writeHeader(sheet, "nom", "categoriePlats", "prixVente");
            int rowIndex = 1;
            for (Plats plat : platsRepository.findAll()) {
                Row row = sheet.createRow(rowIndex++);
                writeRow(row,
                        plat.getNom(),
                        plat.getCategoriePlats() != null ? plat.getCategoriePlats().getLibelle() : "",
                        plat.getPrixVente() != null ? plat.getPrixVente().toPlainString() : "0");
            }
        });
    }

    public ImportSummary importClients(MultipartFile file) throws IOException {
        return importSimple(file, "Clients", row -> {
            String typeLabel = required(row, "typeclient", "type client");
            TypeClient type = resolve(typeLabel, typeClientRepository.findAll(), TypeClient::new,
                    t -> t.setLibelle(typeLabel.trim()), typeClientRepository::save, TypeClient::getLibelle);
            Clients client = new Clients();
            client.setNom(required(row, "nom"));
            client.setPrenom(required(row, "prenom"));
            client.setContact(required(row, "contact"));
            client.setTypeClient(type);
            return clientsRepository.save(client);
        });
    }

    public ImportSummary importFournisseurs(MultipartFile file) throws IOException {
        return importSimple(file, "Fournisseurs", row -> {
            String typeLabel = required(row, "typefournisseurs", "type fournisseurs");
            TypeFournisseurs type = resolve(typeLabel, typeFournisseursRepository.findAll(), TypeFournisseurs::new,
                    t -> t.setLibelle(typeLabel.trim()), typeFournisseursRepository::save,
                    TypeFournisseurs::getLibelle);
            Fournisseurs fournisseur = new Fournisseurs();
            fournisseur.setTypeFournisseurs(type);
            fournisseur.setNom(required(row, "nom"));
            fournisseur.setPrenom(required(row, "prenom"));
            fournisseur.setContact(required(row, "contact"));
            return fournisseursRepository.save(fournisseur);
        });
    }

    public ImportSummary importPersonnels(MultipartFile file) throws IOException {
        return importSimple(file, "Personnels", row -> {
            String roleLabel = required(row, "rolepersonnel", "role personnel");
            RolePersonnels role = resolve(roleLabel, rolePersonnelsRepository.findAll(), RolePersonnels::new,
                    r -> r.setLibelle(roleLabel.trim()), rolePersonnelsRepository::save, RolePersonnels::getLibelle);
            Personnels personnel = new Personnels();
            personnel.setRolePersonnels(role);
            personnel.setNom(required(row, "nom"));
            personnel.setPrenom(required(row, "prenom"));
            personnel.setContact(required(row, "contact"));
            String date = required(row, "dateembauche", "date embauche");
            personnel.setDateEmbauche(LocalDate.parse(date));
            return personnelsRepository.save(personnel);
        });
    }

    public ImportSummary importCaisse(MultipartFile file) throws IOException {
        return importSimple(file, "Caisse", row -> {
            TypeMouvementCaisse type = resolve(required(row, "typemouvement", "type mouvement"),
                    typeMouvementCaisseRepository.findAll(), TypeMouvementCaisse::new,
                    t -> t.setLibelle(required(row, "typemouvement", "type mouvement").trim()),
                    typeMouvementCaisseRepository::save, TypeMouvementCaisse::getLibelle);
            MouvementCaisse mouvement = new MouvementCaisse();
            mouvement.setDateMouvement(LocalDate.parse(required(row, "datemouvement", "date mouvement")));
            mouvement.setMontant(new BigDecimal(required(row, "montant")));
            mouvement.setTypeMouvement(type);
            return mouvementCaisseRepository.save(mouvement);
        });
    }

    public ImportSummary importPlats(MultipartFile file) throws IOException {
        return importSimple(file, "Plats", row -> {
            CategoriePlats categorie = resolve(required(row, "categorieplats", "categorie plats"),
                    categoriePlatsRepository.findAll(), CategoriePlats::new,
                    c -> c.setLibelle(required(row, "categorieplats", "categorie plats").trim()),
                    categoriePlatsRepository::save, CategoriePlats::getLibelle);
            Plats plat = new Plats();
            plat.setNom(required(row, "nom"));
            plat.setCategoriePlats(categorie);
            plat.setPrixVente(new BigDecimal(required(row, "prixvente", "prix vente")));
            return platsRepository.save(plat);
        });
    }

    public Resource template(String resourcePath) {
        return new ClassPathResource(resourcePath);
    }

    private <T> ImportSummary importSimple(MultipartFile file, String sheetName, RowSaver<T> rowSaver)
            throws IOException {
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("Le fichier Excel ne contient aucune feuille.");
            }
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                throw new IllegalArgumentException("Feuille introuvable : " + sheetName);
            }
            int imported = 0;
            int duplicates = 0;
            for (Row row : sheet) {
                if (row.getRowNum() == 0 || isRowEmpty(row)) {
                    continue;
                }
                rowSaver.save(row);
                imported++;
            }
            return new ImportSummary(imported, duplicates, 1);
        }
    }

    private <T> T resolve(String label, Iterable<T> existing, Factory<T> factory, Setter<T> setter, Saver<T> saver,
            LabelGetter<T> getter) {
        String normalized = normalize(label);
        for (T item : existing) {
            if (normalize(getter.get(item)).equals(normalized)) {
                return item;
            }
        }
        T created = factory.create();
        setter.set(created);
        return saver.save(created);
    }

    private void writeWorkbook(HttpServletResponse response, String filename, WorkbookWriter writer)
            throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            writer.write(workbook);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8));
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        }
    }

    private void writeHeader(Sheet sheet, String... columns) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            writeCell(row, i, columns[i]);
        }
    }

    private void writeRow(Row row, String... values) {
        for (int i = 0; i < values.length; i++) {
            writeCell(row, i, values[i]);
        }
    }

    private void writeCell(Row row, int index, String value) {
        Cell cell = row.createCell(index, CellType.STRING);
        cell.setCellValue(value != null ? value : "");
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && !new DataFormatter().formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String required(Row row, String... headers) {
        String value = optional(row, headers);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Valeur manquante pour: " + String.join(", ", headers));
        }
        return value.trim();
    }

    private String optional(Row row, String... headers) {
        Row headerRow = row.getSheet().getRow(0);
        if (headerRow == null) {
            return null;
        }
        Map<String, Integer> indexes = new HashMap<>();
        DataFormatter formatter = new DataFormatter();
        for (Cell cell : headerRow) {
            indexes.put(normalize(formatter.formatCellValue(cell)), cell.getColumnIndex());
        }
        for (String header : headers) {
            Integer index = indexes.get(normalize(header));
            if (index != null) {
                return formatter.formatCellValue(row.getCell(index));
            }
        }
        return null;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    public ImportSummary importMaterielles(MultipartFile file) throws IOException {
        return importSimple(file, "Materielles", row -> {

            String categorieLabel = required(
                    row,
                    "categorieMaterielles",
                    "categorie materielle",
                    "categorie materiels");

            CategorieMaterielles categorie = resolve(
                    categorieLabel,
                    categorieMateriellesRepository.findAll(),
                    CategorieMaterielles::new,
                    c -> c.setLibelle(categorieLabel.trim()),
                    categorieMateriellesRepository::save,
                    CategorieMaterielles::getLibelle);

            String statutLabel = required(
                    row,
                    "statutMaterielles",
                    "statut materielle",
                    "statut materiels");

            StatutMaterielles statut = resolve(
                    statutLabel,
                    statutMateriellesRepository.findAll(),
                    StatutMaterielles::new,
                    s -> s.setLibelle(statutLabel.trim()),
                    statutMateriellesRepository::save,
                    StatutMaterielles::getLibelle);

            Materielles materiel = new Materielles();

            materiel.setNom(
                    required(row, "nom"));

            materiel.setDateEntree(
                    LocalDate.parse(
                            required(row, "dateEntree", "date entree")));

            materiel.setCategorieMaterielles(categorie);
            materiel.setStatutMaterielles(statut);

            return materiellesRepository.save(materiel);
        });
    }

    public ImportSummary importCommandes(MultipartFile file) throws IOException {

        try (InputStream inputStream = file.getInputStream();
                Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet commandesSheet = workbook.getSheet("Commandes");
            Sheet detailsSheet = workbook.getSheet("DetailsCommandes");
            Sheet facturesSheet = workbook.getSheet("FactureCommandes");

            if (commandesSheet == null) {
                throw new IllegalArgumentException(
                        "Feuille introuvable : Commandes");
            }

            if (detailsSheet == null) {
                throw new IllegalArgumentException(
                        "Feuille introuvable : DetailsCommandes");
            }

            if (facturesSheet == null) {
                throw new IllegalArgumentException(
                        "Feuille introuvable : FactureCommandes");
            }

            /*
             * Map utilisée pour retrouver une commande
             * depuis la colonne "commande" des feuilles
             * DetailsCommandes et FactureCommandes.
             *
             * La clé principale est le numéro de ligne Excel.
             *
             * Exemple :
             * Commandes
             * ligne 2 -> commande créée avec ID 15
             *
             * DetailsCommandes
             * commande = 2
             *
             * => on retrouve la commande ID 15.
             */
            Map<String, Commandes> commandesMap = new LinkedHashMap<>();

            int importedCommandes = 0;
            int importedDetails = 0;
            int importedFactures = 0;

            /*
             * ============================================================
             * 1. IMPORT DES COMMANDES
             * ============================================================
             */

            for (Row row : commandesSheet) {

                if (row.getRowNum() == 0 || isRowEmpty(row)) {
                    continue;
                }

                String clientLabel = required(row, "client");

                String dateCommandeValue = required(row, "datecommande", "date commande");

                String zoneLabel = required(row, "zonelivraison", "zone livraison");

                String montantTotalValue = required(row, "montanttotal", "montant total");

                Clients client = findClientForImport(clientLabel);

                ZonesLivraison zone = findZoneForImport(zoneLabel);

                Commandes commande = new Commandes();

                commande.setClient(client);
                commande.setDateCommande(
                        LocalDate.parse(dateCommandeValue));
                commande.setZoneLivraison(zone);
                commande.setMontantTotal(
                        new BigDecimal(montantTotalValue));

                Commandes saved = commandesRepository.save(commande);

                /*
                 * row.getRowNum() commence à 0.
                 *
                 * Excel :
                 * ligne 1 = header
                 * ligne 2 = première donnée
                 *
                 * Donc on utilise row.getRowNum() + 1
                 * comme référence de commande dans Excel.
                 */
                String excelRowReference = String.valueOf(row.getRowNum() + 1);

                commandesMap.put(
                        excelRowReference,
                        saved);

                /*
                 * On permet également de retrouver la commande
                 * avec son ID PostgreSQL.
                 */
                if (saved.getId() != null) {
                    commandesMap.put(
                            String.valueOf(saved.getId()),
                            saved);
                }

                importedCommandes++;
            }

            /*
             * ============================================================
             * 2. IMPORT DES DETAILS
             * ============================================================
             */

            for (Row row : detailsSheet) {

                if (row.getRowNum() == 0 || isRowEmpty(row)) {
                    continue;
                }

                String commandeReference = required(row, "commande");

                String platLabel = required(row, "plat");

                String quantiteValue = required(row, "quantite");

                String prixUnitaireValue = required(row, "prixunitaire", "prix unitaire");

                String montantValue = required(row, "montant");

                Commandes commande = commandesMap.get(
                        normalize(commandeReference));

                if (commande == null) {
                    throw new IllegalArgumentException(
                            "Commande introuvable dans DetailsCommandes : "
                                    + commandeReference);
                }

                Plats plat = findPlatForImport(platLabel);

                DetailsCommandes detail = new DetailsCommandes();

                detail.setCommande(commande);
                detail.setPlat(plat);

                detail.setQuantite(
                        new BigDecimal(quantiteValue));

                detail.setPrixUnitaire(
                        new BigDecimal(prixUnitaireValue));

                detail.setMontant(
                        new BigDecimal(montantValue));

                detailsCommandesRepository.save(detail);

                importedDetails++;
            }

            /*
             * ============================================================
             * 3. IMPORT DES FACTURES
             * ============================================================
             */

            for (Row row : facturesSheet) {

                if (row.getRowNum() == 0 || isRowEmpty(row)) {
                    continue;
                }

                String commandeReference = required(row, "commande");

                String dateFactureValue = required(row, "datefacture", "date facture");

                String montantTotalValue = required(row, "montanttotal", "montant total");

                Commandes commande = commandesMap.get(
                        normalize(commandeReference));

                if (commande == null) {
                    throw new IllegalArgumentException(
                            "Commande introuvable dans FactureCommandes : "
                                    + commandeReference);
                }

                FacturesCommandes facture = new FacturesCommandes();

                facture.setCommande(commande);

                facture.setDateFacture(
                        LocalDate.parse(dateFactureValue));

                facture.setMontantTotal(
                        new BigDecimal(montantTotalValue));

                facturesCommandesRepository.save(facture);

                importedFactures++;
            }

            /*
             * importedRows = total des objets réellement insérés.
             */
            int totalImported = importedCommandes
                    + importedDetails
                    + importedFactures;

            return new ImportSummary(
                    totalImported,
                    0,
                    3);
        }
    }

    private Clients findClientForImport(String value) {

        String normalized = normalize(value);

        /*
         * Si Excel contient directement l'ID du client.
         */
        try {
            Long id = Long.parseLong(normalized);

            Clients client = clientsRepository.findById(id).orElse(null);

            if (client != null) {
                return client;
            }
        } catch (NumberFormatException ignored) {
        }

        /*
         * Sinon :
         * "Nom Prénom"
         */
        for (Clients client : clientsRepository.findAll()) {

            String fullName = normalize(
                    client.getNom()
                            + " "
                            + client.getPrenom());

            if (fullName.equals(normalized)) {
                return client;
            }

            /*
             * On accepte également uniquement le nom.
             */
            if (normalize(client.getNom()).equals(normalized)) {
                return client;
            }
        }

        throw new IllegalArgumentException(
                "Client introuvable : " + value);
    }

    private ZonesLivraison findZoneForImport(String value) {

        String normalized = normalize(value);

        /*
         * Excel peut contenir l'ID.
         */
        try {
            Long id = Long.parseLong(normalized);

            ZonesLivraison zone = zoneLivraisonRepository.findById(id).orElse(null);

            if (zone != null) {
                return zone;
            }
        } catch (NumberFormatException ignored) {
        }

        /*
         * Sinon recherche par libellé.
         */
        for (ZonesLivraison zone : zoneLivraisonRepository.findAll()) {

            if (normalize(zone.getLibelle()).equals(normalized)) {
                return zone;
            }
        }

        throw new IllegalArgumentException(
                "Zone de livraison introuvable : " + value);
    }

    private Plats findPlatForImport(String value) {

        String normalized = normalize(value);

        /*
         * Excel peut contenir l'ID du plat.
         */
        try {
            Long id = Long.parseLong(normalized);

            Plats plat = platsRepository.findById(id).orElse(null);

            if (plat != null) {
                return plat;
            }
        } catch (NumberFormatException ignored) {
        }

        /*
         * Sinon recherche par nom.
         */
        for (Plats plat : platsRepository.findAll()) {

            if (normalize(plat.getNom()).equals(normalized)) {
                return plat;
            }
        }

        throw new IllegalArgumentException(
                "Plat introuvable : " + value);
    }

    public void exportZonesLivraison(HttpServletResponse response) throws IOException {
        writeWorkbook(response, "zones-livraison.xlsx", workbook -> {
            Sheet sheet = workbook.createSheet("ZonesLivraison");
            writeHeader(sheet, "libelle", "min", "max", "prix");
            int rowIndex = 1;
            for (ZonesLivraison zone : zoneLivraisonRepository.findAll()) {
                Row row = sheet.createRow(rowIndex++);
                writeRow(row,
                        zone.getLibelle(),
                        zone.getMin().toPlainString(),
                        zone.getMax().toPlainString(),
                        zone.getPrix().toPlainString());
            }
        });
    }

    public ImportSummary importZonesLivraison(MultipartFile file) throws IOException {
        return importSimple(file, "ZonesLivraison", row -> {
            ZonesLivraison zone = new ZonesLivraison();
            zone.setLibelle(required(row, "libelle"));
            zone.setMin(new BigDecimal(required(row, "min")));
            zone.setMax(new BigDecimal(required(row, "max")));
            zone.setPrix(new BigDecimal(required(row, "prix")));
            return zoneLivraisonRepository.save(zone);
        });
    }

    @FunctionalInterface
    private interface RowSaver<T> {
        T save(Row row);
    }

    @FunctionalInterface
    private interface WorkbookWriter {
        void write(Workbook workbook) throws IOException;
    }

    @FunctionalInterface
    private interface Factory<T> {
        T create();
    }

    @FunctionalInterface
    private interface Setter<T> {
        void set(T value);
    }

    @FunctionalInterface
    private interface Saver<T> {
        T save(T value);
    }

    @FunctionalInterface
    private interface LabelGetter<T> {
        String get(T value);
    }
}