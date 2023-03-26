package fr.shayfox.k_note.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.shayfox.k_note.R;
import fr.shayfox.k_note.model.Note;

public class ToolsCreateAdapter extends RecyclerView.Adapter<ToolsCreateAdapter.ViewHolder>{

    private List<String> listTools;
    private final Note mNote;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTextView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            mTextView = view.findViewById(R.id.item_tools_edittext_ustensil);
        }

        public TextView getTextView() {
            return mTextView;
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param listNote      String[] containing the data to populate views to be used
     *                      by RecyclerView
     */
    public ToolsCreateAdapter(List<String> listNote, Note mNote) {
        this.listTools = listNote;
        this.mNote = mNote;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ToolsCreateAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_tools_create_note, viewGroup, false);

        return new ToolsCreateAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ToolsCreateAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.getTextView().setText(listTools.get(position));
        viewHolder.getTextView().setOnFocusChangeListener((view, b) -> {
            if(!b){
                if(viewHolder.getTextView().getText().toString() != null && !viewHolder.getTextView().getText().toString().isEmpty()) {
                    mNote.getCookingTools().set(position, viewHolder.getTextView().getText().toString());
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return listTools.size();
    }

    public List<String> getListTools() {
        return listTools;
    }

    public void setListTools(List<String> listTools) {
        this.listTools = listTools;
    }
}
