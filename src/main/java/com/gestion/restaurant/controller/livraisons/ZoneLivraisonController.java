package com.gestion.restaurant.controller.livraisons;

import com.gestion.restaurant.dto.livraisons.ZoneLivraisonDto;
import com.gestion.restaurant.dto.livraisons.ZoneLivraisonFilterDto;
import com.gestion.restaurant.service.livraisons.ZoneLivraisonService;
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
@RequestMapping("/zones-livraison")
public class ZoneLivraisonController {

    private final ZoneLivraisonService service;

    public ZoneLivraisonController(ZoneLivraisonService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@ModelAttribute("filter") ZoneLivraisonFilterDto filter,
                       @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
                       Model model) {
        model.addAttribute("page", service.findAll(filter, pageable));
        return "zones-livraison/index";
    }

    @GetMapping("/creer")
    public String createForm(Model model) {
        model.addAttribute("zone", new ZoneLivraisonDto());
        return "zones-livraison/form";
    }

    @GetMapping("/modifier/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("zone", service.toDto(service.findById(id)));
        return "zones-livraison/form";
    }

    @PostMapping("/enregistrer")
    public String save(@Valid @ModelAttribute("zone") ZoneLivraisonDto dto,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "zones-livraison/form";
        }

        try {
            service.save(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Zone de livraison enregistrée avec succès.");
        } catch (com.gestion.restaurant.exception.BusinessRuleException e) {
            result.rejectValue("libelle", "error.zone", e.getMessage());
            return "zones-livraison/form";
        }

        return "redirect:/zones-livraison";
    }

    @PostMapping("/supprimer/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Zone de livraison supprimée.");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Impossible de supprimer cette zone (données liées).");
        }
        return "redirect:/zones-livraison";
    }
}