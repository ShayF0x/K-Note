package fr.shayfox.k_note.controller;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spanned;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import fr.shayfox.k_note.R;
import fr.shayfox.k_note.manager.Response;
import fr.shayfox.k_note.model.Ingredient;
import fr.shayfox.k_note.model.Note;
import fr.shayfox.k_note.model.Tag;
import fr.shayfox.k_note.serializer.BitmapSerializer;
import fr.shayfox.k_note.utils.FileUtils;
import io.noties.markwon.Markwon;

public class InfoActivity extends AppCompatActivity {

    public MaterialToolbar toolbar;
    public TextView mVersionTextView;

    public ConstraintLayout mChangeLogLayout, mAuthorLayout, mBugLayout;

    private int numberOfTaps;
    private long timeMillisOfFirstClick;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        toolbar = findViewById(R.id.info_activity_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> {
            finish();
        });

        mChangeLogLayout = findViewById(R.id.info_activity_changelog_layout);
        mAuthorLayout = findViewById(R.id.info_activity_author_layout);
        mBugLayout = findViewById(R.id.info_activity_bug_layout);
        mVersionTextView = findViewById(R.id.info_activity_version_textfield);

        PackageManager manager = getApplicationContext().getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(
                    getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        String version = info.versionName;
        mVersionTextView.setText(version);

        mChangeLogLayout.setOnClickListener(view -> {

            Response response = null;
            try {
                response = getResponse("https://api.github.com/repos/ShayF0x/K-Note/releases/tags/"+version);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            String message = "La mise à jour ne contient pas de note";
            if(response != null){
                if(response.get_code() == 404){
                    message = "Votre version n'est pas officielle";
                }else if (response.get_code() == 200){
                    Gson gson = new Gson();
                    message = gson.fromJson(response.get_content(), JsonObject.class).get("body").getAsString();
                }
            }

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(InfoActivity.this, R.style.PopupDialogTheme);

            final Markwon markwon = Markwon.create(InfoActivity.this);
            final Spanned markdown = markwon.toMarkdown(message);

            alertDialogBuilder.setMessage(markdown);

            // set dialog message
            alertDialogBuilder.setCancelable(false).setPositiveButton("OK", null);

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.secondary_font));

        });

        mBugLayout.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ShayF0x/K-Note/issues"));
            startActivity(browserIntent);
        });

        mAuthorLayout.setOnClickListener(view -> {

            if (timeMillisOfFirstClick > 0 && (System.currentTimeMillis() - timeMillisOfFirstClick) > 2000)
                numberOfTaps = 1;
            else
                numberOfTaps++;
            if (numberOfTaps == 1)
                timeMillisOfFirstClick = System.currentTimeMillis();
            if (numberOfTaps == 7 && (System.currentTimeMillis() - timeMillisOfFirstClick) <= 2000 && !MainActivity.INSTANCE.mAppManager.hasNote("Omelette au lait")) {
                final Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Note.class, new BitmapSerializer())
                        .create();
                Toast.makeText(this, "Vous avez activé la recette de l'omelette au lait !", Toast.LENGTH_SHORT).show();
                final Note note = new Note("Omelette au lait", "Qui a dit que dans une omelette on met pas de lait ?",
                        BitmapFactory.decodeResource(getResources(), R.drawable.omelette),
                        new ArrayList<>(), new int[]{0, 5, 0}, new int[]{0, 5, 0}, new int[]{0, 0, 0},
                        2,
                        new ArrayList<>(Arrays.asList(
                                new Ingredient("Sel", 1, "pincée"),
                                new Ingredient("oeufs", 4, null),
                                new Ingredient("lait", 20, Ingredient.Measure.CENTILITRE),
                                new Ingredient("gruyère", 30, Ingredient.Measure.GRAMME)
                        )),
                        "ÉTAPE 1\nDans un bol mélanger les œufs puis y incorporer le lait puis le gruyère et le sel.\n\nÉTAPE 2\nFaire cuire environs 5 minutes.\n\nÉTAPE 3\nEt vous obtenez une omelette très aérée et moelleuse.",
                        new ArrayList<>(Arrays.asList(
                                "1 pôele",
                                "1 bol",
                                "1 cuillère en bois"
                        )), null);
                FileUtils.writeFileExternalStorage("Notes"+ File.separator+note.getUUID()+".json", gson.toJson(note), true);
                MainActivity.INSTANCE.mAppManager.addNote(note);
            }

        });


    }

    private Response getResponse(String src) throws ExecutionException, InterruptedException {
        AsyncTask<String, Void, Response> async = new MainActivity.getResponseTask().execute(src);
        return async.get();
    }
}
