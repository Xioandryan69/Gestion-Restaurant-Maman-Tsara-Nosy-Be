package com.gestion.restaurant.controller.caisse;

import com.gestion.restaurant.dto.caisse.MouvementCaisseRequestDto;
import com.gestion.restaurant.dto.caisse.MouvementCaisseSearchCriteria;
import com.gestion.restaurant.service.caisse.CaisseService;
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
@RequestMapping("/caisse")
public class CaisseController {

    private final CaisseService caisseService;

    public CaisseController(CaisseService caisseService) {
        this.caisseService = caisseService;
    }

    @GetMapping
    public String list(@ModelAttribute("criteria") MouvementCaisseSearchCriteria criteria,
                       @PageableDefault(size = 10, sort = "dateMouvement", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        model.addAttribute("page", caisseService.search(criteria, pageable));
        model.addAttribute("typesMouvement", caisseService.findAllTypes());
        return "caisse/list";
    }

    @GetMapping("/new")
    public String showCreate(Model model) {
        model.addAttribute("mouvement", new MouvementCaisseRequestDto());
        model.addAttribute("typesMouvement", caisseService.findAllTypes());
        return "caisse/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("mouvement") MouvementCaisseRequestDto dto,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("typesMouvement", caisseService.findAllTypes());
            return "caisse/form";
        }
        caisseService.saveFromDto(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Mouvement de caisse enregistré.");
        return "redirect:/caisse";
    }

    @GetMapping("/edit/{id}")
    public String showEdit(@PathVariable("id") Long id, Model model) {
        model.addAttribute("mouvement", caisseService.toRequestDto(id));
        model.addAttribute("typesMouvement", caisseService.findAllTypes());
        return "caisse/form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        caisseService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Mouvement de caisse supprimé.");
        return "redirect:/caisse";
    }
    
}
