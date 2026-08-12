package com.gestion.restaurant.controller.excel;

import com.gestion.restaurant.service.excel.RestaurantExcelExchangeService;
import com.gestion.restaurant.service.importation.ingredients.IngredientsImportService;
import jakarta.servlet.http.HttpServletResponse;
import com.gestion.restaurant.service.importation.recettes.RecetteImportService;
import java.util.List;
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
        private final RecetteImportService recetteImportService;

        public ImportController(
                        RestaurantExcelExchangeService excelService,
                        IngredientsImportService ingredientsImportService,
                        RecetteImportService recetteImportService) {

                this.excelService = excelService;
                this.ingredientsImportService = ingredientsImportService;
                this.recetteImportService = recetteImportService;
        }

        @PostMapping("/clients")
        public String clients(
                        @RequestParam("file") MultipartFile file,
                        RedirectAttributes redirectAttributes) throws IOException {

                var result = excelService.importClients(file);

                redirectAttributes.addFlashAttribute(
                                "successMessage",
                                "Import clients terminé : " + result.total() + " ligne(s).");

                return "redirect:/clients";
        }

        @PostMapping("/fournisseurs")
        public String fournisseurs(
                        @RequestParam("file") MultipartFile file,
                        RedirectAttributes redirectAttributes) throws IOException {

                var result = excelService.importFournisseurs(file);

                redirectAttributes.addFlashAttribute(
                                "successMessage",
                                "Import fournisseurs terminé : " + result.total() + " ligne(s).");

                return "redirect:/fournisseurs";
        }

        @PostMapping("/personnels")
        public String personnels(
                        @RequestParam("file") MultipartFile file,
                        RedirectAttributes redirectAttributes) throws IOException {

                var result = excelService.importPersonnels(file);

                redirectAttributes.addFlashAttribute(
                                "successMessage",
                                "Import personnels terminé : " + result.total() + " ligne(s).");

                return "redirect:/personnels";
        }

        @PostMapping("/caisse")
        public String caisse(
                        @RequestParam("file") MultipartFile file,
                        RedirectAttributes redirectAttributes) throws IOException {

                var result = excelService.importCaisse(file);

                redirectAttributes.addFlashAttribute(
                                "successMessage",
                                "Import caisse terminé : " + result.total() + " ligne(s).");

                return "redirect:/caisse";
        }

        @PostMapping("/plats")
        public String plats(
                        @RequestParam("file") MultipartFile file,
                        RedirectAttributes redirectAttributes) throws IOException {

                var result = excelService.importPlats(file);

                redirectAttributes.addFlashAttribute(
                                "successMessage",
                                "Import plats terminé : " + result.total() + " ligne(s).");

                return "redirect:/plats";
        }

        @PostMapping("/ingredients")
        public String ingredients(
                        @RequestParam("file") MultipartFile file,
                        RedirectAttributes redirectAttributes) throws IOException {
                if (file.isEmpty()) {
                        redirectAttributes.addFlashAttribute("errorMessage",
                                        "Veuillez sélectionner un fichier Excel valide.");
                        return "redirect:/ingredients";
                }
                var result = ingredientsImportService.importerExcel(file);

                redirectAttributes.addFlashAttribute(
                                "successMessage",
                                "Import ingrédients terminé : " + result.total() + " ligne(s).");

                return "redirect:/ingredients";
        }

        @PostMapping("/commandes")
        public String commandes(
                        @RequestParam("file") MultipartFile file,
                        RedirectAttributes redirectAttributes) throws IOException {

                var result = excelService.importCommandes(file);

                redirectAttributes.addFlashAttribute(
                                "successMessage",
                                "Import commandes terminé : " + result.total() + " ligne(s).");

                return "redirect:/commandes";
        }

        @PostMapping("/materielles")
        public String materielles(
                        @RequestParam("file") MultipartFile file,
                        RedirectAttributes redirectAttributes) throws IOException {

                var result = excelService.importMaterielles(file);

                redirectAttributes.addFlashAttribute(
                                "successMessage",
                                "Import matériels terminé : " + result.total() + " ligne(s).");

                return "redirect:/materielles";
        }

        @PostMapping("/zones-livraison")
        public String zonesLivraison(
                        @RequestParam("file") MultipartFile file,
                        RedirectAttributes redirectAttributes) throws IOException {

                var result = excelService.importZonesLivraison(file);

                redirectAttributes.addFlashAttribute(
                                "successMessage",
                                "Import zones de livraison terminé : " + result.total() + " ligne(s).");

                return "redirect:/zones-livraison";
        }

        // ---- Ajout : import des recettes — plusieurs fichiers *.xlsx en une fois ----
        @PostMapping("/recettes")
        public String recettes(
                        @RequestParam("files") List<MultipartFile> files,
                        RedirectAttributes redirectAttributes) throws IOException {

                List<MultipartFile> fichiersValides = files.stream()
                                .filter(f -> f != null && !f.isEmpty())
                                .toList();

                if (fichiersValides.isEmpty()) {
                        redirectAttributes.addFlashAttribute("errorMessage",
                                        "Veuillez sélectionner au moins un fichier Excel valide.");
                        return "redirect:/plats";
                }

                var result = recetteImportService.importerRecettes(fichiersValides);

                redirectAttributes.addFlashAttribute(
                                "successMessage",
                                "Import recettes terminé : " + result.lignesImportees() + " ligne(s) importée(s) sur "
                                                + result.fichiersTraites() + " fichier(s) — " + result.platsCrees()
                                                + " plat(s) créé(s).");

                return "redirect:/plats";
        }

}