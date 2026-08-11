package com.gestion.restaurant.service.importation.ingredients;

import com.gestion.restaurant.entity.fournisseurs.Fournisseurs;
import com.gestion.restaurant.entity.fournisseurs.TypeFournisseurs;
import com.gestion.restaurant.entity.ingredients.CategorieIngredients;
import com.gestion.restaurant.entity.ingredients.EtatStockIngredient;
import com.gestion.restaurant.entity.ingredients.HistoriqueIngredients;
import com.gestion.restaurant.entity.ingredients.Ingredients;
import com.gestion.restaurant.entity.ingredients.InventaireIngredient;
import com.gestion.restaurant.entity.ingredients.StatutIngredient;
import com.gestion.restaurant.entity.ingredients.TypeMvtIngredient;
import com.gestion.restaurant.entity.ingredients.Unite;
import com.gestion.restaurant.repository.fournisseur.FournisseursRepository;
import com.gestion.restaurant.repository.fournisseur.TypeFournisseursRepository;
import com.gestion.restaurant.repository.ingredients.CategorieIngredientsRepository;
import com.gestion.restaurant.repository.ingredients.EtatStockIngredientRepository;
import com.gestion.restaurant.repository.ingredients.HistoriqueIngredientsRepository;
import com.gestion.restaurant.repository.ingredients.IngredientsRepository;
import com.gestion.restaurant.repository.ingredients.InventaireIngredientRepository;
import com.gestion.restaurant.repository.ingredients.StatutIngredientRepository;
import com.gestion.restaurant.repository.ingredients.TypeMvtIngredientRepository;
import com.gestion.restaurant.repository.ingredients.UniteRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional
public class IngredientsImportService {

    public record ImportResult(int categories, int statuts, int unites, int typesFournisseurs,
                               int fournisseurs, int ingredients, int etatsStock,
                               int inventaires, int historiques) {
        public int total() {
            return categories + statuts + unites + typesFournisseurs + fournisseurs + ingredients + etatsStock + inventaires + historiques;
        }
    }

    private final CategorieIngredientsRepository categorieIngredientsRepository;
    private final IngredientsRepository ingredientsRepository;
    private final HistoriqueIngredientsRepository historiqueIngredientsRepository;
    private final EtatStockIngredientRepository etatStockIngredientRepository;
    private final UniteRepository uniteRepository;
    private final StatutIngredientRepository statutIngredientRepository;
    private final TypeMvtIngredientRepository typeMvtIngredientRepository;
    private final InventaireIngredientRepository inventaireIngredientRepository;
    private final FournisseursRepository fournisseursRepository;
    private final TypeFournisseursRepository typeFournisseursRepository;

    public IngredientsImportService(CategorieIngredientsRepository categorieIngredientsRepository,
                                    IngredientsRepository ingredientsRepository,
                                    HistoriqueIngredientsRepository historiqueIngredientsRepository,
                                    EtatStockIngredientRepository etatStockIngredientRepository,
                                    UniteRepository uniteRepository,
                                    StatutIngredientRepository statutIngredientRepository,
                                    TypeMvtIngredientRepository typeMvtIngredientRepository,
                                    InventaireIngredientRepository inventaireIngredientRepository,
                                    FournisseursRepository fournisseursRepository,
                                    TypeFournisseursRepository typeFournisseursRepository) {
        this.categorieIngredientsRepository = categorieIngredientsRepository;
        this.ingredientsRepository = ingredientsRepository;
        this.historiqueIngredientsRepository = historiqueIngredientsRepository;
        this.etatStockIngredientRepository = etatStockIngredientRepository;
        this.uniteRepository = uniteRepository;
        this.statutIngredientRepository = statutIngredientRepository;
        this.typeMvtIngredientRepository = typeMvtIngredientRepository;
        this.inventaireIngredientRepository = inventaireIngredientRepository;
        this.fournisseursRepository = fournisseursRepository;
        this.typeFournisseursRepository = typeFournisseursRepository;
    }

    public ImportResult importerExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Map<String, CategorieIngredients> categories = indexCategories();
            Map<String, StatutIngredient> statuts = indexStatuts();
            Map<String, Unite> unites = indexUnites();
            Map<String, TypeFournisseurs> typesFournisseurs = indexTypesFournisseurs();
            Map<String, Fournisseurs> fournisseurs = indexFournisseurs();
            Map<String, TypeMvtIngredient> typesMvt = indexTypesMvt();
            Map<String, Ingredients> ingredients = indexIngredients();

            int importedCategories = importCategories(workbook.getSheet("CategoriesIngredients"), categories);
            int importedStatuts = importStatuts(workbook.getSheet("StatutsIngredients"), statuts);
            int importedUnites = importUnites(workbook.getSheet("Unites"), unites);
            int importedTypesFournisseurs = importTypesFournisseurs(workbook.getSheet("TypesFournisseurs"), typesFournisseurs);
            int importedFournisseurs = importFournisseurs(workbook.getSheet("Fournisseurs"), typesFournisseurs, fournisseurs);
            int importedIngredients = importIngredients(workbook.getSheet("Ingredients"), categories, statuts, fournisseurs, unites, ingredients);
            int importedEtatsStock = importEtatsStock(workbook.getSheet("EtatsStockIngredients"), ingredients);
            int importedInventaires = importInventaires(workbook.getSheet("InventairesIngredients"), ingredients, typesMvt);
            int importedHistoriques = importHistoriques(workbook.getSheet("HistoriquesIngredients"), ingredients);

            return new ImportResult(importedCategories, importedStatuts, importedUnites, importedTypesFournisseurs,
                    importedFournisseurs, importedIngredients, importedEtatsStock, importedInventaires, importedHistoriques);
        }
    }

    public Resource getTemplateImportIngredients() {
        return new ClassPathResource("static/template_exel/ingredients/template_import_ingredients.xlsx");
    }

    private int importCategories(Sheet sheet, Map<String, CategorieIngredients> existing) {
        return importSimpleSheet(sheet, existing, row -> {
            String libelle = requiredString(row, "libelle", "nom");
            return createCategory(libelle, existing);
        });
    }

    private int importStatuts(Sheet sheet, Map<String, StatutIngredient> existing) {
        return importSimpleSheet(sheet, existing, row -> {
            String libelle = requiredString(row, "libelle", "nom");
            return createStatut(libelle, existing);
        });
    }

    private int importUnites(Sheet sheet, Map<String, Unite> existing) {
        return importSimpleSheet(sheet, existing, row -> {
            String nom = requiredString(row, "nom");
            String symbole = optionalString(row, "symbole");
            String key = normalize(nom);
            Unite unite = existing.get(key);
            if (unite == null) {
                unite = new Unite();
                unite.setNom(nom.trim());
                unite.setSymbole(symbole);
                unite = uniteRepository.save(unite);
                existing.put(key, unite);
            } else if (symbole != null && !symbole.isBlank()) {
                unite.setSymbole(symbole.trim());
                unite = uniteRepository.save(unite);
                existing.put(key, unite);
            }
            return unite;
        });
    }

    private int importTypesFournisseurs(Sheet sheet, Map<String, TypeFournisseurs> existing) {
        return importSimpleSheet(sheet, existing, row -> {
            String libelle = requiredString(row, "libelle", "nom");
            return createTypeFournisseur(libelle, existing);
        });
    }

    private int importFournisseurs(Sheet sheet, Map<String, TypeFournisseurs> types, Map<String, Fournisseurs> existing) {
        if (sheet == null) {
            return 0;
        }
        int imported = 0;
        for (Row row : sheet) {
            if (row.getRowNum() == 0 || isRowEmpty(row)) {
                continue;
            }
            String typeLibelle = requiredString(row, "typefournisseurs", "type_fournisseurs");
            String nom = requiredString(row, "nom");
            String prenom = optionalString(row, "prenom");
            String contact = optionalString(row, "contact");

            TypeFournisseurs type = createTypeFournisseur(typeLibelle, types);
            String key = normalize(type.getLibelle() + "|" + nom + "|" + defaultString(prenom));
            Fournisseurs fournisseur = existing.get(key);
            if (fournisseur == null) {
                fournisseur = new Fournisseurs();
                fournisseur.setTypeFournisseurs(type);
                fournisseur.setNom(nom.trim());
                fournisseur.setPrenom(defaultString(prenom));
                fournisseur.setContact(defaultString(contact));
                fournisseur = fournisseursRepository.save(fournisseur);
                existing.put(key, fournisseur);
                imported++;
            }
        }
        return imported;
    }

    private int importIngredients(Sheet sheet,
                                  Map<String, CategorieIngredients> categories,
                                  Map<String, StatutIngredient> statuts,
                                  Map<String, Fournisseurs> fournisseurs,
                                  Map<String, Unite> unites,
                                  Map<String, Ingredients> existing) {
        if (sheet == null) {
            return 0;
        }
        int imported = 0;
        for (Row row : sheet) {
            if (row.getRowNum() == 0 || isRowEmpty(row)) {
                continue;
            }
            String nom = requiredString(row, "nom");
            String categorieLibelle = requiredString(row, "categorieingredients", "categorie ingredients");
            String statutLibelle = requiredString(row, "statutingredients", "statut ingredients");
            String fournisseurLibelle = requiredString(row, "fournisseur");
            String uniteLibelle = requiredString(row, "unite");

            CategorieIngredients categorie = createCategory(categorieLibelle, categories);
            StatutIngredient statut = createStatut(statutLibelle, statuts);
            Unite unite = resolveUnite(uniteLibelle, unites);
            Fournisseurs fournisseur = resolveFournisseur(fournisseurLibelle, fournisseurs);

            String key = normalize(nom + "|" + categorie.getLibelle() + "|" + statut.getLibelle() + "|" + fournisseur.getNom() + " " + fournisseur.getPrenom() + "|" + unite.getNom());
            Ingredients ingredient = existing.get(key);
            if (ingredient == null) {
                ingredient = new Ingredients();
                ingredient.setNom(nom.trim());
                ingredient.setCategorieIngredients(categorie);
                ingredient.setStatutIngredient(statut);
                ingredient.setFournisseur(fournisseur);
                ingredient.setUnite(unite);
                ingredient = ingredientsRepository.save(ingredient);
                existing.put(key, ingredient);
                imported++;
            }
        }
        return imported;
    }

    private int importEtatsStock(Sheet sheet, Map<String, Ingredients> ingredients) {
        if (sheet == null) {
            return 0;
        }
        int imported = 0;
        Map<String, EtatStockIngredient> seen = new HashMap<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0 || isRowEmpty(row)) {
                continue;
            }
            String ingredientNom = requiredString(row, "ingredient", "nomingredient");
            LocalDate dateEtatStock = requiredDate(row, "dateetatstock", "date etat stock");
            BigDecimal quantite = requiredDecimal(row, "quantite");
            Ingredients ingredient = resolveIngredient(ingredientNom, ingredients);
            String key = normalize(ingredient.getNom() + "|" + dateEtatStock + "|" + quantite.toPlainString());
            if (seen.containsKey(key)) {
                continue;
            }
            EtatStockIngredient state = new EtatStockIngredient();
            state.setIngredient(ingredient);
            state.setDateEtatStock(dateEtatStock);
            state.setQuantite(quantite);
            etatStockIngredientRepository.save(state);
            seen.put(key, state);
            imported++;
        }
        return imported;
    }

    private int importInventaires(Sheet sheet, Map<String, Ingredients> ingredients, Map<String, TypeMvtIngredient> typesMvt) {
        if (sheet == null) {
            return 0;
        }
        int imported = 0;
        Map<String, InventaireIngredient> seen = new HashMap<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0 || isRowEmpty(row)) {
                continue;
            }
            String ingredientNom = requiredString(row, "ingredient", "nomingredient");
            LocalDate dateInventaire = requiredDate(row, "dateinventaire", "date inventaire");
            BigDecimal quantite = requiredDecimal(row, "quantite");
            String typeMvtLibelle = requiredString(row, "typemvtingredient", "type mvt ingredient");

            Ingredients ingredient = resolveIngredient(ingredientNom, ingredients);
            TypeMvtIngredient typeMvtIngredient = resolveTypeMvt(typeMvtLibelle, typesMvt);
            String key = normalize(ingredient.getNom() + "|" + dateInventaire + "|" + quantite.toPlainString() + "|" + typeMvtIngredient.getLibelle());
            if (seen.containsKey(key)) {
                continue;
            }
            InventaireIngredient inventaire = new InventaireIngredient();
            inventaire.setIngredient(ingredient);
            inventaire.setDateInventaire(dateInventaire);
            inventaire.setQuantite(quantite);
            inventaire.setTypeMvtIngredient(typeMvtIngredient);
            inventaireIngredientRepository.save(inventaire);
            seen.put(key, inventaire);
            imported++;
        }
        return imported;
    }

    private int importHistoriques(Sheet sheet, Map<String, Ingredients> ingredients) {
        if (sheet == null) {
            return 0;
        }
        int imported = 0;
        Map<String, HistoriqueIngredients> seen = new HashMap<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0 || isRowEmpty(row)) {
                continue;
            }
            String ingredientNom = requiredString(row, "nomingredient", "ingredient");
            LocalDate dateEntree = requiredDate(row, "dateentree", "date entree");
            LocalDate datePeremption = optionalDate(row, "dateperemption", "date peremption");
            BigDecimal quantite = requiredDecimal(row, "quantite");
            BigDecimal prixAchat = requiredDecimal(row, "prixachat", "prix achat");

            Ingredients ingredient = resolveIngredient(ingredientNom, ingredients);
            String key = normalize(ingredient.getNom() + "|" + dateEntree + "|" + defaultString(datePeremption) + "|" + quantite.toPlainString() + "|" + prixAchat.toPlainString());
            if (seen.containsKey(key)) {
                continue;
            }
            HistoriqueIngredients historique = new HistoriqueIngredients();
            historique.setIngredient(ingredient);
            historique.setDateEntree(dateEntree);
            historique.setDatePeremption(datePeremption);
            historique.setQuantite(quantite);
            historique.setPrixAchat(prixAchat);
            historiqueIngredientsRepository.save(historique);
            seen.put(key, historique);
            imported++;
        }
        return imported;
    }

    private <T> int importSimpleSheet(Sheet sheet, Map<String, T> existing, RowImporter<T> importer) {
        if (sheet == null) {
            return 0;
        }
        int imported = 0;
        for (Row row : sheet) {
            if (row.getRowNum() == 0 || isRowEmpty(row)) {
                continue;
            }
            T entity = importer.importRow(row);
            if (entity != null) {
                imported++;
            }
        }
        return imported;
    }

    private CategorieIngredients createCategory(String libelle, Map<String, CategorieIngredients> existing) {
        String key = normalize(libelle);
        CategorieIngredients categorie = existing.get(key);
        if (categorie == null) {
            categorie = new CategorieIngredients();
            categorie.setLibelle(libelle.trim());
            categorie = categorieIngredientsRepository.save(categorie);
            existing.put(key, categorie);
        }
        return categorie;
    }

    private StatutIngredient createStatut(String libelle, Map<String, StatutIngredient> existing) {
        String key = normalize(libelle);
        StatutIngredient statut = existing.get(key);
        if (statut == null) {
            statut = new StatutIngredient();
            statut.setLibelle(libelle.trim());
            statut = statutIngredientRepository.save(statut);
            existing.put(key, statut);
        }
        return statut;
    }

    private TypeFournisseurs createTypeFournisseur(String libelle, Map<String, TypeFournisseurs> existing) {
        String key = normalize(libelle);
        TypeFournisseurs type = existing.get(key);
        if (type == null) {
            type = new TypeFournisseurs();
            type.setLibelle(libelle.trim());
            type = typeFournisseursRepository.save(type);
            existing.put(key, type);
        }
        return type;
    }

    private Unite resolveUnite(String label, Map<String, Unite> existing) {
        Unite unite = existing.get(normalize(label));
        if (unite != null) {
            return unite;
        }
        for (Unite candidate : uniteRepository.findAll()) {
            if (normalize(candidate.getNom()).equals(normalize(label)) || normalize(candidate.getSymbole()).equals(normalize(label))) {
                existing.put(normalize(candidate.getNom()), candidate);
                return candidate;
            }
        }
        throw new IllegalArgumentException("Unité introuvable : " + label);
    }

    private Fournisseurs resolveFournisseur(String label, Map<String, Fournisseurs> existing) {
        String normalized = normalize(label);
        Fournisseurs fournisseur = existing.get(normalized);
        if (fournisseur != null) {
            return fournisseur;
        }
        for (Fournisseurs candidate : fournisseursRepository.findAllWithTypeList()) {
            String fullName = candidate.getNom() + " " + defaultString(candidate.getPrenom());
            if (normalize(fullName).equals(normalized) || normalize(candidate.getNom()).equals(normalized)) {
                existing.put(normalize(fullName), candidate);
                return candidate;
            }
        }
        throw new IllegalArgumentException("Fournisseur introuvable : " + label);
    }

    private TypeMvtIngredient resolveTypeMvt(String label, Map<String, TypeMvtIngredient> existing) {
        TypeMvtIngredient type = existing.get(normalize(label));
        if (type != null) {
            return type;
        }
        for (TypeMvtIngredient candidate : typeMvtIngredientRepository.findAll()) {
            if (normalize(candidate.getLibelle()).equals(normalize(label))) {
                existing.put(normalize(candidate.getLibelle()), candidate);
                return candidate;
            }
        }
        TypeMvtIngredient created = new TypeMvtIngredient();
        created.setLibelle(label.trim());
        created = typeMvtIngredientRepository.save(created);
        existing.put(normalize(created.getLibelle()), created);
        return created;
    }

    private Ingredients resolveIngredient(String label, Map<String, Ingredients> existing) {
        Ingredients ingredient = existing.get(normalize(label));
        if (ingredient != null) {
            return ingredient;
        }
        for (Ingredients candidate : ingredientsRepository.findAllWithRelationsList()) {
            if (normalize(candidate.getNom()).equals(normalize(label))) {
                existing.put(normalize(candidate.getNom()), candidate);
                return candidate;
            }
        }
        throw new IllegalArgumentException("Ingrédient introuvable : " + label);
    }

    private Map<String, CategorieIngredients> indexCategories() {
        Map<String, CategorieIngredients> map = new LinkedHashMap<>();
        for (CategorieIngredients categorie : categorieIngredientsRepository.findAll()) {
            map.put(normalize(categorie.getLibelle()), categorie);
        }
        return map;
    }

    private Map<String, StatutIngredient> indexStatuts() {
        Map<String, StatutIngredient> map = new LinkedHashMap<>();
        for (StatutIngredient statut : statutIngredientRepository.findAll()) {
            map.put(normalize(statut.getLibelle()), statut);
        }
        return map;
    }

    private Map<String, Unite> indexUnites() {
        Map<String, Unite> map = new LinkedHashMap<>();
        for (Unite unite : uniteRepository.findAll()) {
            map.put(normalize(unite.getNom()), unite);
            map.putIfAbsent(normalize(unite.getSymbole()), unite);
        }
        return map;
    }

    private Map<String, TypeFournisseurs> indexTypesFournisseurs() {
        Map<String, TypeFournisseurs> map = new LinkedHashMap<>();
        for (TypeFournisseurs type : typeFournisseursRepository.findAll()) {
            map.put(normalize(type.getLibelle()), type);
        }
        return map;
    }

    private Map<String, Fournisseurs> indexFournisseurs() {
        Map<String, Fournisseurs> map = new LinkedHashMap<>();
        for (Fournisseurs fournisseur : fournisseursRepository.findAllWithTypeList()) {
            String fullName = fournisseur.getNom() + " " + defaultString(fournisseur.getPrenom());
            map.put(normalize(fullName), fournisseur);
            map.putIfAbsent(normalize(fournisseur.getNom()), fournisseur);
        }
        return map;
    }

    private Map<String, TypeMvtIngredient> indexTypesMvt() {
        Map<String, TypeMvtIngredient> map = new LinkedHashMap<>();
        for (TypeMvtIngredient typeMvtIngredient : typeMvtIngredientRepository.findAll()) {
            map.put(normalize(typeMvtIngredient.getLibelle()), typeMvtIngredient);
        }
        return map;
    }

    private Map<String, Ingredients> indexIngredients() {
        Map<String, Ingredients> map = new LinkedHashMap<>();
        for (Ingredients ingredient : ingredientsRepository.findAllWithRelationsList()) {
            map.put(normalize(ingredient.getNom()), ingredient);
        }
        return map;
    }

    private String requiredString(Row row, String... candidates) {
        for (String candidate : candidates) {
            String value = optionalString(row, candidate);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        throw new IllegalArgumentException("Valeur obligatoire manquante pour les colonnes: " + String.join(", ", candidates));
    }

    private String optionalString(Row row, String columnName) {
        Cell cell = getCellByHeader(row, columnName);
        if (cell == null) {
            return null;
        }
        return new DataFormatter().formatCellValue(cell).trim();
    }

    private LocalDate requiredDate(Row row, String... candidates) {
        for (String candidate : candidates) {
            Cell cell = getCellByHeader(row, candidate);
            if (cell != null) {
                return parseDate(cell);
            }
        }
        throw new IllegalArgumentException("Date obligatoire manquante pour les colonnes: " + String.join(", ", candidates));
    }

    private LocalDate optionalDate(Row row, String... candidates) {
        for (String candidate : candidates) {
            Cell cell = getCellByHeader(row, candidate);
            if (cell != null) {
                String value = new DataFormatter().formatCellValue(cell).trim();
                if (!value.isBlank()) {
                    return parseDate(cell);
                }
            }
        }
        return null;
    }

    private BigDecimal requiredDecimal(Row row, String... candidates) {
        for (String candidate : candidates) {
            Cell cell = getCellByHeader(row, candidate);
            if (cell != null) {
                return parseDecimal(cell);
            }
        }
        throw new IllegalArgumentException("Nombre obligatoire manquant pour les colonnes: " + String.join(", ", candidates));
    }

    private Cell getCellByHeader(Row row, String headerName) {
        Row headerRow = row.getSheet().getRow(0);
        if (headerRow == null) {
            return null;
        }
        Map<String, Integer> headers = new HashMap<>();
        DataFormatter formatter = new DataFormatter();
        for (Cell headerCell : headerRow) {
            headers.put(normalize(formatter.formatCellValue(headerCell)), headerCell.getColumnIndex());
        }
        Integer index = headers.get(normalize(headerName));
        return index == null ? null : row.getCell(index);
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK && !new DataFormatter().formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private LocalDate parseDate(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        String value = new DataFormatter().formatCellValue(cell).trim();
        if (value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private BigDecimal parseDecimal(Cell cell) {
        if (cell == null) {
            return BigDecimal.ZERO;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        String value = new DataFormatter().formatCellValue(cell).trim().replace(',', '.');
        if (value.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String defaultString(String value) {
        return value == null ? "" : value.trim();
    }

    private String defaultString(LocalDate value) {
        return value == null ? "" : value.toString();
    }

    @FunctionalInterface
    private interface RowImporter<T> {
        T importRow(Row row);
    }
}