package com.gestion.restaurant.exception;

import com.gestion.restaurant.dto.ingredients.IngredientManquantDto;
import java.util.List;

public class StockInsuffisantException extends BusinessRuleException {
    private final List<IngredientManquantDto> ingredientsManquants;
    public StockInsuffisantException(List<IngredientManquantDto> ingredientsManquants) {
        super("Stock insuffisant : ajoutez les ingrédients manquants avant de créer la commande.", "/commandes/new");
        this.ingredientsManquants = List.copyOf(ingredientsManquants);
    }
    public List<IngredientManquantDto> getIngredientsManquants() { return ingredientsManquants; }
}
