package fr.shayfox.k_note.adapter;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.shayfox.k_note.R;
import fr.shayfox.k_note.controller.MainActivity;
import fr.shayfox.k_note.model.Ingredient;

public class IngredientsCreateAdapter extends RecyclerView.Adapter<IngredientsCreateAdapter.ViewHolder>{

    private List<Ingredient> listIngredients;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final EditText mNameEditField;
        private final EditText mValueEditField;
        private final TextInputLayout mChoiceBoxMeasureLayout;
        private final AutoCompleteTextView mChoiceMeasureTextView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            mNameEditField = view.findViewById(R.id.item_ingredient_textfield_name);
            mValueEditField = view.findViewById(R.id.item_ingredient_edittext_value);
            mChoiceBoxMeasureLayout = view.findViewById(R.id.item_ingredient_choicebox_measure);
            mChoiceMeasureTextView = view.findViewById(R.id.item_ingredient_autocomplete_measure);
        }

        public EditText getNameEditField() {
            return mNameEditField;
        }
        public EditText getValueEditField() {
            return mValueEditField;
        }
        public TextInputLayout getChoiceBoxMeasureLayout() {
            return mChoiceBoxMeasureLayout;
        }
        public AutoCompleteTextView getChoiceMeasureTextView() {
            return mChoiceMeasureTextView;
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param listNote      String[] containing the data to populate views to be used
     *                      by RecyclerView
     */
    public IngredientsCreateAdapter(List<Ingredient> listNote) {
        this.listIngredients = listNote;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public IngredientsCreateAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_ingredient_create_note, viewGroup, false);

        return new IngredientsCreateAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(IngredientsCreateAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        final Ingredient ingredient = listIngredients.get(position);

        if(ingredient.getName() != null && !ingredient.getName().isEmpty()) {
            viewHolder.getNameEditField().setText(ingredient.getName());
        }
        if(ingredient.getQuantity() != 0) {
            String result = "1";
            float quantity = ingredient.getQuantity();
            if(ingredient.getQuantity() > 0) {
                result = String.valueOf(quantity);
                if ((quantity - (int) quantity) == 0) result = String.valueOf((int)quantity);
            }

            viewHolder.getValueEditField().setText(result);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(MainActivity.INSTANCE.getApplicationContext(), R.layout.tag_dropdown_menu, Arrays.stream(Ingredient.Measure.values()).map(value -> value.getPrefix()).collect(Collectors.toList()));
        adapter.setDropDownViewResource(R.layout.tag_dropdown_menu);
        viewHolder.getChoiceMeasureTextView().setAdapter(adapter);
        viewHolder.getChoiceMeasureTextView().setDropDownBackgroundDrawable(
                ResourcesCompat.getDrawable(MainActivity.INSTANCE.getResources(), R.drawable.dropdown_menu_tag, null));

        viewHolder.getChoiceMeasureTextView().setOnItemClickListener((adapterView, view, position1, l) ->
                ingredient.setMeasure(Ingredient.Measure.values()[position1]));

        if(ingredient.getMeasure() != null) {
            viewHolder.getChoiceMeasureTextView().setText((ingredient.getMeasure() instanceof Ingredient.Measure ?
                    ((Ingredient.Measure) ingredient.getMeasure()).getPrefix():(String)ingredient.getMeasure()), false);
        }

        viewHolder.getNameEditField().setOnFocusChangeListener((view, b) -> {
            if(!b){
                if(viewHolder.getNameEditField().getText().toString() != null && !viewHolder.getNameEditField().getText().toString().isEmpty()) {
                    ingredient.setName(viewHolder.getNameEditField().getText().toString());
                }
            }
        });
        viewHolder.getValueEditField().setOnFocusChangeListener((view, b) -> {
            if(!b){
                if(viewHolder.getValueEditField().getText().toString() != null && !viewHolder.getValueEditField().getText().toString().isEmpty()) {
                    ingredient.setQuantity(Float.parseFloat(viewHolder.getValueEditField().getText().toString()));
                }
            }
        });

        if((ingredient.getQuantity() < 0 || ingredient.getMeasure() == null) && viewHolder.getChoiceBoxMeasureLayout() != null){
            ((LinearLayout)viewHolder.getChoiceBoxMeasureLayout().getParent()).removeView(viewHolder.getChoiceBoxMeasureLayout());
            Resources r = MainActivity.INSTANCE.getResources();
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            params.setMargins(px, 0, px, 0);
            viewHolder.getValueEditField().setLayoutParams(params);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return listIngredients.size();
    }

    public List<Ingredient> getListIngredients() {
        return listIngredients;
    }

    public void setListIngredients(List<Ingredient> listIngredients) {
        this.listIngredients = listIngredients;
    }
}
