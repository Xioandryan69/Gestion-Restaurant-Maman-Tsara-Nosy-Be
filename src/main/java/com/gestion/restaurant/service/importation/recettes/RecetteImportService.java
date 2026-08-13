package com.gestion.restaurant.service.importation.recettes;

import com.gestion.restaurant.entity.ingredients.Ingredients;
import com.gestion.restaurant.entity.plats.CategoriePlats;
import com.gestion.restaurant.entity.plats.Plats;
import com.gestion.restaurant.entity.plats.RecettePlats;
import com.gestion.restaurant.repository.ingredients.IngredientsRepository;
import com.gestion.restaurant.repository.plats.CategoriePlatsRepository;
import com.gestion.restaurant.repository.plats.PlatsRepository;
import com.gestion.restaurant.repository.recettes.RecettePlatsRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Import Excel des recettes (Plat + Ingrédients).
 *
 * Règle métier : on charge une {@code Map<String, Plats>} indexée par le nom
 * normalisé du plat ("nomPlat") au début de l'import, PUIS on l'utilise pour
 * chaque ligne :
 *  - si le plat existe déjà dans la Map -> on le réutilise (pas de nouvelle requête / pas de doublon)
 *  - sinon -> on le crée (avec sa catégorie et son prix de vente) et on l'ajoute à la Map
 *    pour les lignes suivantes, y compris entre plusieurs fichiers importés dans le même appel.
 *
 * Plusieurs fichiers .xlsx peuvent être importés en une seule fois.
 */
@Service
public class RecetteImportService {

    public record ImportSummary(int lignesImportees, int platsCrees, int fichiersTraites) {
        public int total() {
            return lignesImportees;
        }
    }

    private final RecettePlatsRepository recettePlatsRepository;
    private final PlatsRepository platsRepository;
    private final CategoriePlatsRepository categoriePlatsRepository;
    private final IngredientsRepository ingredientsRepository;

    public RecetteImportService(RecettePlatsRepository recettePlatsRepository,
                                 PlatsRepository platsRepository,
                                 CategoriePlatsRepository categoriePlatsRepository,
                                 IngredientsRepository ingredientsRepository) {
        this.recettePlatsRepository = recettePlatsRepository;
        this.platsRepository = platsRepository;
        this.categoriePlatsRepository = categoriePlatsRepository;
        this.ingredientsRepository = ingredientsRepository;
    }

    @Transactional
    public ImportSummary importerRecettes(List<MultipartFile> files) throws IOException {

        // ---- Map<nomPlat> : chargée une seule fois, réutilisée/complétée pour tous les fichiers ----
        Map<String, Plats> platsParNom = indexPlats();
        Map<String, CategoriePlats> categoriesParLibelle = indexCategories();
        Map<String, Ingredients> ingredientsParNom = indexIngredients();

        int platsAuDepart = platsParNom.size();
        int lignesImportees = 0;
        int fichiersTraites = 0;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            lignesImportees += importerUnFichier(file, platsParNom, categoriesParLibelle, ingredientsParNom);
            fichiersTraites++;
        }

        int platsCrees = platsParNom.size() - platsAuDepart;

        return new ImportSummary(lignesImportees, platsCrees, fichiersTraites);
    }

    private int importerUnFichier(MultipartFile file,
                                   Map<String, Plats> platsParNom,
                                   Map<String, CategoriePlats> categoriesParLibelle,
                                   Map<String, Ingredients> ingredientsParNom) throws IOException {

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheet("Recettes");
            if (sheet == null && workbook.getNumberOfSheets() > 0) {
                // Tolère un classeur dont l'onglet a été renommé.
                sheet = workbook.getSheetAt(0);
            }
            if (sheet == null) {
                throw new IllegalArgumentException(
                        "Feuille 'Recettes' introuvable dans le fichier : " + file.getOriginalFilename());
            }

            int imported = 0;
            for (Row row : sheet) {
                if (row.getRowNum() == 0 || isRowEmpty(row)) {
                    continue;
                }

                String nomPlat = required(row, "plat", "nomplat", "nom plat");
                String ingredientLabel = required(row, "ingredient");
                String quantiteValue = required(row, "quantiterequise", "quantite requise", "quantite");

                Plats plat = trouverOuCreerPlat(row, nomPlat, platsParNom, categoriesParLibelle);
                Ingredients ingredient = trouverIngredient(ingredientLabel, ingredientsParNom);
                BigDecimal quantite = new BigDecimal(quantiteValue.replace(",", "."));

                RecettePlats recette = recettePlatsRepository
                        .findByPlatIdAndIngredientId(plat.getId(), ingredient.getId())
                        .orElseGet(RecettePlats::new);
                recette.setPlat(plat);
                recette.setIngredient(ingredient);
                recette.setQuantiteRequise(quantite);
                recettePlatsRepository.save(recette);

                imported++;
            }
            return imported;
        }
    }

    /**
     * Coeur de la règle demandée : utilise toujours la Map&lt;nomPlat&gt; en premier.
     * Si le plat est déjà dans la Map -> réutilisation. Sinon -> création + ajout à la Map.
     */
    private Plats trouverOuCreerPlat(Row row, String nomPlat,
                                      Map<String, Plats> platsParNom,
                                      Map<String, CategoriePlats> categoriesParLibelle) {

        String cle = normalize(nomPlat);
        Plats plat = platsParNom.get(cle);
        if (plat != null) {
            return plat;
        }

        String categorieLabel = optional(row, "categorieplats", "categorie plats", "categorie");
        CategoriePlats categorie = resoudreCategorie(categorieLabel, categoriesParLibelle);

        String prixVenteValue = optional(row, "prixvente", "prix vente");
        BigDecimal prixVente = (prixVenteValue != null && !prixVenteValue.isBlank())
                ? new BigDecimal(prixVenteValue.replace(",", "."))
                : BigDecimal.ZERO;

        Plats nouveauPlat = new Plats();
        nouveauPlat.setNom(nomPlat.trim());
        nouveauPlat.setCategoriePlats(categorie);
        nouveauPlat.setPrixVente(prixVente);

        Plats sauvegarde = platsRepository.save(nouveauPlat);
        platsParNom.put(cle, sauvegarde);
        return sauvegarde;
    }

    private CategoriePlats resoudreCategorie(String label, Map<String, CategoriePlats> categoriesParLibelle) {
        String libelle = (label != null && !label.isBlank()) ? label.trim() : "Non classée";
        String cle = normalize(libelle);

        CategoriePlats categorie = categoriesParLibelle.get(cle);
        if (categorie != null) {
            return categorie;
        }
        CategoriePlats nouvelle = new CategoriePlats();
        nouvelle.setLibelle(libelle);
        CategoriePlats sauvegardee = categoriePlatsRepository.save(nouvelle);
        categoriesParLibelle.put(cle, sauvegardee);
        return sauvegardee;
    }

    private Ingredients trouverIngredient(String label, Map<String, Ingredients> ingredientsParNom) {
        Ingredients ingredient = ingredientsParNom.get(normalize(label));
        if (ingredient == null) {
            throw new IllegalArgumentException(
                    "Ingrédient introuvable : " + label
                            + " (créez-le dans le module Ingrédients avant d'importer la recette)");
        }
        return ingredient;
    }

    private Map<String, Plats> indexPlats() {
        Map<String, Plats> map = new LinkedHashMap<>();
        for (Plats p : platsRepository.findAll()) {
            map.put(normalize(p.getNom()), p);
        }
        return map;
    }

    private Map<String, CategoriePlats> indexCategories() {
        Map<String, CategoriePlats> map = new LinkedHashMap<>();
        for (CategoriePlats c : categoriePlatsRepository.findAll()) {
            map.put(normalize(c.getLibelle()), c);
        }
        return map;
    }

    private Map<String, Ingredients> indexIngredients() {
        Map<String, Ingredients> map = new LinkedHashMap<>();
        for (Ingredients i : ingredientsRepository.findAllWithRelationsList()) {
            map.put(normalize(i.getNom()), i);
        }
        return map;
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
}