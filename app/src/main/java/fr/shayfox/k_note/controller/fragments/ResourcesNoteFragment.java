package fr.shayfox.k_note.controller.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fr.shayfox.k_note.R;
import fr.shayfox.k_note.adapter.IngredientConsultAdapter;
import fr.shayfox.k_note.model.Note;

public class ResourcesNoteFragment extends Fragment {

    private TextView mQuantityTextView;

    private ListView mIngredientsListView;
    private ListView mToolsListView;

    private ImageView mAddPeopleImageView;
    private ImageView mRemovePeopleImageView;

    private QuantityChangeEvent mQuantityChangeEvent;

    private final Note mNote;
    private int mQuantity;
    public ResourcesNoteFragment(Note note) {
        this(note, null);
    }
    public ResourcesNoteFragment(Note note, QuantityChangeEvent quantityChangeEvent) {
        super(R.layout.fragment_resources_note);
        mNote = note;
        mQuantity = note.getQuantity();
        mQuantityChangeEvent = quantityChangeEvent;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mQuantityTextView = view.findViewById(R.id.resources_note_fragment_peoples_textview);

        mAddPeopleImageView = view.findViewById(R.id.resources_note_fragment_add_imageview);
        mRemovePeopleImageView = view.findViewById(R.id.resources_note_fragment_remove_imageview);

        mIngredientsListView = view.findViewById(R.id.resources_note_fragment_ingredients_recycler);
        mToolsListView = view.findViewById(R.id.resources_note_fragment_tools_recycler);

        mQuantityTextView.setText(String.format("%s (%s)", mQuantity, mNote.getQuantity()));

        mAddPeopleImageView.setOnClickListener(view1 -> {
            setQuantity(mQuantity+1);
            mQuantityTextView.setText(String.format("%s (%s)", mQuantity, mNote.getQuantity()));
            if(mQuantityChangeEvent != null){
                mQuantityChangeEvent.onQuantityChange(mQuantity);
            }
        });
        mRemovePeopleImageView.setOnClickListener(view1 -> {
            if(mQuantity <=0)return;
            setQuantity(mQuantity-1);
            if(mQuantityChangeEvent != null){
                mQuantityChangeEvent.onQuantityChange(mQuantity);
            }
        });

        mToolsListView.setAdapter(new ArrayAdapter<>(getContext(), R.layout.item_tools_consult, mNote.getCookingTools()));
        mIngredientsListView.setAdapter(new IngredientConsultAdapter(getContext(), mNote, mQuantity));

    }

    public void setQuantity(int quantity){
        mQuantity = quantity;
        if(mQuantityTextView != null) {
            mQuantityTextView.setText(String.format("%s (%s)", mQuantity, mNote.getQuantity()));
        }
        if(mIngredientsListView != null){
            mIngredientsListView.setAdapter(new IngredientConsultAdapter(getContext(), mNote, mQuantity));
        }
    }

    public interface QuantityChangeEvent{
        void onQuantityChange(int value);
    }
}