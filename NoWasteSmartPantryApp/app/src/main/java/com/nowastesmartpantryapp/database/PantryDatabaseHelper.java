package com.nowastesmartpantryapp.database;
// ContentValues holds column->value pairs for INSERT/UPDATE.
import android.content.ContentValues;
// Context is needed by SQLiteOpenHelper to locate the database file.
import android.content.Context;
// Cursor iterates over the rows returned by a query.
import android.database.Cursor;
// SQLiteDatabase is the handle used to run SQL.
import android.database.sqlite.SQLiteDatabase;
// SQLiteOpenHelper manages creating, opening and upgrading the database.
import android.database.sqlite.SQLiteOpenHelper;

// Our model classes.
import com.nowastesmartpantryapp.Datamodels.PantryItem;
import com.nowastesmartpantryapp.Datamodels.Recipe;
import com.nowastesmartpantryapp.Datamodels.RecipeIngredient;

// Java collections.
import java.util.ArrayList;
import java.util.List;
public class PantryDatabaseHelper extends SQLiteOpenHelper {
    // File name of the database on the device.
    private static final String DB_NAME = "smart_pantry.db";
    // Schema version. Increase this number if the table structure ever changes (triggers onUpgrade).
    private static final int DB_VERSION = 1;

    // ---- Table and column name constants (avoids typos in SQL strings) ----
    private static final String T_PANTRY = "pantry_items";
    private static final String T_RECIPES = "recipes";
    private static final String T_INGREDIENTS = "recipe_ingredients";
    private static final String C_ID = "id";
    private static final String C_NAME = "name";
    private static final String C_QUANTITY = "quantity";
    private static final String C_UNIT = "unit";
    private static final String C_EXPIRY = "expiry_date";
    private static final String C_COOK = "cook_minutes";
    private static final String C_STEPS = "steps";
    private static final String C_RECIPE_ID = "recipe_id";

    // Constructor: passes the file name and version to the parent class.
    public PantryDatabaseHelper(Context context) {
        // The null argument means "use the default cursor factory".
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Called every time the database is opened, BEFORE onCreate/onUpgrade.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        // Parent implementation first.
        super.onConfigure(db);
        // Turn on foreign keys so deleting a recipe also deletes its ingredient rows (ON DELETE CASCADE).
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called ONCE, the very first time the database file is created (first run of the app).
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the pantry table. AUTOINCREMENT gives each row a unique id automatically.
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + C_NAME + " TEXT NOT NULL, "
                + C_QUANTITY + " REAL NOT NULL, "
                + C_UNIT + " TEXT NOT NULL, "
                + C_EXPIRY + " TEXT)");
        // Create the recipes table.
        db.execSQL("CREATE TABLE " + T_RECIPES + " ("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + C_NAME + " TEXT NOT NULL, "
                + C_COOK + " INTEGER NOT NULL, "
                + C_STEPS + " TEXT NOT NULL)");
        // Create the ingredients table: one row per ingredient line, linked to its recipe (one-to-many).
        db.execSQL("CREATE TABLE " + T_INGREDIENTS + " ("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + C_RECIPE_ID + " INTEGER NOT NULL, "
                + C_NAME + " TEXT NOT NULL, "
                + C_QUANTITY + " REAL NOT NULL, "
                + C_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + C_RECIPE_ID + ") REFERENCES " + T_RECIPES + "(" + C_ID + ") ON DELETE CASCADE)");
        // Pre-load (seed) the recipe collection so the app is useful on first run.
        seedRecipes(db);
    }

    // Called when DB_VERSION increases. For this simple app we just rebuild the tables.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop children first, then parents, then the pantry table.
        db.execSQL("DROP TABLE IF EXISTS " + T_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        // Recreate everything (and re-seed the recipes).
        onCreate(db);
    }

    // =====================================================================================
    // CRUD for pantry items
    // =====================================================================================

    // Builds the column->value map shared by insert and update.
    private ContentValues toValues(PantryItem item) {
        // ContentValues is a map of column names to values.
        ContentValues v = new ContentValues();
        // Name column.
        v.put(C_NAME, item.getName());
        // Quantity column.
        v.put(C_QUANTITY, item.getQuantity());
        // Unit column.
        v.put(C_UNIT, item.getUnit());
        // Expiry column: store NULL when there is no date.
        v.put(C_EXPIRY, item.hasExpiryDate() ? item.getExpiryDate() : null);
        // Return the map.
        return v;
    }

    // CREATE: inserts a new pantry item and returns its new id (or -1 on failure).
    public long insertItem(PantryItem item) {
        // Get a writable handle to the database.
        SQLiteDatabase db = getWritableDatabase();
        // Insert the row; SQLite generates the id.
        return db.insert(T_PANTRY, null, toValues(item));
    }

    // READ (list): returns every pantry item, sorted alphabetically (case-insensitive).
    public List<PantryItem> getAllItems() {
        // The list we will fill.
        List<PantryItem> items = new ArrayList<>();
        // Get a readable handle.
        SQLiteDatabase db = getReadableDatabase();
        // Run SELECT * FROM pantry_items ORDER BY name COLLATE NOCASE.
        Cursor c = db.query(T_PANTRY, null, null, null, null, null, C_NAME + " COLLATE NOCASE ASC");
        // try/finally guarantees the cursor is closed even if an error occurs.
        try {
            // Move through the rows one by one.
            while (c.moveToNext()) {
                // Convert the current row into a PantryItem and add it to the list.
                items.add(cursorToItem(c));
            }
        } finally {
            // Release the cursor's resources.
            c.close();
        }
        // Return the list (empty if the pantry has no items).
        return items;
    }

    // READ (single): returns one item by id, or null when it does not exist.
    public PantryItem getItem(long id) {
        // Readable handle.
        SQLiteDatabase db = getReadableDatabase();
        // SELECT * FROM pantry_items WHERE id = ?   (the ? is filled safely to avoid SQL injection).
        Cursor c = db.query(T_PANTRY, null, C_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            // If a row was found, convert it; otherwise return null.
            return c.moveToFirst() ? cursorToItem(c) : null;
        } finally {
            // Always close the cursor.
            c.close();
        }
    }

    // UPDATE: overwrites an existing item (matched by id). Returns the number of rows changed.
    public int updateItem(PantryItem item) {
        // Writable handle.
        SQLiteDatabase db = getWritableDatabase();
        // UPDATE pantry_items SET ... WHERE id = ?
        return db.update(T_PANTRY, toValues(item), C_ID + "=?", new String[]{String.valueOf(item.getId())});
    }

    // DELETE: removes an item by id. Returns the number of rows removed.
    public int deleteItem(long id) {
        // Writable handle.
        SQLiteDatabase db = getWritableDatabase();
        // DELETE FROM pantry_items WHERE id = ?
        return db.delete(T_PANTRY, C_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Converts the cursor's current row into a PantryItem object.
    private PantryItem cursorToItem(Cursor c) {
        // Read each column by looking up its index by name.
        long id = c.getLong(c.getColumnIndexOrThrow(C_ID));
        String name = c.getString(c.getColumnIndexOrThrow(C_NAME));
        double quantity = c.getDouble(c.getColumnIndexOrThrow(C_QUANTITY));
        String unit = c.getString(c.getColumnIndexOrThrow(C_UNIT));
        // The expiry column may be NULL, in which case getString returns null.
        String expiry = c.getString(c.getColumnIndexOrThrow(C_EXPIRY));
        // Build and return the model object.
        return new PantryItem(id, name, quantity, unit, expiry);
    }

    // =====================================================================================
    // Reading recipes
    // =====================================================================================

    // Returns every recipe together with its ingredient list.
    public List<Recipe> getAllRecipes() {
        // List to fill.
        List<Recipe> recipes = new ArrayList<>();
        // Readable handle.
        SQLiteDatabase db = getReadableDatabase();
        // SELECT * FROM recipes ORDER BY name.
        Cursor c = db.query(T_RECIPES, null, null, null, null, null, C_NAME + " COLLATE NOCASE ASC");
        try {
            // Loop through each recipe row.
            while (c.moveToNext()) {
                // Build a Recipe (this also loads its ingredients) and add it.
                recipes.add(cursorToRecipe(db, c));
            }
        } finally {
            // Close the cursor.
            c.close();
        }
        // Return all recipes.
        return recipes;
    }

    // Returns one recipe by id (used by the detail screen), or null when not found.
    public Recipe getRecipe(long id) {
        // Readable handle.
        SQLiteDatabase db = getReadableDatabase();
        // SELECT * FROM recipes WHERE id = ?
        Cursor c = db.query(T_RECIPES, null, C_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            // Convert the row if it exists.
            return c.moveToFirst() ? cursorToRecipe(db, c) : null;
        } finally {
            // Close the cursor.
            c.close();
        }
    }

    // Converts the current recipe row into a Recipe object, loading its ingredient rows too.
    private Recipe cursorToRecipe(SQLiteDatabase db, Cursor c) {
        // Read the recipe's own columns.
        long id = c.getLong(c.getColumnIndexOrThrow(C_ID));
        String name = c.getString(c.getColumnIndexOrThrow(C_NAME));
        int cook = c.getInt(c.getColumnIndexOrThrow(C_COOK));
        String steps = c.getString(c.getColumnIndexOrThrow(C_STEPS));
        // Load the matching ingredient rows (one-to-many relationship).
        List<RecipeIngredient> ingredients = new ArrayList<>();
        // SELECT * FROM recipe_ingredients WHERE recipe_id = ?
        Cursor ic = db.query(T_INGREDIENTS, null, C_RECIPE_ID + "=?", new String[]{String.valueOf(id)}, null, null, C_ID + " ASC");
        try {
            // Loop through the ingredient rows.
            while (ic.moveToNext()) {
                // Create a RecipeIngredient from each row.
                ingredients.add(new RecipeIngredient(
                        ic.getString(ic.getColumnIndexOrThrow(C_NAME)),
                        ic.getDouble(ic.getColumnIndexOrThrow(C_QUANTITY)),
                        ic.getString(ic.getColumnIndexOrThrow(C_UNIT))));
            }
        } finally {
            // Close the inner cursor.
            ic.close();
        }
        // Build the complete Recipe.
        return new Recipe(id, name, cook, steps, ingredients);
    }

    // =====================================================================================
    // Seeding: the 21 pre-loaded recipes
    // =====================================================================================

    /**
     * Inserts ONE recipe and its ingredients.
     * Each ingredient is written as a compact "name|quantity|unit" string, e.g. "egg|2|pcs".
     */
    private void addRecipe(SQLiteDatabase db, String name, int cookMinutes, String steps, String... ingredientSpecs) {
        // Insert the recipe row first.
        ContentValues rv = new ContentValues();
        rv.put(C_NAME, name);
        rv.put(C_COOK, cookMinutes);
        rv.put(C_STEPS, steps);
        // insert() returns the new recipe's id which the ingredient rows need as a foreign key.
        long recipeId = db.insert(T_RECIPES, null, rv);
        // Insert one row per ingredient spec.
        for (String spec : ingredientSpecs) {
            // Split "egg|2|pcs" into ["egg", "2", "pcs"].
            String[] parts = spec.split("\\|");
            // Fill the column values.
            ContentValues iv = new ContentValues();
            iv.put(C_RECIPE_ID, recipeId);
            iv.put(C_NAME, parts[0]);
            iv.put(C_QUANTITY, Double.parseDouble(parts[1]));
            iv.put(C_UNIT, parts[2]);
            // Insert the ingredient row.
            db.insert(T_INGREDIENTS, null, iv);
        }
    }

    // Inserts all seed recipes. Runs inside onCreate, which SQLite already wraps in a transaction.
    private void seedRecipes(SQLiteDatabase db) {
        // 1
        addRecipe(db, "Scrambled Eggs on Toast", 10,
                "Toast the bread.\nMelt the butter in a pan over medium heat.\nWhisk the eggs with the salt and pour into the pan.\nStir gently until just set, then spoon onto the toast.",
                "egg|2|pcs", "bread|2|pcs", "butter|10|g", "salt|0.25|tsp");
        // 2
        addRecipe(db, "Tomato Omelette", 15,
                "Chop the tomato and onion.\nHeat the oil and fry the onion and tomato for 3 minutes.\nWhisk the eggs with the salt and pour over the vegetables.\nCook until set, fold in half and serve.",
                "egg|3|pcs", "tomato|1|pcs", "onion|0.5|pcs", "oil|1|tbsp", "salt|0.25|tsp");
        // 3
        addRecipe(db, "Simple Pancakes", 20,
                "Mix the flour, sugar, egg and milk into a smooth batter.\nHeat a little butter in a pan.\nPour in a ladle of batter and cook 2 minutes per side.\nRepeat until the batter is finished.",
                "flour|200|g", "milk|300|ml", "egg|1|pcs", "sugar|1|tbsp", "butter|20|g");
        // 4
        addRecipe(db, "Cheese Toastie", 8,
                "Butter one side of each slice of bread.\nPlace the cheese between the slices, butter side out.\nFry in a pan for 2-3 minutes per side until golden and melted.",
                "bread|2|pcs", "cheese|50|g", "butter|10|g");
        // 5
        addRecipe(db, "Egg Fried Rice", 20,
                "Cook the rice and let it cool.\nHeat the oil and fry the chopped onion and carrot for 4 minutes.\nPush to the side, scramble the eggs in the pan.\nAdd the rice and soy sauce and stir-fry for 3 minutes.",
                "rice|150|g", "egg|2|pcs", "onion|1|pcs", "carrot|1|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");
        // 6
        addRecipe(db, "Fresh Tomato Pasta", 25,
                "Boil the pasta in salted water until tender.\nFry the chopped onion and garlic in the oil.\nAdd the chopped tomatoes and simmer for 10 minutes.\nToss the drained pasta in the sauce and serve.",
                "pasta|200|g", "tomato|4|pcs", "onion|1|pcs", "garlic|2|pcs", "oil|2|tbsp", "salt|0.5|tsp");
        // 7
        addRecipe(db, "Garlic Butter Pasta", 15,
                "Boil the pasta until tender and drain.\nMelt the butter and gently fry the crushed garlic for 1 minute.\nToss the pasta in the garlic butter, season with salt and serve.",
                "pasta|200|g", "garlic|3|pcs", "butter|30|g", "salt|0.5|tsp");
        // 8
        addRecipe(db, "Vegetable Soup", 35,
                "Peel and dice the potatoes, carrots and onion.\nPlace everything in a pot with the stock.\nSimmer for 25 minutes until soft.\nSeason with salt and serve hot.",
                "potato|2|pcs", "carrot|2|pcs", "onion|1|pcs", "vegetable stock|500|ml", "salt|0.5|tsp");
        // 9
        addRecipe(db, "Creamy Mashed Potatoes", 30,
                "Peel and cube the potatoes.\nBoil for 15-20 minutes until soft and drain.\nMash with the butter and warm milk.\nSeason with salt.",
                "potato|4|pcs", "butter|30|g", "milk|100|ml", "salt|0.5|tsp");
        // 10
        addRecipe(db, "Chickpea Curry", 30,
                "Fry the chopped onion and garlic in the oil.\nStir in the curry powder for 1 minute.\nAdd the chopped tomatoes and chickpeas.\nSimmer for 15 minutes and serve.",
                "chickpea|400|g", "tomato|2|pcs", "onion|1|pcs", "garlic|2|pcs", "curry powder|2|tsp", "oil|1|tbsp");
        // 11
        addRecipe(db, "Lentil Stew", 40,
                "Rinse the lentils.\nPlace lentils, diced carrots, onion and tomatoes in a pot.\nCover with water and simmer for 30 minutes.\nSeason with salt and serve.",
                "lentil|200|g", "carrot|2|pcs", "onion|1|pcs", "tomato|2|pcs", "salt|1|tsp");
        // 12
        addRecipe(db, "Creamy Porridge", 10,
                "Combine the oats and milk in a pot.\nStir over medium heat for 5 minutes until thick.\nSweeten with the sugar and serve warm.",
                "oats|80|g", "milk|300|ml", "sugar|1|tsp");
        // 13
        addRecipe(db, "Banana Smoothie", 5,
                "Peel the bananas.\nBlend the bananas, milk and honey until smooth.\nServe immediately.",
                "banana|2|pcs", "milk|250|ml", "honey|1|tbsp");
        // 14
        addRecipe(db, "Peanut Butter Sandwich", 3,
                "Spread the peanut butter evenly on one slice of bread.\nTop with the second slice and cut in half.",
                "bread|2|pcs", "peanut butter|2|tbsp");
        // 15
        addRecipe(db, "Pap and Tomato Gravy", 35,
                "Fry the chopped onion in the oil until soft.\nAdd the chopped tomatoes and salt and simmer for 15 minutes into a gravy.\nBoil water and stir in the maize meal; cook covered for 15 minutes to make stiff pap.\nServe the gravy over the pap.",
                "maize meal|250|g", "tomato|3|pcs", "onion|1|pcs", "oil|1|tbsp", "salt|0.5|tsp");
        // 16
        addRecipe(db, "Egg Fried Noodles", 15,
                "Cook the noodles and drain.\nFry the crushed garlic in the oil for 30 seconds.\nScramble the eggs into the pan.\nAdd the noodles and soy sauce and toss for 2 minutes.",
                "noodle|200|g", "egg|2|pcs", "soy sauce|2|tbsp", "oil|1|tbsp", "garlic|1|pcs");
        // 17
        addRecipe(db, "Baked Beans on Toast", 8,
                "Toast the bread and butter it.\nHeat the baked beans in a small pot for 3 minutes.\nSpoon the beans over the toast.",
                "baked bean|200|g", "bread|2|pcs", "butter|10|g");
        // 18
        addRecipe(db, "Spiced Potato Wedges", 40,
                "Heat the oven to 220 C.\nCut the potatoes into wedges and toss with the oil, salt and paprika.\nBake on a tray for 30 minutes, turning once.",
                "potato|3|pcs", "oil|2|tbsp", "salt|0.5|tsp", "paprika|1|tsp");
        // 19
        addRecipe(db, "Cheese Omelette", 10,
                "Whisk the eggs with the salt.\nMelt the butter in a pan and pour in the eggs.\nWhen almost set, sprinkle over the grated cheese and fold.\nServe at once.",
                "egg|3|pcs", "cheese|40|g", "butter|10|g", "salt|0.25|tsp");
        // 20
        addRecipe(db, "Rice Pudding", 45,
                "Place the rice, milk and sugar in a pot.\nSimmer gently for 35 minutes, stirring often, until thick and creamy.\nServe warm or cold.",
                "rice|100|g", "milk|500|ml", "sugar|2|tbsp");
        // 21
        addRecipe(db, "Quick Vegetable Stir-fry", 15,
                "Slice the carrots, onion and pepper into thin strips.\nHeat the oil and fry the garlic for 30 seconds.\nAdd the vegetables and stir-fry for 5 minutes.\nSplash in the soy sauce and serve.",
                "carrot|2|pcs", "onion|1|pcs", "bell pepper|1|pcs", "garlic|1|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");
    }
}


