package com.gestion.restaurant.controller.commandes;

import com.gestion.restaurant.dto.commandes.CommandeCreateRequestDto;
import com.gestion.restaurant.service.commandes.CommandesService;
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

    public CommandesController(CommandesService commandesService) {
        this.commandesService = commandesService;
    }

    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        model.addAttribute("page", commandesService.findAll(pageable));
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
        model.addAttribute("details", commandesService.findDetailsByCommandeId(id));
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
