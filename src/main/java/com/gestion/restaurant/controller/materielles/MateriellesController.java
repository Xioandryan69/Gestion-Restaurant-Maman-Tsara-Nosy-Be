package com.gestion.restaurant.controller.materielles;

import com.gestion.restaurant.dto.materielles.MaterielRequestDto;
import com.gestion.restaurant.dto.materielles.MaterielSearchCriteria;
import com.gestion.restaurant.service.materielles.MateriellesService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/materielles")
public class MateriellesController {

    private final MateriellesService materiellesService;

    public MateriellesController(MateriellesService materiellesService) {
        this.materiellesService = materiellesService;
    }

    @GetMapping
    public String listMaterielles(@ModelAttribute("criteria") MaterielSearchCriteria criteria,
                                  @PageableDefault(size = 10, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable,
                                  Model model) {
        model.addAttribute("page", materiellesService.search(criteria, pageable));
        model.addAttribute("categories", materiellesService.findAllCategories());
        model.addAttribute("statuts", materiellesService.findAllStatuts());
        model.addAttribute("selectedCategorie", criteria != null ? criteria.getIdCategorie() : null);
        model.addAttribute("selectedStatut", criteria != null ? criteria.getIdStatut() : null);
        return "materielles/list";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", materiellesService.getDashboardData());
        return "materielles/dashboard";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("materiel", new MaterielRequestDto());
        model.addAttribute("categories", materiellesService.findAllCategories());
        model.addAttribute("statuts", materiellesService.findAllStatuts());
        return "materielles/form";
    }

    @PostMapping("/save")
    public String saveMateriel(@Valid @ModelAttribute("materiel") MaterielRequestDto dto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", materiellesService.findAllCategories());
            model.addAttribute("statuts", materiellesService.findAllStatuts());
            return "materielles/form";
        }
        var enregistre = materiellesService.saveFromDto(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Matériel enregistré.");
        return "redirect:/materielles/" + enregistre.getId() + "/detail";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("materiel", materiellesService.toRequestDto(id));
        model.addAttribute("categories", materiellesService.findAllCategories());
        model.addAttribute("statuts", materiellesService.findAllStatuts());
        return "materielles/form";
    }

    @PostMapping("/delete/{id}")
    public String deleteMateriel(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        materiellesService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Matériel supprimé.");
        return "redirect:/materielles";
    }

    @GetMapping("/{id}/detail")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("materiel", materiellesService.findById(id));
        model.addAttribute("historiqueList", materiellesService.findHistorique(id));
        model.addAttribute("maintenancesList", materiellesService.findMaintenances(id));
        model.addAttribute("inventaireList", materiellesService.findInventaire(id));
        model.addAttribute("stockActuel", materiellesService.getStockActuel(id));
        model.addAttribute("fournisseurs", materiellesService.findAllFournisseurs());
        return "materielles/detail";
    }

    @PostMapping("/{id}/historique/save")
    public String saveAchat(@PathVariable("id") Long id,
                            @RequestParam("quantite") BigDecimal quantite,
                            @RequestParam("prixAchat") BigDecimal prixAchat,
                            @RequestParam(value = "idFournisseur", required = false) Long idFournisseur,
                            @RequestParam("dateEntree") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEntree,
                            RedirectAttributes redirectAttributes) {
        materiellesService.enregistrerAchat(id, dateEntree, quantite, prixAchat, idFournisseur);
        redirectAttributes.addFlashAttribute("successMessage", "Achat matériel enregistré.");
        return "redirect:/materielles/" + id + "/detail";
    }

    @PostMapping("/{id}/maintenance/save")
    public String saveMaintenance(@PathVariable("id") Long id,
                                  @RequestParam("description") String description,
                                  @RequestParam("cout") BigDecimal cout,
                                  @RequestParam(value = "technicien", required = false) String technicien,
                                  @RequestParam("dateMaintenance")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateMaintenance,
                                  RedirectAttributes redirectAttributes) {
        materiellesService.enregistrerMaintenance(id, dateMaintenance, description, cout, technicien);
        redirectAttributes.addFlashAttribute("successMessage", "Maintenance enregistrée.");
        return "redirect:/materielles/" + id + "/detail";
    }

    @PostMapping("/{id}/hors-service")
    public String horsService(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        materiellesService.mettreHorsService(id);
        redirectAttributes.addFlashAttribute("successMessage", "Matériel mis hors service.");
        return "redirect:/materielles/" + id + "/detail";
    }
}
