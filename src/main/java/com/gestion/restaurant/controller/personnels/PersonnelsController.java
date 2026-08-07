package com.gestion.restaurant.controller.personnels;

import com.gestion.restaurant.dto.personnels.PersonnelRequestDto;
import com.gestion.restaurant.dto.personnels.PersonnelSearchCriteria;
import com.gestion.restaurant.service.personnels.PersonnelsService;
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
@RequestMapping("/personnels")
public class PersonnelsController {

    private final PersonnelsService personnelsService;

    public PersonnelsController(PersonnelsService personnelsService) {
        this.personnelsService = personnelsService;
    }

    @GetMapping
    public String list(@ModelAttribute("criteria") PersonnelSearchCriteria criteria,
                       @PageableDefault(size = 10, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable,
                       Model model) {
        model.addAttribute("page", personnelsService.search(criteria, pageable));
        model.addAttribute("roles", personnelsService.findAllRoles());
        return "personnels/list";
    }

    @GetMapping("/new")
    public String showCreate(Model model) {
        model.addAttribute("personnel", new PersonnelRequestDto());
        model.addAttribute("roles", personnelsService.findAllRoles());
        return "personnels/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("personnel") PersonnelRequestDto dto,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", personnelsService.findAllRoles());
            return "personnels/form";
        }
        var saved = personnelsService.saveFromDto(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Personnel enregistré.");
        return "redirect:/personnels/" + saved.getId() + "/detail";
    }

    @GetMapping("/edit/{id}")
    public String showEdit(@PathVariable("id") Long id, Model model) {
        model.addAttribute("personnel", personnelsService.findDtoById(id));
        model.addAttribute("roles", personnelsService.findAllRoles());
        return "personnels/form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        personnelsService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Personnel supprimé.");
        return "redirect:/personnels";
    }

    @GetMapping({"/{id}/detail", "/detail/{id}"})
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("personnel", personnelsService.findById(id));
        model.addAttribute("historiquePaie", personnelsService.findHistoriquePaie(id));
        model.addAttribute("absencesList", personnelsService.findAbsences(id));
        model.addAttribute("raisonsAbsence", personnelsService.findAllRaisonsAbsence());
        return "personnels/detail";
    }

    @PostMapping("/{id}/paie/save")
    public String genererPaie(@PathVariable("id") Long id,
                              @RequestParam("salaire") BigDecimal salaire,
                              @RequestParam(value = "prime", required = false) BigDecimal prime,
                              @RequestParam(value = "datePaie", required = false)
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datePaie,
                              RedirectAttributes redirectAttributes) {
        personnelsService.genererFichePaie(id, salaire, prime, datePaie);
        redirectAttributes.addFlashAttribute("successMessage", "Fiche de paie générée.");
        return "redirect:/personnels/" + id + "/detail";
    }

    @PostMapping("/{id}/absence/save")
    public String enregistrerAbsence(@PathVariable("id") Long id,
                                     @RequestParam("dateDebut")
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                     @RequestParam("dateFin")
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                     @RequestParam("idRaison") Long idRaison,
                                     @RequestParam(value = "commentaire", required = false) String commentaire,
                                     RedirectAttributes redirectAttributes) {
        personnelsService.enregistrerAbsence(id, dateDebut, dateFin, idRaison, commentaire);
        redirectAttributes.addFlashAttribute("successMessage", "Absence enregistrée.");
        return "redirect:/personnels/" + id + "/detail";
    }
}
