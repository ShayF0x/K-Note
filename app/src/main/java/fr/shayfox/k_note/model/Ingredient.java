package fr.shayfox.k_note.model;

public class Ingredient {
    private String mName;
    private float mQuantity;
    //Measure or String if that Custom
    private Object mMeasure;

    public Ingredient(String name, float quantity, Object measure) {
        mName = name;
        mQuantity = quantity;
        mMeasure = measure;
    }
    public Ingredient() {}

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public float getQuantity() {
        return mQuantity;
    }

    public void setQuantity(float quantity) {
        mQuantity = quantity;
    }

    public Object getMeasure() {
        return mMeasure;
    }

    public void setMeasure(Object measure) {
        mMeasure = measure;
    }

    public enum Measure {

        GRAMME("gramme", "g"),
        KILOGRAMME("Kilogramme", "kg"),
        MILLIGRAMME("Milligramme", "mg"),
        LITRE("Litre", "L"),
        MILLILITRE("Millilitre", "ml"),
        DECILITRE("Décilitre", "dl"),
        CENTILITRE("Centilitre", "cl"),
        TEASPOON("Cuillère à café", "c.c."),
        TABLESPOON("Cuillère à soupe", "c.s.");

        Measure(String name, String prefix) {
            this.name = name;
            this.prefix = prefix;
        }

        private final String name;
        private final String prefix;

        public String getName() {
            return name;
        }

        public String getPrefix() {
            return prefix;
        }
    }
}
