#  No Waste Smart Pantry Manager

A Java Android application that helps reduce food waste. The user records the ingredients they
already have at home (their *pantry*) and the app suggests **only** recipes they can cook right now,
with no shopping trip needed.


## Core feature: the strict-matching rule

A recipe is suggested **only if every ingredient it needs is in the pantry in at least the required quantity.**

* 5 ingredients needed, 4 in the pantry -> the recipe is **not** suggested.
  * `Tomato` = `tomatoes` = ` TOMATOES ` (case, spaces and plural/singular are normalised)
  * `0.5 kg` flour is satisfied by `600 g` flour (unit conversion: g/kg, ml/l/tsp/tbsp/cup, pcs)
  * Two pantry rows of the same ingredient are added together
  * Expired pantry items are ignored
* A separate **"Almost there"** list shows recipes missing exactly one ingredient (can be
  switched off in Settings). It is never mixed into the strict list.

## Features

| Requirement | Where |
|---|---|
| Pantry add / edit / delete (name, quantity, unit, optional expiry) | `PantryListActivity`, `AddEditIngredientActivity` |
| Pantry list in a RecyclerView with a custom adapter | `PantryAdapter` |
| 21 seeded recipes (name, ingredients, steps) | `PantryDatabaseHelper.seedRecipes()` |
| Suggested Recipes screen (strict matching) | `SuggestedRecipesActivity`, `RecipeMatcher` |
| Recipe detail (ingredients with tick/cross + numbered method) | `RecipeDetailActivity` |
| Settings (expiring-soon alerts, show "Almost there") | `SettingsActivity`, `SettingsManager` |
| Empty state: "No recipes match your pantry yet - add more ingredients" | `SuggestedRecipesActivity` |
| Input validation (required name, positive number quantity, etc.) | `AddEditIngredientActivity.saveItem()` |
| Navigation: toolbar menu + Up arrows + Intents with extras | `menu_main.xml`, activities |

The app uses **no** Google Maps, mapping SDK, GPS or location, and requests **no** permissions.

## Database choice: SQLite

I chose **SQLite** (via `SQLiteOpenHelper`) because:

1. The pantry is personal data that should work **offline**, in the kitchen, with no internet.
2. It is built into Android, so there is no server, account or API key to set up, which makes the
   project easy for a marker to run.
3. The data is small and relational (recipes have many ingredients), which SQLite handles well.
4. It is the approach taught in the module's persistent-data chapter, and I can explain it end-to-end.

Tables: `pantry_items`, `recipes`, `recipe_ingredients` (foreign key to `recipes`, `ON DELETE CASCADE`).
Data is stored in the app's private storage, so it persists after the app is closed and reopened.

## Setup / run instructions

**Requirements:** Android Studio (Rabit 2026), JDK 17 (bundled with Android Studio), Android SDK 34.

1. Clone the repository:
   `git clone <https://github.com/K2983Moeng/Food-Save-Smart-Pantry-Manager.git>`
2. In Android Studio choose **File > Open** and select the `FoodSaveSmartPantryManager` folder.
3. Wait for **Gradle Sync** to finish (it downloads the dependencies automatically; internet needed once).
4. App tested by pluging in a Xiaomi phone with USB debugging.(an emulator will also work)
5. Press the green **Run** button.

### Quick test script (for the marker)

1. Tap **+**, add `Bread` 2 pcs and `Peanut Butter` 2 tbsp -> **Suggested** shows *Peanut Butter Sandwich*.
2. Edit `Peanut Butter` and set it to 1 tbsp -> the sandwich disappears (quantity too low).
3. Delete `Bread` -> no recipes match and the empty-state message appears.
4. Add `Bread` 2 pcs, `Cheese` 50 g (no butter) -> *Cheese Toastie* appears under **Almost there** (missing: butter) but **not** in the strict list.
5. Close and reopen the app -> your pantry is still there.

## Project structure

```
app/src/main/java/com/katlego/smartpantry/
  activity/   PantryListActivity, AddEditIngredientActivity, SuggestedRecipesActivity,
              RecipeDetailActivity, SettingsActivity
  adapter/    PantryAdapter, RecipeAdapter
  database/   PantryDatabaseHelper  (SQLite schema, CRUD, seed data)
  logic/      RecipeMatcher (strict rule), UnitConverter, DateUtils, SettingsManager
  model/      PantryItem, Recipe, RecipeIngredient, MatchResult
app/src/main/res/  layouts, menu, strings, colours, theme
```

## Known limitations

* An ingredient stored in a different *kind* of unit from the recipe (e.g. onions as `500 g` when the
  recipe needs `2 pcs`) is treated as not matching, because the app cannot know an onion's weight.
* Plural handling is rule-based (not full language processing), e.g. irregular plurals are not covered.

## Author

Katlego Moeng - BSc Information Technology.
