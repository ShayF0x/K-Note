package fr.shayfox.k_note.manager;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.shayfox.k_note.R;
import fr.shayfox.k_note.controller.MainActivity;
import fr.shayfox.k_note.model.Ingredient;
import fr.shayfox.k_note.model.Note;
import fr.shayfox.k_note.model.Tag;
import fr.shayfox.k_note.serializer.BitmapDeserializer;
import fr.shayfox.k_note.utils.FileUtils;

public class AppManager {
    private List<Note> mNoteList = new ArrayList();
    private List<Tag> mTagsManager = new ArrayList<>();

    public AppManager(Context context) {
        createAllFiles();

        try {
            registerTagFile();
            registerAllNotes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void createAllFiles() {
        File tagFile = new File(MainActivity.INSTANCE.getExternalFilesDir(null).toString()+File.separator+"Config.json");
            try {
                tagFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    private void registerAllNotes() throws Exception {
        //Checking the availability state of the External Storage.
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            //If it isn't mounted - we can't write into it.
            return;
        }
        final String path = MainActivity.INSTANCE.getExternalFilesDir(null).toString()+File.separator+"Notes";
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files != null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Note.class, new BitmapDeserializer(this))
                    .create();
            for (int i = 0; i < files.length; i++) {

//                Log.d("Files", "FileName:" + files[i].getName());
//                Log.d("Files", "FileContent: "+ getStringFromFile(files[i].getAbsolutePath()));
                Note note = gson.fromJson(getStringFromFile(files[i].getAbsolutePath()), Note.class);
                addNote(note);
            }
        }
    }

    public void updateTagFile() throws Exception {
        final String path = MainActivity.INSTANCE.getExternalFilesDir(null).toString()+File.separator+"Config.json";
        final Gson gson = new Gson();

        String jsonString = getStringFromFile(path);
        JsonObject root;


        if(jsonString == null || jsonString.isEmpty() || !FileUtils.isJSONValid(jsonString)){
            root = new JsonObject();
        }else{
            root = gson.fromJson(jsonString, JsonElement.class).getAsJsonObject();
        }


        JsonArray jsonArray = new JsonArray();

        for (Tag tag:getTagsManager()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("Name", tag.getName());
            jsonObject.add("AffiliatesNotes", gson.toJsonTree(tag.getAffiliateNote().stream().map(uuid -> uuid.toString()).collect(Collectors.toList())));

            jsonArray.add(jsonObject);
        }
        if(root.has("Tags")){
            root.remove("Tags");
        }

        root.add("Tags", jsonArray);
        FileUtils.writeFileExternalStorage("Config.json", gson.toJson(root, JsonElement.class), true);
    }

    public void registerTagFile() throws Exception {
        final String path = MainActivity.INSTANCE.getExternalFilesDir(null).toString()+File.separator+"Config.json";
        final Gson gson = new Gson();

        String jsonString = getStringFromFile(path);
        if(jsonString == null || jsonString.isEmpty() || !FileUtils.isJSONValid(jsonString))return;
        JsonObject root = gson.fromJson(jsonString, JsonElement.class).getAsJsonObject();
        if(root.has("Tags")){
            JsonArray array = root.getAsJsonArray("Tags");
            for (JsonElement jsonElement:array) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                Tag tag = new Tag(jsonObject.get("Name").getAsString());
                JsonArray list = (JsonArray) jsonObject.get("AffiliatesNotes");
                for (JsonElement element:list) {
                    tag.addAffiliateNote(UUID.fromString(element.getAsString()));
                }
                getTagsManager().add(tag);
            }
        }
    }

    private String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public List<Note> getNoteList() {
        return mNoteList;
    }

    public void setNoteList(List<Note> noteList) {
        mNoteList = noteList;
    }
    public void addNote(Note note) {
        note.getTags().forEach(tag -> tag.addAffiliateNote(note.getUUID()));
        mNoteList.add(note);
    }
    public boolean removeNote(Note note){
        note.getTags().forEach(tag -> tag.removeAffiliateNote(note.getUUID()));
        return mNoteList.remove(note);
    }
    public Note getNoteByUUID(UUID uuid){
        for (Note note:mNoteList) {
            if(note.getUUID().toString().equalsIgnoreCase(uuid.toString()))
                return note;
        }
        return null;
    }
    public boolean hasNote(Note note){
        return mNoteList.contains(note);
    }
    public boolean hasNote(UUID uuid){
        for (Note note:mNoteList) {
            if(note.getUUID() == uuid)
                return true;
        }
        return false;
    }

    public List<Tag> getTagsManager() {
        return mTagsManager;
    }

    public void setTagsManager(List<Tag> tagsManager) {
        mTagsManager = tagsManager;
    }

    public Optional<Tag> getTag(String name){
        return mTagsManager.stream().filter(tag -> tag.getName().equalsIgnoreCase(name)).findFirst();
    }
    public void addTag(Tag tag) {
        tag.getAffiliateNote().forEach(uuid -> getNoteByUUID(uuid).addTag(tag));
        mTagsManager.add(tag);
    }
    public boolean removeTag(Tag tag){
        tag.getAffiliateNote().forEach(uuid -> getNoteByUUID(uuid).removeTag(tag));
        return mTagsManager.remove(tag);
    }
    public boolean hasTag(String tag){
        for (Tag tag1:mTagsManager) {
            if (tag1.getName().equalsIgnoreCase(tag)) return true;
        }

        return false;
    }

}
