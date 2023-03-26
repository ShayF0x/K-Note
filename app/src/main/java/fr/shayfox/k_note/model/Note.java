package fr.shayfox.k_note.model;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Note {
    private String mName;
    private String mDescription;
    private boolean liked;
    private Bitmap mImage;
    private List<Tag> mTags;

    private int[] mPreparationTime;
    private int[] mCookTime;
    private int[] mWaitTime;

    private int mQuantity;

    private List<Ingredient> mIngredients;
    private String mPreparationText;
    private List<String> mCookingTools;
    private String mDetails;

    private UUID mUUID;

    /**
     * @param name
     * @param description
     * @param image
     * @param tags
     * @param preparationTime
     * @param cookTime
     * @param waitTime
     * @param quantity
     * @param ingredients
     * @param preparationText
     * @param cookingTools
     * @param details
     * @param uuid
     */
    public Note(String name, String description, Bitmap image, List<Tag> tags, int[] preparationTime, int[] cookTime, int[] waitTime, int quantity, List<Ingredient> ingredients, String preparationText, List<String> cookingTools, String details, UUID uuid) {
        this.mName = name;
        this.mDescription = description;
        this.liked = false;
        this.mImage = image;
        this.mTags = tags;
        this.mPreparationTime = preparationTime;
        this.mCookTime = cookTime;
        this.mWaitTime = waitTime;
        this.mQuantity = quantity;
        this.mIngredients = ingredients;
        this.mPreparationText = preparationText;
        this.mCookingTools = cookingTools;
        this.mDetails = details;
        this.mUUID = uuid;
    }

    public Note(String name, String description, Bitmap image, List<Tag> tags, int[] preparationTime, int[] cookTime, int[] waitTime, int quantity, List<Ingredient> ingredients, String preparationText, List<String> cookingTools, String details) {
        this(name, description, image, tags, preparationTime, cookTime, waitTime, quantity, ingredients, preparationText, cookingTools, details, UUID.randomUUID());
    }

    public Note(UUID uuid){
        liked = false;
        mPreparationTime = new int[3];
        mCookTime = new int[3];
        mWaitTime = new int[3];
        mTags = new ArrayList<>();
        mIngredients = new ArrayList<>();
        mCookingTools = new ArrayList<>();
        mUUID = uuid;
    }

    public Note() {
        this(UUID.randomUUID());
    }

    public String getName() {
        return mName;
    }
    public void setName(String name) {
        mName = name;
    }

    public String getDescription() {
        return mDescription;
    }
    public void setDescription(String description) {
        mDescription = description;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public Bitmap getImage() {
        return mImage;
    }
    public void setImage(Bitmap image) {
        mImage = image;
    }

    public List<Tag> getTags() {
        return mTags;
    }
    public void setTags(List<Tag> tags) {
        mTags = tags;
    }
    public void addTag(Tag tag) {
        if(!mTags.contains(tag))
            mTags.add(tag);
    }
    public boolean removeTag(Tag tag){
        return mTags.remove(tag);
    }
    public boolean hasTag(Tag tag){
        return mTags.contains(tag);
    }

    public int[] getPreparationTime() {
        return mPreparationTime;
    }
    public void setPreparationTime(int[] preparationTime) {
        mPreparationTime = preparationTime;
    }
    public String getPreparationTimeAsString(){
        StringBuilder result = new StringBuilder();
        if(mPreparationTime[0] != 0)
            result.append(mPreparationTime[0]).append("H ");
        if(mPreparationTime[1] != 0)
            result.append(mPreparationTime[1]).append("min ");
        if(mPreparationTime[2] != 0)
            result.append(mPreparationTime[2]).append("s");
        return result.toString();
    }

    public int[] getCookTime() {
        return mCookTime;
    }
    public void setCookTime(int[] cookTime) {
        mCookTime = cookTime;
    }
    public String getCookTimeAsString(){
        StringBuilder result = new StringBuilder();
        if(mCookTime[0] != 0)
            result.append(mCookTime[0]).append("H ");
        if(mCookTime[1] != 0)
            result.append(mCookTime[1]).append("min ");
        if(mCookTime[2] != 0)
            result.append(mCookTime[2]).append("s");
        return result.toString();
    }

    public int[] getWaitTime() {
        return mWaitTime;
    }
    public void setWaitTime(int[] waitTime) {
        mWaitTime = waitTime;
    }
    public String getWaitTimeAsString(){
        StringBuilder result = new StringBuilder();
        if(mWaitTime[0] != 0)
            result.append(mWaitTime[0]).append("H ");
        if(mWaitTime[1] != 0)
            result.append(mWaitTime[1]).append("min ");
        if(mWaitTime[2] != 0)
            result.append(mWaitTime[2]).append("s");
        return result.toString();
    }

    public int[] getTotalTime() {
        int[] finalTime = new int[3];
        for(int i = 0; i<3; i++){
            finalTime[i] = mPreparationTime[i]+mCookTime[i]+mWaitTime[i];
        }
        while (finalTime[2]>=60){
            finalTime[1]+=1;
            finalTime[2]-=60;
        }
        while (finalTime[1]>=60){
            finalTime[0]+=1;
            finalTime[1]-=60;
        }
        return finalTime;
    }
    public String getTotalTimeAsString(){
        StringBuilder result = new StringBuilder();
        if(getTotalTime()[0] != 0)
            result.append(getTotalTime()[0]).append("H ");
        if(getTotalTime()[1] != 0)
            result.append(getTotalTime()[1]).append("min ");
        if(getTotalTime()[2] != 0)
            result.append(getTotalTime()[2]).append("s");
        return result.toString();
    }

    public int getQuantity() {
        return mQuantity;
    }
    public void setQuantity(int quantity) {
        mQuantity = quantity;
    }

    public List<Ingredient> getIngredients() {
        return mIngredients;
    }
    public void setIngredients(List<Ingredient> ingredients) {
        mIngredients = ingredients;
    }
    public void addIngredient(Ingredient ingredient) {
        mIngredients.add(ingredient);
    }
    public boolean removeIngredient(Ingredient ingredient){
        return mIngredients.remove(ingredient);
    }
    public boolean hasIngredient(Ingredient ingredient){
        return mIngredients.contains(ingredient);
    }

    public String getPreparationText() {
        return mPreparationText;
    }
    public void setPreparationText(String preparationText) {
        mPreparationText = preparationText;
    }

    public List<String> getCookingTools() {
        return mCookingTools;
    }
    public void setCookingTools(List<String> cookingTools) {
        mCookingTools = cookingTools;
    }
    public void addCookingTool(String cookingTool) {
        mCookingTools.add(cookingTool);
    }
    public boolean removeCookingTool(String cookingTool){
        return mCookingTools.remove(cookingTool);
    }
    public boolean hasCookingTool(String cookingTool){
        return mCookingTools.contains(cookingTool);
    }

    public String getDetails() {
        return mDetails;
    }
    public void setDetails(String details) {
        mDetails = details;
    }

    public UUID getUUID() {
        return mUUID;
    }
    public void setUUID(UUID UUID) {
        mUUID = UUID;
    }
}
