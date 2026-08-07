package com.gestion.restaurant.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Ressource introuvable : {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(BusinessRuleException.class)
    public String handleBusinessRule(BusinessRuleException ex,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request) {
        log.info("Règle métier : {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:" + resolveRedirect(ex.getRedirectUrl(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex,
                                        RedirectAttributes redirectAttributes,
                                        HttpServletRequest request) {
        log.info("Argument invalide : {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage",
                ex.getMessage() != null ? ex.getMessage() : "Données invalides.");
        return "redirect:" + resolveRedirect(null, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrity(DataIntegrityViolationException ex,
                                      RedirectAttributes redirectAttributes,
                                      HttpServletRequest request) {
        log.warn("Violation d'intégrité : {}", ex.getMostSpecificCause().getMessage());
        redirectAttributes.addFlashAttribute("errorMessage",
                "Impossible d'effectuer cette opération : des données liées existent encore.");
        return "redirect:" + resolveRedirect(null, request);
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Erreur inattendue", ex);
        model.addAttribute("errorMessage",
                "Une erreur inattendue est survenue. Veuillez réessayer ou contacter l'administrateur.");
        return "error/500";
    }

    private String resolveRedirect(String preferred, HttpServletRequest request) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.startsWith("/") ? preferred : "/" + preferred;
        }
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            try {
                String path = URI.create(referer).getPath();
                // Chemin relatif sûr uniquement (pas d'open-redirect / protocol-relative)
                if (path != null
                        && path.startsWith("/")
                        && !path.startsWith("//")
                        && !path.startsWith("/error")
                        && path.indexOf('\\') < 0) {
                    return path;
                }
            } catch (IllegalArgumentException ignored) {
                // fallback dashboard
            }
        }
        return "/dashboard";
    }
}
