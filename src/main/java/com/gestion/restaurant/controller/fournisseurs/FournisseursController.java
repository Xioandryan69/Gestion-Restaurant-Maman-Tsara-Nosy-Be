package com.gestion.restaurant.controller.fournisseurs;

import com.gestion.restaurant.dto.fournisseurs.FournisseurRequestDto;
import com.gestion.restaurant.service.fournisseurs.FournisseursService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fournisseurs")
public class FournisseursController {

    private final FournisseursService fournisseursService;

    public FournisseursController(FournisseursService fournisseursService) {
        this.fournisseursService = fournisseursService;
    }

    @GetMapping
    public String listFournisseurs(
            @PageableDefault(size = 10, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable,
            Model model) {
        model.addAttribute("page", fournisseursService.findAll(pageable));
        return "fournisseurs/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("fournisseur", new FournisseurRequestDto());
        model.addAttribute("typesFournisseur", fournisseursService.findAllTypes());
        return "fournisseurs/form";
    }

    @PostMapping("/save")
    public String saveFournisseur(@Valid @ModelAttribute("fournisseur") FournisseurRequestDto dto,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("typesFournisseur", fournisseursService.findAllTypes());
            return "fournisseurs/form";
        }
        fournisseursService.saveFromDto(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Fournisseur enregistré.");
        return "redirect:/fournisseurs";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("fournisseur", fournisseursService.toRequestDto(id));
        model.addAttribute("typesFournisseur", fournisseursService.findAllTypes());
        return "fournisseurs/form";
    }

    @PostMapping("/delete/{id}")
    public String deleteFournisseur(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        fournisseursService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Fournisseur supprimé.");
        return "redirect:/fournisseurs";
    }
}
