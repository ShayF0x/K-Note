package fr.shayfox.k_note.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import fr.shayfox.k_note.model.Ingredient;

public class IngredientDeserializer implements JsonDeserializer<Ingredient> {
    @Override
    public Ingredient deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Object mMeasure = null;
        String name = null;
        float quantity = 0;
        if(json.getAsJsonObject().has("mMeasure")){
            String value = json.getAsJsonObject().get("mMeasure").getAsString();
            if(asEnumValue(value)){
                mMeasure = Ingredient.Measure.valueOf(value);
            }else{
                mMeasure = value;
            }
        }
        if(json.getAsJsonObject().has("mName")){
            name = json.getAsJsonObject().get("mName").getAsString();
        }
        if(json.getAsJsonObject().has("mQuantity")){
            quantity = json.getAsJsonObject().get("mQuantity").getAsFloat();
        }
        return new Ingredient(name, quantity, mMeasure);
    }

    private boolean asEnumValue(String str){
        final Ingredient.Measure[] values = Ingredient.Measure.values();
        for (Ingredient.Measure measure:values) {
            if(measure.name().equalsIgnoreCase(str)){
                return true;
            }
        }
        return false;
    }
}
