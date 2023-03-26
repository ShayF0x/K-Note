package fr.shayfox.k_note.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Tag {
    private String mName;
    private List<UUID> mAffiliateNote;

    public Tag(String name, List<UUID> noteAffiliate) {
        mName = name;
        mAffiliateNote = noteAffiliate;
    }
    public Tag(String name) {
        mName = name;
        mAffiliateNote = new ArrayList<>();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public List<UUID> getAffiliateNote() {
        return mAffiliateNote;
    }

    public void addAffiliateNote(UUID note) {
        if(!mAffiliateNote.contains(note))
            mAffiliateNote.add(note);
    }

    public boolean removeAffiliateNote(UUID note){
        return mAffiliateNote.remove(note);
    }

    public boolean hasAffiliateNote(UUID note){
        return mAffiliateNote.contains(note);
    }

    public void setAffiliateNote(List<UUID> affiliateNote) {
        mAffiliateNote = affiliateNote;
    }
}
