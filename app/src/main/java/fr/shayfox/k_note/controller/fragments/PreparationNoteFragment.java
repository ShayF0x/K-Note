package fr.shayfox.k_note.controller.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fr.shayfox.k_note.R;
import fr.shayfox.k_note.model.Note;

public class PreparationNoteFragment extends Fragment {

    private TextView mTextView;
    private final Note mNote;

    public PreparationNoteFragment(Note note) {
        super(R.layout.fragment_preparation_note);
        mNote = note;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextView = view.findViewById(R.id.preparation_note_fragment_preparation_textview);

        mTextView.setText(mNote.getPreparationText());
    }
}