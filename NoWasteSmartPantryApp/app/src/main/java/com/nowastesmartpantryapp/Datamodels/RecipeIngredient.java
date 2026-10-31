package com.nowastesmartpantryapp.Datamodels;

public class RecipeIngredient {
    // The ingredient name as written in the recipe (e.g. "tomato").
    private final String name;
    // The amount the recipe needs (e.g. 2).
    private final double quantity;
    // The unit of that amount (e.g. "pcs", "g", "tbsp").
    private final String unit;

    // Constructor: all three values are required and never change afterwards (final fields).
    public RecipeIngredient(String name, double quantity, String unit) {
        // Save the ingredient name.
        this.name = name;
        // Save the required quantity.
        this.quantity = quantity;
        // Save the unit.
        this.unit = unit;
    }

    // Getter: ingredient name.
    public String getName() {
        return name;
    }

    // Getter: required quantity.
    public double getQuantity() {
        return quantity;
    }

    // Getter: unit of the required quantity.
    public String getUnit() {
        return unit;
    }
}

