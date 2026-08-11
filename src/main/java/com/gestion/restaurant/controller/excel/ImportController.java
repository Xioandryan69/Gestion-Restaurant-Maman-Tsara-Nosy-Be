package com.gestion.restaurant.controller.excel;

import com.gestion.restaurant.service.excel.RestaurantExcelExchangeService;
import com.gestion.restaurant.service.importation.ingredients.IngredientsImportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/import")
public class ImportController {

    private final RestaurantExcelExchangeService excelService;
    private final IngredientsImportService ingredientsImportService;

    public ImportController(
            RestaurantExcelExchangeService excelService,
            IngredientsImportService ingredientsImportService) {

        this.excelService = excelService;
        this.ingredientsImportService = ingredientsImportService;
    }

    @PostMapping("/clients")
    public String clients(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException {

        var result = excelService.importClients(file);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Import clients terminé : " + result.total() + " ligne(s)."
        );

        return "redirect:/clients";
    }

    @PostMapping("/fournisseurs")
    public String fournisseurs(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException {

        var result = excelService.importFournisseurs(file);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Import fournisseurs terminé : " + result.total() + " ligne(s)."
        );

        return "redirect:/fournisseurs";
    }

    @PostMapping("/personnels")
    public String personnels(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException {

        var result = excelService.importPersonnels(file);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Import personnels terminé : " + result.total() + " ligne(s)."
        );

        return "redirect:/personnels";
    }

    @PostMapping("/caisse")
    public String caisse(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException {

        var result = excelService.importCaisse(file);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Import caisse terminé : " + result.total() + " ligne(s)."
        );

        return "redirect:/caisse";
    }

    @PostMapping("/plats")
    public String plats(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException {

        var result = excelService.importPlats(file);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Import plats terminé : " + result.total() + " ligne(s)."
        );

        return "redirect:/plats";
    }

    @PostMapping("/ingredients")
    public String ingredients(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException {

        var result = ingredientsImportService.importerExcel(file);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Import ingrédients terminé : " + result.total() + " ligne(s)."
        );

        return "redirect:/ingredients";
    }
}