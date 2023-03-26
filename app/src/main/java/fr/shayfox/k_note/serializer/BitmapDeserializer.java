package fr.shayfox.k_note.serializer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.UUID;

import fr.shayfox.k_note.manager.AppManager;
import fr.shayfox.k_note.model.Ingredient;
import fr.shayfox.k_note.model.Note;

public class BitmapDeserializer implements JsonDeserializer<Note> {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Ingredient.class, new IngredientDeserializer()).create();
    public final AppManager mAppManager;

    public BitmapDeserializer(AppManager appManager) {
        this.mAppManager = appManager;
    }

    @Override
    public Note deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jObject = json.getAsJsonObject();

        Note note = new Note(UUID.fromString(jObject.get("UUID").getAsString()));

        if(jObject.has("Name") && jObject.get("Name").getAsString() != null && !jObject.get("Name").getAsString().isEmpty()){
            note.setName(jObject.get("Name").getAsString());
        }
        if(jObject.has("Description") && jObject.get("Description").getAsString() != null && !jObject.get("Description").getAsString().isEmpty()){
            note.setDescription(jObject.get("Description").getAsString());
        }
        if(jObject.has("PreparationText") && jObject.get("PreparationText").getAsString() != null && !jObject.get("PreparationText").getAsString().isEmpty()){
            note.setPreparationText(jObject.get("PreparationText").getAsString());
        }
        if(jObject.has("Details") && jObject.get("Details").getAsString() != null && !jObject.get("Details").getAsString().isEmpty()){
            note.setDetails(jObject.get("Details").getAsString());
        }
        if(jObject.has("isLiked") && jObject.get("isLiked").getAsString() != null){
            note.setLiked(jObject.get("isLiked").getAsBoolean());
        }
        if(jObject.has("Quantity")){
            note.setQuantity(jObject.get("Quantity").getAsInt());
        }
        if(jObject.has("PreparationTime")){
            note.setPreparationTime(gson.fromJson(jObject.get("PreparationTime"), int[].class));
        }
        if(jObject.has("CookTime")){
            note.setCookTime(gson.fromJson(jObject.get("CookTime"), int[].class));
        }
        if(jObject.has("WaitTime")){
            note.setWaitTime(gson.fromJson(jObject.get("WaitTime"), int[].class));
        }
        if(jObject.has("CookingTools")){
            JsonArray element = jObject.getAsJsonArray("CookingTools");
            for (JsonElement array:element) {
                note.addCookingTool(array.getAsString());
            }
        }
        if(jObject.has("Tags")){
            JsonArray element = jObject.getAsJsonArray("Tags");
            for (JsonElement array:element) {
                String arrayAsString = array.getAsString();
                if(mAppManager.hasTag(arrayAsString)){
                    note.addTag(mAppManager.getTag(arrayAsString).get());
                }
            }
        }
        if(jObject.has("Ingredients")){
            JsonArray element = jObject.getAsJsonArray("Ingredients");
            for (JsonElement array:element) {
                Ingredient ingredient = gson.fromJson(array, Ingredient.class);
                note.addIngredient(ingredient);
            }
        }
        if(jObject.has("Image")){
            note.setImage(getBitmapFromString(jObject.get("Image").getAsString()));
        }

        return note;
    }

    private Bitmap getBitmapFromString(String stringPicture) {
        byte[] decodedString = Base64.decode(stringPicture, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
