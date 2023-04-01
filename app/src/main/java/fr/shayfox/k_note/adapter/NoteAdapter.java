package fr.shayfox.k_note.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.shayfox.k_note.R;
import fr.shayfox.k_note.controller.ConsultNoteActivity;
import fr.shayfox.k_note.controller.MainActivity;
import fr.shayfox.k_note.model.Note;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder>{

    private List<Note> listNote;
    private final MainActivity mMainActivity;

    //0: List, 1: Blocs
    public int itemMode = 0;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ConstraintLayout mRoot;
        private final ShapeableImageView mImageView;
        private final TextView mTextView;
        private final ImageView mIcon;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            mRoot = view.findViewById(R.id.item_root);
            mImageView = (ShapeableImageView) view.findViewById(R.id.image_item);
            mTextView = (TextView) view.findViewById(R.id.name_item);
            mIcon = (ImageView) view.findViewById(R.id.like_icon);
        }

        public ConstraintLayout getRoot() {
            return mRoot;
        }
        public ShapeableImageView getImageView() {
            return mImageView;
        }
        public TextView getTextView() {
            return mTextView;
        }
        public ImageView getIcon() {
            return mIcon;
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param listNote      String[] containing the data to populate views to be used
     *                      by RecyclerView
     * @param mainActivity
     */
    public NoteAdapter(List<Note> listNote, MainActivity mainActivity) {
        this.listNote = listNote;
        mMainActivity = mainActivity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate((itemMode == 0 ? R.layout.item_list_note:R.layout.item_blocs_note), viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        final Note note = listNote.get(position);

        viewHolder.getRoot().setOnClickListener(view -> {
            Intent intent = new Intent(mMainActivity, ConsultNoteActivity.class);
            intent.putExtra("NoteUUID", note.getUUID());
            mMainActivity.startActivity(intent);
        });

        if(itemMode == 0 && note.getName() != null && !note.getName().isEmpty()) {
            viewHolder.getTextView().setText(note.getName());
        }
        viewHolder.getIcon().setImageResource(note.isLiked() ? R.drawable.ic_like:R.drawable.ic_unlike);
        if(note.getImage() != null) {
            viewHolder.getImageView().setImageBitmap(note.getImage());
        }

        viewHolder.getIcon().setOnClickListener(view -> {
            note.setLiked(!note.isLiked());
            viewHolder.getIcon().setImageResource(note.isLiked() ? R.drawable.ic_like:R.drawable.ic_unlike);
            getListNote().sort((note1, note2) -> {
                if (note1.isLiked() == note2.isLiked()) return 0;
                else if (note1.isLiked() && !note2.isLiked()) return -1;
                return 1;
            });
            notifyDataSetChanged();
        });

//        viewHolder.getImageView().setOnLongClickListener(view -> {
//            if(itemMode != 1)return false;
//
//            final int position1 = viewHolder.getAdapterPosition();
//            final Note note12 = mMainActivity.mAppManager.getNoteList().get(position1);
//            AtomicBoolean restore = new AtomicBoolean(false);
//
//            mMainActivity.mAppManager.removeNote(note12);
//            notifyItemRemoved(position1);
//            mMainActivity.updateListTagNote(mMainActivity.binding.autoCompleteTextView.getText().toString());
//
//            Snackbar snackbar = Snackbar
//                    .make(mMainActivity.binding.coordinatorLayout, "La recette "+ note12.getName()+" va être supprimée definitivement !", Snackbar.LENGTH_LONG);
//            snackbar.setAction("Annuler", v -> {
//                restore.set(true);
//                mMainActivity.mAppManager.addNote(note12);
//                notifyItemInserted(position1);
//                mMainActivity.updateListTagNote(mMainActivity.binding.autoCompleteTextView.getText().toString());
//            });
//
//            snackbar.setActionTextColor(Color.YELLOW);
//            snackbar.show();
//
//            return false;
//        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return listNote.size();
    }

    public List<Note> getListNote() {
        return listNote;
    }

    public void setListNote(List<Note> listNote) {
        this.listNote = listNote;
    }
}
