package com.gestion.restaurant.service.excel;

import com.gestion.restaurant.entity.caisse.MouvementCaisse;
import com.gestion.restaurant.entity.caisse.TypeMouvementCaisse;
import com.gestion.restaurant.entity.clients.Clients;
import com.gestion.restaurant.entity.clients.TypeClient;
import com.gestion.restaurant.entity.fournisseurs.Fournisseurs;
import com.gestion.restaurant.entity.fournisseurs.TypeFournisseurs;
import com.gestion.restaurant.entity.ingredients.CategorieIngredients;
import com.gestion.restaurant.entity.ingredients.Ingredients;
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

    public RestaurantExcelExchangeService(ClientsRepository clientsRepository,
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
                                          IngredientsRepository ingredientsRepository) {
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
            for (Personnels personnel : personnelsRepository.findAll((root, query, cb) -> cb.conjunction(), org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getContent()) {
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
            for (MouvementCaisse mouvement : mouvementCaisseRepository.findAllWithType(org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getContent()) {
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
            TypeClient type = resolve(typeLabel, typeClientRepository.findAll(), TypeClient::new, t -> t.setLibelle(typeLabel.trim()), typeClientRepository::save, TypeClient::getLibelle);
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
            TypeFournisseurs type = resolve(typeLabel, typeFournisseursRepository.findAll(), TypeFournisseurs::new, t -> t.setLibelle(typeLabel.trim()), typeFournisseursRepository::save, TypeFournisseurs::getLibelle);
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
            RolePersonnels role = resolve(roleLabel, rolePersonnelsRepository.findAll(), RolePersonnels::new, r -> r.setLibelle(roleLabel.trim()), rolePersonnelsRepository::save, RolePersonnels::getLibelle);
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
            TypeMouvementCaisse type = resolve(required(row, "typemouvement", "type mouvement"), typeMouvementCaisseRepository.findAll(), TypeMouvementCaisse::new, t -> t.setLibelle(required(row, "typemouvement", "type mouvement").trim()), typeMouvementCaisseRepository::save, TypeMouvementCaisse::getLibelle);
            MouvementCaisse mouvement = new MouvementCaisse();
            mouvement.setDateMouvement(LocalDate.parse(required(row, "datemouvement", "date mouvement")));
            mouvement.setMontant(new BigDecimal(required(row, "montant")));
            mouvement.setTypeMouvement(type);
            return mouvementCaisseRepository.save(mouvement);
        });
    }

    public ImportSummary importPlats(MultipartFile file) throws IOException {
        return importSimple(file, "Plats", row -> {
            CategoriePlats categorie = resolve(required(row, "categorieplats", "categorie plats"), categoriePlatsRepository.findAll(), CategoriePlats::new, c -> c.setLibelle(required(row, "categorieplats", "categorie plats").trim()), categoriePlatsRepository::save, CategoriePlats::getLibelle);
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

    private <T> ImportSummary importSimple(MultipartFile file, String sheetName, RowSaver<T> rowSaver) throws IOException {
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
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

    private <T> T resolve(String label, Iterable<T> existing, Factory<T> factory, Setter<T> setter, Saver<T> saver, LabelGetter<T> getter) {
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

    private void writeWorkbook(HttpServletResponse response, String filename, WorkbookWriter writer) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            writer.write(workbook);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8));
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

    @FunctionalInterface
    private interface RowSaver<T> {
        T save(Row row);
    }

    @FunctionalInterface
    private interface WorkbookWriter {
        void write(Workbook workbook) throws IOException;
    }

    @FunctionalInterface
    private interface Factory<T> { T create(); }
    @FunctionalInterface
    private interface Setter<T> { void set(T value); }
    @FunctionalInterface
    private interface Saver<T> { T save(T value); }
    @FunctionalInterface
    private interface LabelGetter<T> { String get(T value); }
}