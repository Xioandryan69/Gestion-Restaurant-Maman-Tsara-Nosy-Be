package com.gestion.restaurant.service.importation.ingredients;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gestion.restaurant.repository.ingredients.*;

public class IngredientsImportService {
    @Autowired
    private CategorieIngredientsRepository categorieIngredientsRepository;
    @Autowired
    private IngredientsRepository ingredientsRepository;
    @Autowired
    private HistoriqueIngredientsRepository historiqueIngredientsRepository;
    @Autowired
    private EtatStockIngredientRepository etatstockIngredientsRepository;
    @Autowired
    private UniteRepository unitesRepository;
    @Autowired
    private StatutIngredientRepository statutIngredientsRepository;
    @Autowired
    private TypeMvtIngredientRepository typeMvtIngredientRepository;
    @Autowired
    private InventaireIngredientRepository inventaireIngredientRepository;

    public void importerExcelIngredients(MultipartFile file) throws Exception {

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {

            Sheet summarySheet = workbook.getSheet("Résumé Approvisionnement");
            if (summarySheet == null) {
                throw new IllegalArgumentException("La feuille 'Résumé Approvisionnement' est introuvable.");
            }

            // La ligne 0 est l'en-tête, les données sont à la ligne 1
            Row rowMere = summarySheet.getRow(1);
            if (rowMere == null) {
                throw new IllegalArgumentException(
                        "Le fichier Excel ne contient aucune donnée d'approvisionnement parent.");
            }

            // ApprovisionnementMere mere = new ApprovisionnementMere();

            // String dateStr = rowMere.getCell(1).getStringCellValue();
            // mere.setDateApprovisionnement(LocalDate.parse(dateStr));

            // double prixTransportDouble = rowMere.getCell(2).getNumericCellValue();
            // mere.setPrixTransport(BigDecimal.valueOf(prixTransportDouble));

            // SourceApprovisionnement source = new SourceApprovisionnement();
            // String fournisseurNomComplet = rowMere.getCell(3).getStringCellValue(); //
            // "Rakoto Be"
            // source.setNom(fournisseurNomComplet);
            // source.setContact(rowMere.getCell(4).getStringCellValue());
            // source.setLieu(rowMere.getCell(5).getStringCellValue());
            // source = sourceApprovisionnementService.save(source);
            // mere.setSourceApprovisionnement(source);

            // Sheet sheetFille = workbook.getSheet("Détails Articles (Fille)");
            // if (sheetFille == null) {
            // throw new IllegalArgumentException("La feuille 'Détails Articles (Fille)' est
            // introuvable.");
            // }

            // List<ApprovisionnementFille> listeFilles = new ArrayList<>();

            // for (int i = 1; i <= sheetFille.getLastRowNum(); i++) {
            // Row rowFille = sheetFille.getRow(i);
            // if (rowFille == null) {
            // continue;
            // }

            // ApprovisionnementFille fille = new ApprovisionnementFille();
            // fille.setApprovisionnementMere(mere);

            // String libelleRiz = rowFille.getCell(2).getStringCellValue();
            // RaceRiz raceRiz = raceRizRepository.findByLibelleIgnoreCase(libelleRiz)
            // .orElseThrow(() -> new RuntimeException("Variete de riz introuvable : " +
            // libelleRiz));

            // if (rowFille.getCell(5) != null) {
            // raceRiz.setPuVente(BigDecimal.valueOf(rowFille.getCell(5).getNumericCellValue()));
            // }

            // fille.setRaceRiz(raceRiz);

            // double quantite = rowFille.getCell(3).getNumericCellValue();
            // fille.setQuantite(BigDecimal.valueOf(quantite));

            // double puAchat = rowFille.getCell(4).getNumericCellValue();
            // fille.setPuAchat(BigDecimal.valueOf(puAchat));

            // listeFilles.add(fille);

            // }

            // ApprovisionnementMere savedMere = approvisionnementMereRepository.save(mere);
            // for (ApprovisionnementFille fille : listeFilles) {
            // fille.setApprovisionnementMere(savedMere);
            // approvisionnementFilleRepository.save(fille);
            // }
            // }

        }

    }

    private int getInt(Cell cell) {
        if (cell == null) {
            throw new RuntimeException("Cellule vide alors qu'un nombre entier était attendu");
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) {
                    throw new RuntimeException("Cellule vide alors qu'un nombre entier était attendu");
                }
                return Integer.parseInt(value);
            default:
                throw new RuntimeException("Type de cellule non supporté pour un entier : " + cell.getCellType());
        }
    }

    private double getDouble(Cell cell) {
        if (cell == null) {
            throw new RuntimeException("Cellule vide alors qu'un nombre décimal était attendu");
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                String value = cell.getStringCellValue().trim().replace(",", ".");
                if (value.isEmpty()) {
                    throw new RuntimeException("Cellule vide alors qu'un nombre décimal était attendu");
                }
                return Double.parseDouble(value);
            default:
                throw new RuntimeException("Type de cellule non supporté pour un nombre : " + cell.getCellType());
        }
    }

    private LocalDate getLocalDate(Cell cell) {
        if (cell == null) {
            throw new RuntimeException("Cellule vide alors qu'une date était attendue");
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue().trim();
            return LocalDate.parse(value); // format attendu : AAAA-MM-JJ
        }
        throw new RuntimeException("Type de cellule non supporté pour une date : " + cell.getCellType());
    }

    public Resource getTemplateImportPaymentFactures() {
        return new ClassPathResource("static/template_exel/facture/template_import_payment_factures.xlsx");
    }
        private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }


    private String getString(Cell cell) {
        if (cell == null) {
            return null;
        }
        return new DataFormatter().formatCellValue(cell).trim();
    }

    private BigDecimal getBigDecimal(Cell cell) {
        if (cell == null) {
            return BigDecimal.ZERO;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                return new BigDecimal(cell.getStringCellValue().trim());
            default:
                throw new IllegalStateException("Type de cellule salaire invalide : " + cell.getCellType());
        }
    }

}
