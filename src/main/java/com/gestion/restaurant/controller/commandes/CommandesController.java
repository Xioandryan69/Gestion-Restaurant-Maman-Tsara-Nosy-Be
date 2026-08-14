package com.gestion.restaurant.controller.commandes;

import com.gestion.restaurant.dto.commandes.CommandeCreateRequestDto;
import com.gestion.restaurant.dto.commandes.CommandeSearchCriteria;
import com.gestion.restaurant.service.commandes.CommandesService;
import com.gestion.restaurant.service.plats.PlatsService;

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
@RequestMapping("/commandes")
public class CommandesController {

    private final CommandesService commandesService;
    private final PlatsService platsService;

    public CommandesController(CommandesService commandesService,PlatsService platsService) {
        this.commandesService = commandesService;
        this.platsService = platsService;
    }

    @GetMapping
    public String list(@ModelAttribute("criteria") CommandeSearchCriteria criteria,
                       @PageableDefault(size = 10, sort = "dateCommande", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        var page = commandesService.search(criteria, pageable);
        model.addAttribute("page", page);
        model.addAttribute("zones", commandesService.findAllZones());
        model.addAttribute("clients", commandesService.findAllClients());
        // compute profit per commande for server-side display
        var profitMap = new java.util.HashMap<Long, java.math.BigDecimal>();
        for (var c : page.getContent()) {
            if (c != null && c.getId() != null) {
                try {
                    java.math.BigDecimal prof = commandesService.computeProfitForCommande(c.getId());
                    profitMap.put(c.getId(), prof);
                } catch (Exception ex) {
                    // ignore and leave absent
                }
            }
        }
        model.addAttribute("commandeProfitMap", profitMap);
        return "commandes/list";
    }

    @GetMapping("/new")
    public String showCreate(Model model) {
        model.addAttribute("commandeDto", new CommandeCreateRequestDto());
        model.addAttribute("clients", commandesService.findAllClients());
        model.addAttribute("zones", commandesService.findAllZones());
        model.addAttribute("plats", commandesService.findAllPlats());
        return "commandes/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("commandeDto") CommandeCreateRequestDto dto,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("clients", commandesService.findAllClients());
            model.addAttribute("zones", commandesService.findAllZones());
            model.addAttribute("plats", commandesService.findAllPlats());
            return "commandes/form";
        }
        commandesService.creerCommande(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Commande enregistrée avec succès.");
        return "redirect:/commandes";
    }

    @GetMapping("/edit/{id}")
    public String showEdit(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage",
                "La modification d'une commande facturée n'est pas autorisée. Consultez le détail ou supprimez puis recréez.");
        return "redirect:/commandes/" + id + "/detail";
    }

    @GetMapping({"/{id}/detail", "/detail/{id}"})
    public String showDetail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("commande", commandesService.findById(id));
        var details = commandesService.findDetailsByCommandeId(id);
        model.addAttribute("details", details);
        // compute profit per detail (per-unit and per-line) and total profit for the commande
        var detailUnitProfit = new java.util.HashMap<Long, java.math.BigDecimal>();
        var detailLineProfit = new java.util.HashMap<Long, java.math.BigDecimal>();
        for (var d : details) {
            try {
                if (d.getId() == null) continue;
                var plat = d.getPlat();
                java.math.BigDecimal quant = d.getQuantite() != null ? d.getQuantite() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal prixUnitaire = d.getPrixUnitaire() != null ? d.getPrixUnitaire() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal coutUnitaire = java.math.BigDecimal.ZERO;
                if (plat != null && plat.getId() != null) {
                    coutUnitaire = platsService.calculatePrixAchatPlat(plat.getId());
                }
                java.math.BigDecimal unitProfit = prixUnitaire.subtract(coutUnitaire);
                java.math.BigDecimal lineProfit = unitProfit.multiply(quant);
                detailUnitProfit.put(d.getId(), unitProfit);
                detailLineProfit.put(d.getId(), lineProfit);
            } catch (Exception ex) {
                // ignore per-line failures
            }
        }
        model.addAttribute("detailUnitProfit", detailUnitProfit);
        model.addAttribute("detailLineProfit", detailLineProfit);
        // total profit for the whole commande
        try {
            model.addAttribute("commandeProfitTotal", commandesService.computeProfitForCommande(id));
        } catch (Exception ex) {
            model.addAttribute("commandeProfitTotal", java.math.BigDecimal.ZERO);
        }
        return "commandes/detail";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        commandesService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Commande supprimée. Stock réintégré et sortie de caisse compensatoire enregistrée.");
        return "redirect:/commandes";
    }
}
