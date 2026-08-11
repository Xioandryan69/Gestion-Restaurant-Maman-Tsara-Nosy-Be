package com.gestion.restaurant.service.exportation.ingredients;

import com.gestion.restaurant.entity.ingredients.Ingredients;
import com.gestion.restaurant.repository.ingredients.IngredientsRepository;
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

@Service
public class IngredientsExport {

    private final IngredientsRepository ingredientsRepository;

    public IngredientsExport(IngredientsRepository ingredientsRepository) {
        this.ingredientsRepository = ingredientsRepository;
    }

    public void exportIngredients(HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ingredients");
            Row header = sheet.createRow(0);
            createCell(header, 0, "nom");
            createCell(header, 1, "categorieIngredients");
            createCell(header, 2, "statutIngredients");
            createCell(header, 3, "fournisseur");
            createCell(header, 4, "unite");

            int rowIndex = 1;
            for (Ingredients ingredient : ingredientsRepository.findAllWithRelationsList()) {
                Row row = sheet.createRow(rowIndex++);
                createCell(row, 0, ingredient.getNom());
                createCell(row, 1, ingredient.getCategorieIngredients() != null ? ingredient.getCategorieIngredients().getLibelle() : "");
                createCell(row, 2, ingredient.getStatutIngredient() != null ? ingredient.getStatutIngredient().getLibelle() : "");
                createCell(row, 3, ingredient.getFournisseur() != null ? ingredient.getFournisseur().getNom() + " " + ingredient.getFournisseur().getPrenom() : "");
                createCell(row, 4, ingredient.getUnite() != null ? ingredient.getUnite().getNom() : "");
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = URLEncoder.encode("ingredients-export.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
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
