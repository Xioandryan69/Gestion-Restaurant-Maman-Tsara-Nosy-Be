package com.gestion.restaurant.controller.client;

import com.gestion.restaurant.dto.clients.ClientRequestDto;
import com.gestion.restaurant.dto.clients.ClientSearchCriteria;
import com.gestion.restaurant.service.clients.ClientsService;
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
@RequestMapping("/clients")
public class ClientsController {

    private final ClientsService clientsService;

    public ClientsController(ClientsService clientsService) {
        this.clientsService = clientsService;
    }

    @GetMapping
    public String listClients(@ModelAttribute("criteria") ClientSearchCriteria criteria,
                              @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                              Model model) {
        model.addAttribute("page", clientsService.search(criteria, pageable));
        model.addAttribute("typesClient", clientsService.findAllTypes());
        return "clients/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("client", new ClientRequestDto());
        model.addAttribute("typesClient", clientsService.findAllTypes());
        return "clients/form";
    }

    @PostMapping("/save")
    public String saveClient(@Valid @ModelAttribute("client") ClientRequestDto dto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("typesClient", clientsService.findAllTypes());
            return "clients/form";
        }
        clientsService.saveFromDto(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Client enregistré avec succès.");
        return "redirect:/clients";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("client", clientsService.findDtoById(id));
        model.addAttribute("typesClient", clientsService.findAllTypes());
        return "clients/form";
    }

    @PostMapping("/delete/{id}")
    public String deleteClient(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        clientsService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Client supprimé.");
        return "redirect:/clients";
    }
}
