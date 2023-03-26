package fr.shayfox.k_note.controller.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fr.shayfox.k_note.R;
import fr.shayfox.k_note.model.Note;


public class InfoNoteFragment extends Fragment {

    private TextView mDescriptionTextView;
    private TextView mQuantityTextView;
    private TextView mPreparationTimeTextView;
    private TextView mCookTimeTextView;
    private TextView mWaitTimeTextView;
    private TextView mTotalTimeTextView;
    private TextView mDetailsTextView;

    private ImageView mAddPeopleImageView;
    private ImageView mRemovePeopleImageView;

    private QuantityChangeEvent mQuantityChangeEvent;

    private final Note mNote;
    private int mQuantity;

    public InfoNoteFragment(Note note) {
        this(note, null);
    }

    public InfoNoteFragment(Note note, QuantityChangeEvent quantityChangeEvent) {
        super(R.layout.fragment_info_note);
        this.mNote = note;
        mQuantity = mNote.getQuantity();
        this.mQuantityChangeEvent = quantityChangeEvent;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDescriptionTextView = view.findViewById(R.id.info_note_fragment_description_textview);
        mQuantityTextView = view.findViewById(R.id.info_note_fragment_peoples_textview);
        mPreparationTimeTextView = view.findViewById(R.id.info_note_fragment_preparationtime_textview);
        mCookTimeTextView = view.findViewById(R.id.info_note_fragment_cooktime_textview);
        mWaitTimeTextView = view.findViewById(R.id.info_note_fragment_waittime_textview);
        mTotalTimeTextView = view.findViewById(R.id.info_note_fragment_totaltime_textview);
        mDetailsTextView = view.findViewById(R.id.info_note_fragment_details_textview);

        mAddPeopleImageView = view.findViewById(R.id.info_note_fragment_add_imageview);
        mRemovePeopleImageView = view.findViewById(R.id.info_note_fragment_remove_imageview);

        mDescriptionTextView.setText(mNote.getDescription());
        mQuantityTextView.setText(String.format("%s (%s)", mQuantity, mNote.getQuantity()));
        mPreparationTimeTextView.setText(mNote.getPreparationTimeAsString());
        mCookTimeTextView.setText(mNote.getCookTimeAsString());
        mWaitTimeTextView.setText(mNote.getWaitTimeAsString());
        mTotalTimeTextView.setText(mNote.getTotalTimeAsString());
        mDetailsTextView.setText(mNote.getDetails());

        mAddPeopleImageView.setOnClickListener(view1 -> {
            mQuantity ++;
            mQuantityTextView.setText(String.format("%s (%s)", mQuantity, mNote.getQuantity()));
            if(mQuantityChangeEvent != null){
                mQuantityChangeEvent.onQuantityChange(mQuantity);
            }
        });
        mRemovePeopleImageView.setOnClickListener(view1 -> {
            if(mQuantity <=0)return;
            mQuantity --;
            mQuantityTextView.setText(String.format("%s (%s)", mQuantity, mNote.getQuantity()));
            if(mQuantityChangeEvent != null){
                mQuantityChangeEvent.onQuantityChange(mQuantity);
            }
        });

    }

    public void setQuantity(int quantity){
        mQuantity = quantity;
        if(mQuantityTextView != null) {
            mQuantityTextView.setText(String.format("%s (%s)", mQuantity, mNote.getQuantity()));
        }
    }

    public interface QuantityChangeEvent{
        void onQuantityChange(int value);
    }
}