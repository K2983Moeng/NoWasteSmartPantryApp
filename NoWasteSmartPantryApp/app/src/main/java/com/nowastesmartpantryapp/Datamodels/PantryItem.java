

    // Package declaration: this class belongs to the "model" package (plain data classes).
package com.nowastesmartpantryapp.Datamodels;

// Import our helper that works with date strings.

    import android.text.format.DateUtils;

    /**
     * PantryItem is a simple data holder (a "model" or POJO) that represents ONE row of the
     * pantry_items database table, e.g. "Tomato, 4 pcs, expires 2026-10-25".
     */
    public class PantryItem {

        // Primary key from the database. -1 means "not saved yet".
        private long id;
        // The ingredient name as typed by the user (e.g. "Tomatoes").
        private String name;
        // How much the user has (e.g. 500).
        private final double quantity;
        // The unit the quantity is measured in (e.g. "g", "ml", "pcs").
        private final String unit;
        // Optional expiry date in yyyy-MM-dd format; null/empty when the user did not set one.
        private final String expiryDate;

        // Constructor used when we know every field (e.g. when reading from the database).
        public PantryItem(long id, String name, double quantity, String unit, String expiryDate) {
            // Store the database id.
            this.id = id;
            // Store the ingredient name.
            this.name = name;
            // Store the quantity.
            this.quantity = quantity;
            // Store the unit.
            this.unit = unit;
            // Store the (possibly null) expiry date.
            this.expiryDate = expiryDate;
        }

        // Convenience constructor for a brand-new item that has no database id yet.
        public PantryItem(String name, double quantity, String unit, String expiryDate) {
            // Delegate to the full constructor using -1 as the "no id yet" marker.
            this(-1, name, quantity, unit, expiryDate);
        }

        // Getter: returns the database id.
        public long getId() {
            return id;
        }

        // Getter: returns the ingredient name.
        public String getName() {
            return name;
        }

        // Getter: returns the quantity.
        public double getQuantity() {
            return quantity;
        }

        // Getter: returns the unit.
        public String getUnit() {
            return unit;
        }

        // Getter: returns the expiry date string (may be null).
        public String getExpiryDate() {
            return expiryDate;
        }

        // Returns true when the item has an expiry date that is already in the past.
        public boolean isExpired() {
            // Only items that actually have a date can be expired.
            return hasExpiryDate() && DateUtils.isToday(Long.parseLong(expiryDate)) < 0;
        }

        // Returns true when the item expires today or within the next "days" days (but is not expired yet).
        public boolean isExpiringWithin(int days) {
            // Without a date the item never expires.
            if (!hasExpiryDate()) {
                return false;
            }
            // Work out how many days remain until the expiry date.
            long remaining = DateUtils.daysUntil(expiryDate);
            // True when it is not expired (>= 0) and within the warning window.
            return remaining >= 0 && remaining <= days;
        }

        // Helper: true when the expiry date is non-null and non-blank.
        public boolean hasExpiryDate() {
            return expiryDate != null && !expiryDate.trim().isEmpty();
        }
    }
