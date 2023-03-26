package fr.shayfox.k_note.serializer;

import android.graphics.Bitmap;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;

import fr.shayfox.k_note.model.Ingredient;
import fr.shayfox.k_note.model.Note;

public class BitmapSerializer implements JsonSerializer<Note> {
    @Override
    public JsonElement serialize(Note note, Type typeOfSrc, JsonSerializationContext context) {
        Gson gson = new Gson();
        JsonObject object = new JsonObject();


        if(note.getName() != null)object.addProperty("Name",  note.getName());
        object.addProperty("UUID",  note.getUUID().toString());

        if(note.getDescription() != null)object.addProperty("Description",  note.getDescription());

        if(note.isLiked())object.addProperty("isLiked",  true);

        if(note.getTags() != null && !note.getTags().isEmpty()) {
            JsonArray array = new JsonArray();
            note.getTags().forEach(tag -> array.add(tag.getName()));
            object.add("Tags", array);
        }

        if(note.getPreparationTime() != null && !isIntArrayFillOnly(0, note.getPreparationTime()))object.add("PreparationTime",  gson.toJsonTree(note.getPreparationTime(), int[].class));

        if(note.getCookTime() != null && !isIntArrayFillOnly(0, note.getCookTime()))object.add("CookTime",  gson.toJsonTree(note.getCookTime(), int[].class));

        if(note.getWaitTime() != null && !isIntArrayFillOnly(0, note.getWaitTime()))object.add("WaitTime",  gson.toJsonTree(note.getWaitTime(), int[].class));

        if(note.getQuantity() != 0)object.addProperty("Quantity",  note.getQuantity());

        if(note.getIngredients() != null && !note.getIngredients().isEmpty()) {
            JsonArray array = new JsonArray();
            note.getIngredients().forEach(ingredient -> {
                array.add(gson.toJsonTree(ingredient, Ingredient.class));
            });
            object.add("Ingredients", array);
        }

        if(note.getPreparationText() != null)object.addProperty("PreparationText",  note.getPreparationText());

        if(note.getCookingTools() != null && !note.getCookingTools().isEmpty()) {
            JsonArray array = new JsonArray();
            note.getCookingTools().forEach(array::add);
            object.add("CookingTools", array);
        }

        if(note.getDetails() != null)object.addProperty("Details",  note.getDetails());

        if(note.getImage() != null)object.addProperty("Image",  getStringFromBitmap(note.getImage()));

        return object;
    }

    public boolean isIntArrayFillOnly(int value, int[] array){
        for (int i:array) {
            if(i!=value)
                return false;
        }
        return true;
    }



    private String getStringFromBitmap(Bitmap bitmapPicture) {
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }
}
