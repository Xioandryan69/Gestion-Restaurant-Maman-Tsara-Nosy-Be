package com.gestion.restaurant.service.exportation.recettes;

import com.gestion.restaurant.entity.ingredients.Ingredients;
import com.gestion.restaurant.entity.plats.Plats;
import com.gestion.restaurant.entity.plats.RecettePlats;
import com.gestion.restaurant.repository.recettes.RecettePlatsRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Export Excel des recettes (association Plat <-> Ingrédient).
 * Une ligne par couple (plat, ingrédient) — même format que le template d'import.
 */
@Service
public class RecetteExport {

    private final RecettePlatsRepository recettePlatsRepository;

    public RecetteExport(RecettePlatsRepository recettePlatsRepository) {
        this.recettePlatsRepository = recettePlatsRepository;
    }

    public void exportRecettes(HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Recettes");
            Row header = sheet.createRow(0);
            createCell(header, 0, "plat");
            createCell(header, 1, "categoriePlats");
            createCell(header, 2, "prixVente");
            createCell(header, 3, "ingredient");
            createCell(header, 4, "quantiteRequise");
            createCell(header, 5, "unite");

            int rowIndex = 1;
            for (RecettePlats recette : recettePlatsRepository.findAll()) {
                Plats plat = recette.getPlat();
                Ingredients ingredient = recette.getIngredient();

                Row row = sheet.createRow(rowIndex++);
                createCell(row, 0, plat != null ? plat.getNom() : "");
                createCell(row, 1, plat != null && plat.getCategoriePlats() != null
                        ? plat.getCategoriePlats().getLibelle() : "");
                createCell(row, 2, plat != null && plat.getPrixVente() != null
                        ? plat.getPrixVente().toPlainString() : "0");
                createCell(row, 3, ingredient != null ? ingredient.getNom() : "");
                createCell(row, 4, recette.getQuantiteRequise() != null
                        ? recette.getQuantiteRequise().toPlainString() : "0");
                createCell(row, 5, ingredient != null && ingredient.getUnite() != null
                        ? ingredient.getUnite().getNom() : "");
            }

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = URLEncoder.encode("recettes-export.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);

            workbook.write(response.getOutputStream());
            response.flushBuffer();
        }
    }

    private void createCell(Row row, int columnIndex, String value) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value != null ? value : "");
    }
}