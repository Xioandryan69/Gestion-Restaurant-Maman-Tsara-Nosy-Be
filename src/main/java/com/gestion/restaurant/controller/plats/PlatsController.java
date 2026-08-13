package com.gestion.restaurant.controller.plats;

import com.gestion.restaurant.dto.plats.PlatMultipleRequestDto;
import com.gestion.restaurant.dto.plats.PlatSearchCriteria;
import com.gestion.restaurant.service.ingredients.IngredientsService;
import com.gestion.restaurant.service.plats.PlatsService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.gestion.restaurant.dto.plats.PlatUpdateRequestDto;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/plats")
public class PlatsController {

    private final PlatsService platsService;
    private final IngredientsService ingredientsService;

    public PlatsController(PlatsService platsService, IngredientsService ingredientsService) {
        this.platsService = platsService;
        this.ingredientsService = ingredientsService;
    }

    @GetMapping
    public String list(@ModelAttribute("criteria") PlatSearchCriteria criteria,
                       @PageableDefault(size = 10, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable,
                       Model model) {
        model.addAttribute("page", platsService.search(criteria, pageable));
        model.addAttribute("categories", platsService.findAllCategories());
        // compute prixAchat and benef per plat to provide server-side fallback for the list view
        var page = platsService.search(criteria, pageable);
        var prixAchatMap = new java.util.HashMap<Long, java.math.BigDecimal>();
        var benefMap = new java.util.HashMap<Long, java.math.BigDecimal>();
        for (var p : page.getContent()) {
            if (p != null && p.getId() != null) {
                try {
                    java.math.BigDecimal pa = platsService.calculatePrixAchatPlat(p.getId());
                    java.math.BigDecimal bf = platsService.calculateProfitPerUnit(p.getId());
                    prixAchatMap.put(p.getId(), pa);
                    benefMap.put(p.getId(), bf);
                } catch (Exception ex) {
                    // ignore per-item failures and leave map entries absent
                }
            }
        }
        model.addAttribute("prixAchatMap", prixAchatMap);
        model.addAttribute("benefMap", benefMap);
        return "plats/list";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", platsService.getDashboardData());
        return "plats/dashboard";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("formDto", new PlatMultipleRequestDto());
        model.addAttribute("categories", platsService.findAllCategories());
        model.addAttribute("allIngredients", ingredientsService.findAll());
        return "plats/form";
    }

    @PostMapping("/save-multiple")
    public String saveMultiple(@ModelAttribute("formDto") PlatMultipleRequestDto formDto,
                               RedirectAttributes redirectAttributes) {
        platsService.saveMultiplePlats(formDto);
        redirectAttributes.addFlashAttribute("successMessage", "Plat(s) enregistré(s).");
        return "redirect:/plats";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        platsService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Plat supprimé.");
        return "redirect:/plats";
    }
    @GetMapping("/edit/{id}")
public String showEditForm(@PathVariable("id") Long id,
                           Model model) {

    model.addAttribute("plat", platsService.toUpdateDto(id));
    model.addAttribute("categories", platsService.findAllCategories());

    return "plats/edit";
}

@PostMapping("/edit/{id}")
public String updatePlat(
        @PathVariable("id") Long id,
        @Valid @ModelAttribute("plat") PlatUpdateRequestDto dto,
        BindingResult result,
        Model model,
        RedirectAttributes redirectAttributes) {

    dto.setId(id);

    if (result.hasErrors()) {
        model.addAttribute("categories", platsService.findAllCategories());
        return "plats/edit";
    }

    platsService.updateFromDto(dto);

    redirectAttributes.addFlashAttribute(
            "successMessage",
            "Plat modifié avec succès."
    );

    return "redirect:/recettes/plat/" + id;
}
}
