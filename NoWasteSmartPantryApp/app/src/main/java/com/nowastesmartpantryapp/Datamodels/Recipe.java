package com.nowastesmartpantryapp.Datamodels;

import java.util.List;

public class Recipe {
    // Database primary key.
    private final long id;
    // Recipe title, e.g. "Cheese Toastie".
    private final String name;
    // Minutes needed to cook the recipe.
    private final int cookMinutes;
    // The method: one step per line, separated by "\n".
    private final String steps;
    // Every ingredient the recipe requires.
    private final List<RecipeIngredient> ingredients;

    // Constructor: sets every field once.
    public Recipe(long id, String name, int cookMinutes, String steps, List<RecipeIngredient> ingredients) {
        // Save the id.
        this.id = id;
        // Save the title.
        this.name = name;
        // Save the cooking time.
        this.cookMinutes = cookMinutes;
        // Save the method text.
        this.steps = steps;
        // Save the ingredient list.
        this.ingredients = ingredients;
    }

    // Getter: id.
    public long getId() {
        return id;
    }

    // Getter: name.
    public String getName() {
        return name;
    }

    // Getter: cooking minutes.
    public int getCookMinutes() {
        return cookMinutes;
    }

    // Getter: method text (steps separated by newlines).
    public String getSteps() {
        return steps;
    }

    // Getter: the required ingredients.
    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }
}


