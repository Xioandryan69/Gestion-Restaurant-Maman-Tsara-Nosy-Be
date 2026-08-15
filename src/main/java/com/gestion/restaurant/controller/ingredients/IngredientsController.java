package com.gestion.restaurant.controller.ingredients;

import com.gestion.restaurant.dto.ingredients.IngredientRequestDto;
import com.gestion.restaurant.dto.ingredients.IngredientSearchCriteria;
import com.gestion.restaurant.dto.ingredients.StockPageView;
import com.gestion.restaurant.service.exportation.ingredients.IngredientsExport;
import com.gestion.restaurant.service.importation.ingredients.IngredientsImportService;
import com.gestion.restaurant.service.ingredients.IngredientsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/ingredients")
public class IngredientsController {

    private final IngredientsService ingredientsService;
    private final IngredientsImportService ingredientsImportService;
    private final IngredientsExport ingredientsExport;

    public IngredientsController(IngredientsService ingredientsService,
                                 IngredientsImportService ingredientsImportService,
                                 IngredientsExport ingredientsExport) {
        this.ingredientsService = ingredientsService;
        this.ingredientsImportService = ingredientsImportService;
        this.ingredientsExport = ingredientsExport;
    }

    @GetMapping
    public String listIngredients(@ModelAttribute("criteria") IngredientSearchCriteria criteria,
                                  @PageableDefault(size = 10, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable,
                                  Model model) {
        model.addAttribute("page", ingredientsService.search(criteria, pageable));
        model.addAttribute("categories", ingredientsService.findAllCategories());
        model.addAttribute("statuts", ingredientsService.findAllStatuts());
        model.addAttribute("fournisseurs", ingredientsService.findAllFournisseurs());
        model.addAttribute("unites", ingredientsService.findAllUnites());
        return "ingredients/list";
    }



    @GetMapping("/export")
    public void exportIngredients(jakarta.servlet.http.HttpServletResponse response) throws IOException {
        ingredientsExport.exportIngredients(response);
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", ingredientsService.getDashboardData());
        return "ingredients/dashboard";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        populateFormLookups(model);
        model.addAttribute("ingredient", new IngredientRequestDto());
        return "ingredients/form";
    }

    @PostMapping("/save")
    public String saveIngredient(@Valid @ModelAttribute("ingredient") IngredientRequestDto dto,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            populateFormLookups(model);
            return "ingredients/form";
        }
        var enregistre = ingredientsService.saveFromDto(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Ingrédient enregistré.");
        return "redirect:/ingredients/" + enregistre.getId() + "/detail";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        populateFormLookups(model);
        model.addAttribute("ingredient", ingredientsService.toRequestDto(id));
        return "ingredients/form";
    }

    @PostMapping("/delete/{id}")
    public String deleteIngredient(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        ingredientsService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Ingrédient supprimé.");
        return "redirect:/ingredients";
    }

    @GetMapping("/{id}/detail")
    public String detail(@PathVariable("id") Long id,
                         @RequestParam(value = "quantite", required = false) BigDecimal quantite,
                         @RequestParam(value = "retour", required = false) String retour,
                         Model model) {
        model.addAttribute("ingredient", ingredientsService.findById(id));
        model.addAttribute("historiqueList", ingredientsService.findHistorique(id));
        model.addAttribute("inventaireList", ingredientsService.findInventaire(id));
        model.addAttribute("stockActuel", ingredientsService.getStockActuel(id));
        model.addAttribute("quantiteAAjouter", quantite);
        model.addAttribute("retour", ("manquants".equals(retour) || "commande".equals(retour)) ? retour : "");
        return "ingredients/detail";
    }

    @PostMapping("/{id}/achat/save")
    public String saveAchat(@PathVariable("id") Long id,
                            @RequestParam("quantite") BigDecimal quantite,
                            @RequestParam("prixAchat") BigDecimal prixAchat,
                            @RequestParam(value = "dateEntree", required = false)
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEntree,
                            @RequestParam(value = "datePeremption", required = false)
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datePeremption,
                            @RequestParam(value = "retour", required = false) String retour,
                            RedirectAttributes redirectAttributes) {
        ingredientsService.enregistrerAchatEntree(id, dateEntree, datePeremption, quantite, prixAchat);
        redirectAttributes.addFlashAttribute("successMessage", "Achat enregistré (stock + caisse).");
        if ("manquants".equals(retour)) return "redirect:/ingredients/manquants";
        if ("commande".equals(retour)) return "redirect:/commandes/new?resume=true";
        return "redirect:/ingredients/" + id + "/detail";
    }

    @PostMapping("/{id}/sortie/save")
    public String saveSortie(@PathVariable("id") Long id,
                             @RequestParam("quantite") BigDecimal quantite,
                             @RequestParam("typeMouvement") String typeMouvement,
                             @RequestParam(value = "dateMvt", required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateMvt,
                             RedirectAttributes redirectAttributes) {
        ingredientsService.enregistrerSortieOuPerte(id, quantite, typeMouvement, dateMvt);
        redirectAttributes.addFlashAttribute("successMessage", "Sortie / perte enregistrée.");
        return "redirect:/ingredients/" + id + "/detail";
    }

    @GetMapping("/stock")
    public String viewStockGlobal(
            @ModelAttribute("criteria") com.gestion.restaurant.dto.ingredients.StockSearchCriteria criteria,
            @PageableDefault(size = 10, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable,
            Model model) {
        StockPageView stock = ingredientsService.getGlobalStockState(criteria, pageable);
        model.addAttribute("page", stock.page());
        model.addAttribute("nombreAlerteStock", stock.nombreAlerteStock());
        model.addAttribute("nombreStockOk", stock.nombreStockOk());
        model.addAttribute("seuilStockFaible", stock.seuilStockFaible());
        model.addAttribute("categories", ingredientsService.findAllCategories());
        model.addAttribute("unites", ingredientsService.findAllUnites());
        return "ingredients/stock";
    }

    @GetMapping("/manquants")
    public String viewIngredientsManquants(@RequestParam(value = "nom", required = false) String nom,
                                           @RequestParam(value = "unite", required = false) String unite,
                                           @RequestParam(value = "stockMax", required = false) BigDecimal stockMax,
                                           @RequestParam(value = "ajoutMin", required = false) BigDecimal ajoutMin,
                                           @RequestParam(value = "sort", defaultValue = "nom") String sort,
                                           @RequestParam(value = "direction", defaultValue = "asc") String direction,
                                           Model model) {
        List<com.gestion.restaurant.dto.ingredients.IngredientManquantDto> resultat = ingredientsService.findIngredientsEnAlerte().stream()
                .filter(item -> nom == null || nom.isBlank() || item.nom().toLowerCase(Locale.ROOT).contains(nom.trim().toLowerCase(Locale.ROOT)))
                .filter(item -> unite == null || unite.isBlank() || item.unite().equalsIgnoreCase(unite.trim()))
                .filter(item -> stockMax == null || item.quantiteActuelle().compareTo(stockMax) <= 0)
                .filter(item -> ajoutMin == null || item.quantiteManquante().compareTo(ajoutMin) >= 0)
                .toList();
        Comparator<com.gestion.restaurant.dto.ingredients.IngredientManquantDto> comparator = switch (sort) {
            case "stock" -> Comparator.comparing(com.gestion.restaurant.dto.ingredients.IngredientManquantDto::quantiteActuelle);
            case "ajout" -> Comparator.comparing(com.gestion.restaurant.dto.ingredients.IngredientManquantDto::quantiteManquante);
            case "unite" -> Comparator.comparing(com.gestion.restaurant.dto.ingredients.IngredientManquantDto::unite, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(com.gestion.restaurant.dto.ingredients.IngredientManquantDto::nom, String.CASE_INSENSITIVE_ORDER);
        };
        if ("desc".equalsIgnoreCase(direction)) comparator = comparator.reversed();
        model.addAttribute("ingredientsManquants", resultat.stream().sorted(comparator).toList());
        model.addAttribute("nom", nom);
        model.addAttribute("unite", unite);
        model.addAttribute("stockMax", stockMax);
        model.addAttribute("ajoutMin", ajoutMin);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", "desc".equalsIgnoreCase(direction) ? "desc" : "asc");
        model.addAttribute("unites", ingredientsService.findAllUnites());
        model.addAttribute("seuilStockFaible", IngredientsService.SEUIL_STOCK_FAIBLE);
        return "ingredients/manquants";
    }

    private void populateFormLookups(Model model) {
        model.addAttribute("categories", ingredientsService.findAllCategories());
        model.addAttribute("statuts", ingredientsService.findAllStatuts());
        model.addAttribute("fournisseurs", ingredientsService.findAllFournisseurs());
        model.addAttribute("unites", ingredientsService.findAllUnites());
    }
}
