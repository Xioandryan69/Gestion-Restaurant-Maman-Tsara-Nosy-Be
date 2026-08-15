package com.gestion.restaurant.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.gestion.restaurant.service.ingredients.IngredientsService;

@ControllerAdvice
public class ThymeleafModelAttributes {
    private final IngredientsService ingredientsService;

    public ThymeleafModelAttributes(IngredientsService ingredientsService) {
        this.ingredientsService = ingredientsService;
    }

    @ModelAttribute("request")
    public HttpServletRequest httpServletRequest(HttpServletRequest request) {
        return request;
    }

    @ModelAttribute("nombreAlertesStock")
    public int nombreAlertesStock() {
        return ingredientsService.findIngredientsEnAlerte().size();
    }
}
