package fr.shayfox.k_note.controller;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import fr.shayfox.k_note.R;
import fr.shayfox.k_note.adapter.IngredientsCreateAdapter;
import fr.shayfox.k_note.adapter.ToolsCreateAdapter;
import fr.shayfox.k_note.dialog.TimePickerWithSeconds;
import fr.shayfox.k_note.manager.AppManager;
import fr.shayfox.k_note.manager.SwipeToDeleteCallback;
import fr.shayfox.k_note.model.Ingredient;
import fr.shayfox.k_note.model.Note;
import fr.shayfox.k_note.model.Tag;
import fr.shayfox.k_note.serializer.BitmapSerializer;
import fr.shayfox.k_note.utils.FileUtils;

public class CreateNoteActivity extends AppCompatActivity {

    private MaterialToolbar mToolbar;
    private AppManager mAppManager;
    private Note mNote;

    private CoordinatorLayout mRootLayout;

    private IngredientsCreateAdapter mIngredientsAdapter;
    private ToolsCreateAdapter mToolsAdapter;

    private FloatingActionButton mPictureAddButton;

    private ImageView mPictureImageView;

    private ImageView mAddIngredientImageView;
    private ImageView mAddToolsImageView;

    private EditText mDescriptionEditText;
    private EditText mPreparationEditText;
    private EditText mPrecisionEditText;
    private EditText mQuantityEditText;
    private EditText mNameEditText;

    private TextView mPreparationTimeTextView;
    private TextView mCookTimeTextView;
    private TextView mWaitTimeTextView;
    private TextView mFinalTimeTextView;

    private ChipGroup mTagsChipGroup;

    private RecyclerView mIngredientsRecycler;
    private RecyclerView mToolsRecycler;

    private LinearLayout mPreparationTimeLayout;
    private LinearLayout mCookTimeLayout;
    private LinearLayout mWaitTimeLayout;
    private ConstraintLayout mTagLayout;

    private View mBorderDescriptionView;
    private View mBorderQuantityView;
    private View mBorderPreparationView;
    private View mBorderPrecisionsView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        MainActivity mainActivity = MainActivity.INSTANCE;
        mAppManager = mainActivity.mAppManager;

        final boolean isEdit = (boolean) getIntent().getSerializableExtra("isEdit");
        UUID currentUUID = (UUID) getIntent().getSerializableExtra("NoteUUID");
        mNote = mAppManager.getNoteByUUID(currentUUID);

        /*
        Register
         */
        mRootLayout = findViewById(R.id.create_note_activity_coordinator_layout);

        mNameEditText = findViewById(R.id.create_note_activity_edittext_name);

        mPictureAddButton = findViewById(R.id.create_note_activity_edit_image_button);
        mPictureImageView = findViewById(R.id.create_note_activity_picture);

        mAddIngredientImageView = findViewById(R.id.create_note_activity_button_add_ingredient);
        mAddToolsImageView = findViewById(R.id.create_note_activity_button_add_tools);

        mDescriptionEditText = findViewById(R.id.create_note_activity_edittext_description);
        mPreparationEditText = findViewById(R.id.create_note_activity_edittext_preparation);
        mPrecisionEditText = findViewById(R.id.create_note_activity_edittext_precisions);
        mQuantityEditText = findViewById(R.id.create_note_activity_edittext_quantity);

        mPreparationTimeTextView = findViewById(R.id.create_note_activity_text_preparation_time);
        mCookTimeTextView = findViewById(R.id.create_note_activity_text_cook_time);
        mWaitTimeTextView = findViewById(R.id.create_note_activity_text_wait_time);
        mFinalTimeTextView = findViewById(R.id.create_note_activity_textfield_final_time);

        mTagsChipGroup = findViewById(R.id.create_note_activity_chipgroup_tags);

        mIngredientsRecycler = findViewById(R.id.create_note_activity_recycler_ingredients);
        mToolsRecycler = findViewById(R.id.create_note_activity_recycler_tools);

        mPreparationTimeLayout = findViewById(R.id.create_note_activity_layout_preparation_time);
        mCookTimeLayout = findViewById(R.id.create_note_activity_layout_cook_time);
        mWaitTimeLayout = findViewById(R.id.create_note_activity_layout_wait_time);
        mTagLayout = findViewById(R.id.create_note_activity_tag_layout);

        mBorderDescriptionView = findViewById(R.id.create_note_border_description_activity);
        mBorderQuantityView = findViewById(R.id.create_note_border_quantity_activity);
        mBorderPreparationView = findViewById(R.id.create_note_border_preparation_activity);
        mBorderPrecisionsView = findViewById(R.id.create_note_border_precisions_activity);

        mToolbar = findViewById(R.id.create_activity_toolbar);

        mIngredientsAdapter = new IngredientsCreateAdapter(mNote.getIngredients());
        mToolsAdapter = new ToolsCreateAdapter(mNote.getCookingTools(), mNote);

        /*
        SETUP
         */

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setNavigationOnClickListener(view -> {
            if(!isEdit) {
                mAppManager.removeNote(mNote);
                MainActivity.INSTANCE.mNoteAdapter.notifyDataSetChanged();
            }
            finish();
        });

        if(mNote.getName() != null && !mNote.getName().isEmpty()) {
            mNameEditText.setText(mNote.getName());
        }

        if(mNote.getImage() != null){
            mPictureImageView.setImageBitmap(mNote.getImage());
            mPictureImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        if(mNote.getDescription() != null && !mNote.getDescription().isEmpty()) {
            mDescriptionEditText.setText(mNote.getDescription());
        }
        if(mNote.getPreparationText() != null && !mNote.getPreparationText().isEmpty()) {
            mPreparationEditText.setText(mNote.getPreparationText());
        }
        if(mNote.getDetails() != null && !mNote.getDetails().isEmpty()) {
            mPrecisionEditText.setText(mNote.getDetails());
        }

        mQuantityEditText.setText(String.valueOf(mNote.getQuantity()));

        if(mNote.getPreparationTime() != null && !mNote.getPreparationTimeAsString().isEmpty()) {
            mPreparationTimeTextView.setText(mNote.getPreparationTimeAsString());
        }
        if(mNote.getCookTime() != null && !mNote.getCookTimeAsString().isEmpty()) {
            mCookTimeTextView.setText(mNote.getCookTimeAsString());
        }
        if(mNote.getWaitTime() != null && !mNote.getWaitTimeAsString().isEmpty()) {
            mWaitTimeTextView.setText(mNote.getWaitTimeAsString());
        }
        if(mNote.getTotalTime() != null && !mNote.getTotalTimeAsString().isEmpty()) {
            mFinalTimeTextView.setText(mNote.getTotalTimeAsString());
        }

        if(mNote.getTags() != null && mNote.getTags().size() > 0){
            for (Tag tag:mNote.getTags()) {
                addTagChipToChipGroup(tag);
            }
        }

        mNameEditText.setOnFocusChangeListener((view, b) -> {
            if(!b){
                if(mNameEditText.getText().toString() != null && !mNameEditText.getText().toString().isEmpty()) {
                    mNote.setName(mNameEditText.getText().toString());
                }
            }
        });
        mDescriptionEditText.setOnFocusChangeListener((view, b) -> {
            if(b){
                mBorderDescriptionView.setBackgroundResource(R.color.primary);
            }else{
                mBorderDescriptionView.setBackgroundResource(R.color.secondary_font);
                if(mDescriptionEditText.getText().toString() != null) {
                    mNote.setDescription(mDescriptionEditText.getText().toString());
                }
            }
        });
        mQuantityEditText.setOnFocusChangeListener((view, b) -> {
            if(b){
                mBorderQuantityView.setBackgroundResource(R.color.primary);
            }else{
                mBorderQuantityView.setBackgroundResource(R.color.secondary_font);
                if(mQuantityEditText.getText().toString() != null && !mQuantityEditText.getText().toString().equalsIgnoreCase("0")) {
                    mNote.setQuantity(Integer.parseInt(mQuantityEditText.getText().toString()));
                }
            }
        });
        mPreparationEditText.setOnFocusChangeListener((view, b) -> {
            if(b){
                mBorderPreparationView.setBackgroundResource(R.color.primary);
            }else{
                mBorderPreparationView.setBackgroundResource(R.color.secondary_font);
                if(mPreparationEditText.getText().toString() != null) {
                    mNote.setPreparationText(mPreparationEditText.getText().toString());
                }
            }
        });
        mPrecisionEditText.setOnFocusChangeListener((view, b) -> {
            if(b){
                mBorderPrecisionsView.setBackgroundResource(R.color.primary);
            }else{
                mBorderPrecisionsView.setBackgroundResource(R.color.secondary_font);
                if(mPrecisionEditText.getText().toString() != null) {
                    mNote.setDetails(mPrecisionEditText.getText().toString());
                }
            }
        });

        mDescriptionEditText.setOnTouchListener((view, motionEvent) -> {

            view.getParent().requestDisallowInterceptTouchEvent(true);
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_UP:
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }

            return false;
        });
        mPreparationEditText.setOnTouchListener((view, motionEvent) -> {

            view.getParent().requestDisallowInterceptTouchEvent(true);
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_UP:
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }

            return false;
        });
        mPrecisionEditText.setOnTouchListener((view, motionEvent) -> {

            view.getParent().requestDisallowInterceptTouchEvent(true);
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_UP:
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }

            return false;
        });


        mPreparationTimeLayout.setOnClickListener(view -> {
            TimePickerWithSeconds timePicker = new TimePickerWithSeconds(mNote.getPreparationTime()[0],
                    mNote.getPreparationTime()[1], mNote.getPreparationTime()[2]);
            timePicker.setTitle("Selectionnez une heure");

            timePicker.setOnTimeSetOption((hour, minute, seconds) -> {
                mNote.setPreparationTime(new int[]{hour, minute, seconds});
                mPreparationTimeTextView.setText(mNote.getPreparationTimeAsString());
                mFinalTimeTextView.setText(mNote.getTotalTimeAsString());
            });

            /* To show the dialog you have to supply the "fragment manager"
                and a tag (whatever you want)
             */
            timePicker.show(getSupportFragmentManager(), "time_picker");
        });
        mCookTimeLayout.setOnClickListener(view -> {
            TimePickerWithSeconds timePicker = new TimePickerWithSeconds(mNote.getCookTime()[0],
                    mNote.getCookTime()[1], mNote.getCookTime()[2]);
            timePicker.setTitle("Selectionnez une heure");

            timePicker.setOnTimeSetOption((hour, minute, seconds) -> {
                mNote.setCookTime(new int[]{hour, minute, seconds});
                mCookTimeTextView.setText(mNote.getCookTimeAsString());
                mFinalTimeTextView.setText(mNote.getTotalTimeAsString());
            });

            /* To show the dialog you have to supply the "fragment manager"
                and a tag (whatever you want)
             */
            timePicker.show(getSupportFragmentManager(), "time_picker");
        });
        mWaitTimeLayout.setOnClickListener(view -> {
            TimePickerWithSeconds timePicker = new TimePickerWithSeconds(mNote.getWaitTime()[0],
                    mNote.getWaitTime()[1], mNote.getWaitTime()[2]);
            timePicker.setTitle("Selectionnez une heure");

            timePicker.setOnTimeSetOption((hour, minute, seconds) -> {
                mNote.setWaitTime(new int[]{hour, minute, seconds});
                mWaitTimeTextView.setText(mNote.getWaitTimeAsString());
                mFinalTimeTextView.setText(mNote.getTotalTimeAsString());
            });

            /* To show the dialog you have to supply the "fragment manager"
                and a tag (whatever you want)
             */
            timePicker.show(getSupportFragmentManager(), "time_picker");
        });

        mIngredientsRecycler.setAdapter(mIngredientsAdapter);
        mToolsRecycler.setAdapter(mToolsAdapter);
        enableSwipeToDeleteAndUndo();

        mAddToolsImageView.setOnClickListener(view -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CreateNoteActivity.this, R.style.PopupDialogTheme);

            final EditText et = new EditText(CreateNoteActivity.this);
            et.setTextColor(getResources().getColor(R.color.secondary_font));
            et.getBackground().setColorFilter(getResources().getColor(R.color.secondary_font), PorterDuff.Mode.SRC_ATOP);

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(et);
            alertDialogBuilder.setMessage("Entrer le nom de l'Ustensil");

            // set dialog message
            alertDialogBuilder.setCancelable(false).setPositiveButton("OK", (dialog, id) -> {
                String text = et.getText().toString().trim();
                if(mNote.hasCookingTool(text)){
                    Snackbar.make(mRootLayout, "L'Ustensil "+text+" existe déjà", Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }
                if(!text.isEmpty()){
                    mNote.addCookingTool(text);
                    mToolsAdapter.notifyDataSetChanged();
                }
            }).setNeutralButton("Annuler", null);


            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.secondary_font));
            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getColor(R.color.secondary_font));

        });

        mAddIngredientImageView.setOnClickListener(view -> {
            Ingredient ingredient = new Ingredient(getResources().getString(R.string.ingredient_name_unknown), 100, Ingredient.Measure.MILLIGRAMME);
            mNote.addIngredient(ingredient);
            mIngredientsAdapter.notifyDataSetChanged();
        });

        mPictureAddButton.setOnClickListener(view -> {
            chooseDialogPicture();
        });

        mTagLayout.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(CreateNoteActivity.this, view, Gravity.CENTER, 0, R.style.Theme_KNote_PopupMenuStyle);

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                Tag tag = mAppManager.getTag(menuItem.getTitle().toString()).get();
                mNote.addTag(tag);
                tag.addAffiliateNote(mNote.getUUID());
                addTagChipToChipGroup(tag);
                return true;
            });
            mAppManager.getTagsManager().forEach(tag -> {
                if(!mNote.getTags().contains(tag)) {
                    SpannableString s = new SpannableString(tag.getName());
                    s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.secondary_font)), 0, s.length(), 0);
                    popupMenu.getMenu().add(s);
                }
            });
            popupMenu.show();
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Uri uri=data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                mNote.setImage(bitmap);
                mPictureImageView.setImageBitmap(mNote.getImage());
                mPictureImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else if(resultCode== ImagePicker.RESULT_ERROR){
            Snackbar.make(mRootLayout, "Désolé, une erreur est survenue", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallbackIngredients = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final Ingredient ingredient = mIngredientsAdapter.getListIngredients().get(position);

                mNote.removeIngredient(ingredient);
                mIngredientsAdapter.notifyItemRemoved(position);
            }
        };
        SwipeToDeleteCallback swipeToDeleteCallbackTools = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final String tools = mToolsAdapter.getListTools().get(position);

                mNote.removeCookingTool(tools);
                mToolsAdapter.notifyItemRemoved(position);
            }
        };


        ItemTouchHelper itemTouchHelperIngredients = new ItemTouchHelper(swipeToDeleteCallbackIngredients);
        itemTouchHelperIngredients.attachToRecyclerView(mIngredientsRecycler);
        ItemTouchHelper itemTouchHelperTools = new ItemTouchHelper(swipeToDeleteCallbackTools);
        itemTouchHelperTools.attachToRecyclerView(mToolsRecycler);
    }

    private void addTagChipToChipGroup(Tag tag){
        Chip chip = new Chip(CreateNoteActivity.this);
        chip.setText(tag.getName());
        chip.setCloseIconVisible(true);

        chip.setOnCloseIconClickListener(view -> {
            // Remove the chip with an animation
            Animation anim = new AlphaAnimation(1f,0f);
            anim.setDuration(250);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mTagsChipGroup.removeView(chip);
                    tag.removeAffiliateNote(mNote.getUUID());
                    mNote.removeTag(tag);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            chip.startAnimation(anim);
        });

        mTagsChipGroup.addView(chip);
        Animation anim = new AlphaAnimation(0f,1f);
        anim.setDuration(500);

        chip.startAnimation(anim);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(Note.class, new BitmapSerializer())
                .create();

        switch(item.getItemId()){
            case R.id.create_note_menu_check: {//this item has your app icon



                FileUtils.writeFileExternalStorage("Notes"+File.separator+mNote.getUUID()+".json", gson.toJson(mNote), true);
                try {
                    mAppManager.updateTagFile();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                finish();
                return true;
            }
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void chooseDialogPicture(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CreateNoteActivity.this, R.style.PopupDialogTheme);

        View layout = getLayoutInflater().inflate(R.layout.picture_mode_choose_dialog, null);
        LinearLayout photo = layout.findViewById(R.id.lytCameraPick);
        LinearLayout gallery = layout.findViewById(R.id.lytGalleryPick);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setMessage("Choisissez");

        // set dialog message
        alertDialogBuilder.setCancelable(false).setNeutralButton("Annuler", null);


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();

        photo.setOnClickListener(view -> {
            alertDialog.cancel();
            ImagePicker.with(this)
                    .cameraOnly()
                    .crop()
                    .galleryMimeTypes(new String[]{"image/png", "image/jpg", "image/jpeg", "image/bmp"})
                    .start();
        });
        gallery.setOnClickListener(view -> {
            alertDialog.cancel();
            ImagePicker.with(this)
                    .galleryOnly()
                    .crop()
                    .galleryMimeTypes(new String[]{"image/png", "image/jpg", "image/jpeg", "image/bmp"})
                    .start();
        });

        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getColor(R.color.secondary_font));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.create_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mAppManager.removeNote(mNote);
        MainActivity.INSTANCE.mNoteAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
