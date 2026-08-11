package com.gestion.restaurant.controller.excel;

import com.gestion.restaurant.service.excel.RestaurantExcelExchangeService;
import com.gestion.restaurant.service.exportation.ingredients.IngredientsExport;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/export")
public class ExportController {

    private final RestaurantExcelExchangeService excelService;
    private final IngredientsExport ingredientsExport;

    public ExportController(
            RestaurantExcelExchangeService excelService,
            IngredientsExport ingredientsExport) {

        this.excelService = excelService;
        this.ingredientsExport = ingredientsExport;
    }

    @GetMapping("/clients")
    public void clients(HttpServletResponse response) throws IOException {
        excelService.exportClients(response);
    }

    @GetMapping("/fournisseurs")
    public void fournisseurs(HttpServletResponse response) throws IOException {
        excelService.exportFournisseurs(response);
    }

    @GetMapping("/personnels")
    public void personnels(HttpServletResponse response) throws IOException {
        excelService.exportPersonnels(response);
    }

    @GetMapping("/caisse")
    public void caisse(HttpServletResponse response) throws IOException {
        excelService.exportCaisse(response);
    }

    @GetMapping("/plats")
    public void plats(HttpServletResponse response) throws IOException {
        excelService.exportPlats(response);
    }

    @GetMapping("/ingredients")
    public void ingredients(HttpServletResponse response) throws IOException {
        ingredientsExport.exportIngredients(response);
    }
}