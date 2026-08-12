package com.gestion.restaurant.controller.excel;

import com.gestion.restaurant.service.excel.RestaurantExcelExchangeService;
import com.gestion.restaurant.service.exportation.ingredients.IngredientsExport;
import com.gestion.restaurant.service.exportation.recettes.RecetteExport;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/export")
public class ExportController {

    private final RestaurantExcelExchangeService excelService;
    private final IngredientsExport ingredientsExport;
    private final RecetteExport recetteExport;

    public ExportController(
            RestaurantExcelExchangeService excelService,
            IngredientsExport ingredientsExport,
            RecetteExport recetteExport) {

        this.excelService = excelService;
        this.ingredientsExport = ingredientsExport;
        this.recetteExport = recetteExport;
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

    // ---- Ajout : export des recettes (Plat + Ingrédients) ----
    @GetMapping("/recettes")
    public void recettes(HttpServletResponse response) throws IOException {
        recetteExport.exportRecettes(response);
    }

    @GetMapping("/zones-livraison")
    public void zonesLivraison(HttpServletResponse response) throws IOException {
        excelService.exportZonesLivraison(response);
    }
    @PostMapping("/zones-livraison")
    public String zonesLivraison(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException {

        var result = excelService.importZonesLivraison(file);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Import zones de livraison terminé : " + result.total() + " ligne(s)."
        );

        return "redirect:/zones-livraison";
    }
}