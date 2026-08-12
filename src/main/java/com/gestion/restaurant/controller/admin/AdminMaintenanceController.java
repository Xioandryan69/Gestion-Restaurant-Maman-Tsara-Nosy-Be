package com.gestion.restaurant.controller.admin;

import com.gestion.restaurant.service.admin.DatabaseMaintenanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Page d'administration "Zone dangereuse" : vidage complet de la base
 * et reset des séquences. Protégée par l'authentification globale
 * (SecurityConfig : anyRequest().authenticated(), un seul compte ADMIN existe).
 * <p>
 * Chaque action nécessite en plus une confirmation textuelle exacte
 * (mot-clé tapé par l'utilisateur), vérifiée côté serveur, pour éviter
 * qu'un simple clic accidentel ou un confirm() JS contourné ne déclenche
 * une perte de données irréversible.
 */
@Controller
@RequestMapping("/admin/maintenance")
public class AdminMaintenanceController {

    private static final String MOT_CLE_TRUNCATE = "SUPPRIMER";
    private static final String MOT_CLE_RESET_SEQ = "RESET";

    private final DatabaseMaintenanceService maintenanceService;

    public AdminMaintenanceController(DatabaseMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("tables", maintenanceService.listUserTables());
        model.addAttribute("motCleTruncate", MOT_CLE_TRUNCATE);
        model.addAttribute("motCleResetSeq", MOT_CLE_RESET_SEQ);
        return "admin/maintenance";
    }

    @PostMapping("/truncate")
    public String truncate(@RequestParam("confirmation") String confirmation,
                            RedirectAttributes redirectAttributes) {
        if (!MOT_CLE_TRUNCATE.equals(confirmation)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Confirmation invalide : tape exactement \"" + MOT_CLE_TRUNCATE + "\" pour valider.");
            return "redirect:/admin/maintenance";
        }
        int nbTables = maintenanceService.truncateAllTables();
        redirectAttributes.addFlashAttribute("successMessage",
                nbTables + " table(s) vidée(s) et compteurs remis à zéro.");
        return "redirect:/admin/maintenance";
    }

    @PostMapping("/reset-sequences")
    public String resetSequences(@RequestParam("confirmation") String confirmation,
                                  RedirectAttributes redirectAttributes) {
        if (!MOT_CLE_RESET_SEQ.equals(confirmation)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Confirmation invalide : tape exactement \"" + MOT_CLE_RESET_SEQ + "\" pour valider.");
            return "redirect:/admin/maintenance";
        }
        int nbSeq = maintenanceService.resetAllSequencesToZero();
        redirectAttributes.addFlashAttribute("successMessage",
                nbSeq + " séquence(s) remise(s) à 0.");
        return "redirect:/admin/maintenance";
    }
}