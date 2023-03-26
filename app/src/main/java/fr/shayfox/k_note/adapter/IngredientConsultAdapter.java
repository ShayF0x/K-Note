package fr.shayfox.k_note.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import fr.shayfox.k_note.R;
import fr.shayfox.k_note.model.Ingredient;
import fr.shayfox.k_note.model.Note;

public class IngredientConsultAdapter extends ArrayAdapter<Ingredient> {
    private final Context _context;
    private List<Ingredient> _ingredients;
    private final Note _note;
    private int _quantity;

    public IngredientConsultAdapter(Context context, Note note, int quantity) {
        super(context, R.layout.item_ingredients_consult, note.getIngredients());
        _context = context;
        _ingredients = note.getIngredients();
        _note = note;
        _quantity = quantity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_ingredients_consult, parent, false);
        } else {
            convertView = (LinearLayout) convertView;
        }

        Ingredient ingredient = _ingredients.get(position);

        TextView viewTitle = (TextView) convertView.findViewById(R.id.ingredients_consult_item_name);
        viewTitle.setText(ingredient.getName());

        TextView viewMeasure = (TextView) convertView.findViewById(R.id.ingredients_consult_item_measure);
        StringBuilder measureBuilder = new StringBuilder();
        if(ingredient.getQuantity() != 0) {
            String result = "1";
            float quantity = ingredient.getQuantity();
            if(ingredient.getQuantity() > 0) {
                quantity *= (float) (_quantity)/(float) (_note.getQuantity());
                result = String.valueOf(quantity);
                if ((quantity - (int)quantity) == 0) result = String.valueOf((int)quantity);
            }

            measureBuilder.append(result);
            measureBuilder.append(" ");
        }
        if(ingredient.getMeasure() != null) {
            measureBuilder.append((ingredient.getMeasure() instanceof Ingredient.Measure ?
                    ((Ingredient.Measure) ingredient.getMeasure()).getPrefix():(String)ingredient.getMeasure()));
        }

        viewMeasure.setText(measureBuilder.toString());

        return convertView;
    }
}
