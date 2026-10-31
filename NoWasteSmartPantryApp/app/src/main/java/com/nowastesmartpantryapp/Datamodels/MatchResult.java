package com.nowastesmartpantryapp.Datamodels;

import java.util.List;

public class MatchResult {
    // The recipe that was tested.
    private final Recipe recipe;
    // Names of the ingredients the pantry cannot cover (empty list = perfect match).
    private final List<String> missing;

    // Constructor: stores the recipe and its list of missing ingredients.
    public MatchResult(Recipe recipe, List<String> missing) {
        // Save the tested recipe.
        this.recipe = recipe;
        // Save the missing ingredient names.
        this.missing = missing;
    }

    // Getter: the recipe.
    public Recipe getRecipe() {
        return recipe;
    }

    // Getter: the missing ingredient names.
    public List<String> getMissing() {
        return missing;
    }

    // THE STRICT RULE: a recipe is "suggested" only when NOTHING is missing.
    public boolean isStrictMatch() {
        return missing.isEmpty();
    }

    // Bonus "Almost there" rule: exactly one ingredient is missing.
    public boolean isAlmostThere() {
        return missing.size() == 1;
    }
}


