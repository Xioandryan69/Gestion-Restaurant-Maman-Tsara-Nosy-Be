package com.gestion.restaurant.controller.api;

import com.gestion.restaurant.dto.caisse.CaisseSummaryDTO;
import com.gestion.restaurant.dto.ingredients.IngredientMovementSummaryDTO;
import com.gestion.restaurant.entity.materielles.HistoriqueMaterielles;
import com.gestion.restaurant.entity.materielles.MaintenanceMaterielles;
import com.gestion.restaurant.service.caisse.CaisseService;
import com.gestion.restaurant.service.commandes.CommandesService;
import com.gestion.restaurant.service.ingredients.IngredientsService;
import com.gestion.restaurant.service.materielles.MateriellesService;
import com.gestion.restaurant.service.plats.PlatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class StatsController {

    private final MateriellesService materiellesService;
    private final IngredientsService ingredientsService;
    private final PlatsService platsService;
    private final CommandesService commandesService;
    private final CaisseService caisseService;

    public StatsController(MateriellesService materiellesService,
                           IngredientsService ingredientsService,
                           PlatsService platsService,
                           CommandesService commandesService,
                           CaisseService caisseService) {
        this.materiellesService = materiellesService;
        this.ingredientsService = ingredientsService;
        this.platsService = platsService;
        this.commandesService = commandesService;
        this.caisseService = caisseService;
    }

    @GetMapping("/api/stats/materielles/range")
    public Map<String, Object> materielRange(
            @RequestParam("debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        Map<String, Object> m = new HashMap<>();
        List<HistoriqueMaterielles> achats = materiellesService.getAchatMateriellesBetween(debut, fin);
        List<MaintenanceMaterielles> maints = materiellesService.getMaintenancesBetween(debut, fin);
        m.put("achats", achats);
        m.put("maintenances", maints);
        return m;
    }

    @GetMapping("/api/stats/materielles/year/{year}/hors-service")
    public Map<String, Object> materielHorsService(@PathVariable("year") int year) {
        Map<String, Object> m = new HashMap<>();
        long count = materiellesService.countMateriellesHorsServiceForYear(year);
        m.put("year", year);
        m.put("horsServiceCount", count);
        return m;
    }

    @GetMapping("/api/stats/ingredients/year/{year}")
    public List<IngredientMovementSummaryDTO> ingredientsYear(@PathVariable("year") int year) {
        return ingredientsService.getIngredientMovementSummaryForYear(year);
    }

@GetMapping("/api/stats/plat/{id}/cost") // <-- Spécifie bien l'URL avec {id}
    public Map<String, Object> platCost(@PathVariable("id") String id) {
        Map<String, Object> m = new HashMap<>();
        try {
            Long lid = Long.parseLong(id);
            m.put("prixAchat", platsService.calculatePrixAchatPlat(lid));
            m.put("profitPerUnit", platsService.calculateProfitPerUnit(lid));
        } catch (NumberFormatException ex) {
            m.put("error", "invalid id: " + id);
        }
        return m;
    }

    @GetMapping("/api/stats/commandes/{id}/profit")
    public Map<String, Object> commandeProfit(@PathVariable("id") Long id) {
        Map<String, Object> m = new HashMap<>();
        m.put("profit", commandesService.computeProfitForCommande(id));
        return m;
    }

    @GetMapping("/api/stats/caisse/year/{year}")
    public CaisseSummaryDTO caisseYear(@PathVariable("year") int year) {
        return caisseService.getSummaryForYear(year);
    }
}
