package fr.shayfox.k_note.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import fr.shayfox.k_note.R;
import fr.shayfox.k_note.adapter.NoteAdapter;
import fr.shayfox.k_note.databinding.ActivityMainBinding;
import fr.shayfox.k_note.manager.AppManager;
import fr.shayfox.k_note.manager.ExeptionHandlerPrint;
import fr.shayfox.k_note.manager.SwipeToDeleteCallback;
import fr.shayfox.k_note.model.Note;
import fr.shayfox.k_note.model.Tag;
import fr.shayfox.k_note.serializer.BitmapDeserializer;
import fr.shayfox.k_note.utils.FileUtils;

public class MainActivity extends AppCompatActivity {
    public ActivityMainBinding binding;
    public AppManager mAppManager;
    public NoteAdapter mNoteAdapter;

    private DrawerLayout mDrawerLayout;

    public static MainActivity INSTANCE;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        INSTANCE = this;
        Thread.setDefaultUncaughtExceptionHandler(new ExeptionHandlerPrint());

        checkUpdate();

        mAppManager = new AppManager(getApplicationContext());
        mNoteAdapter = new NoteAdapter(mAppManager.getNoteList(), this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.addNoteButton.setOnClickListener(view ->{
            Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
            Note note = new Note();
            mAppManager.addNote(note);
            intent.putExtra("NoteUUID", note.getUUID());
            intent.putExtra("isEdit", false);
            startActivity(intent);
        });

        ArrayAdapter<String> adapter = new ArrayAdapter(this.getApplicationContext(), R.layout.tag_dropdown_menu, mAppManager.getTagsManager().stream().map(tag -> tag.getName()).collect(Collectors.toList()));
        adapter.setDropDownViewResource(R.layout.tag_dropdown_menu);
        binding.autoCompleteTextView.setAdapter(adapter);
        adapter.add(String.valueOf(getResources().getText(R.string.all)));
        adapter.sort(String::compareTo);
        adapter.notifyDataSetChanged();
        binding.autoCompleteTextView.setDropDownBackgroundDrawable(
                ResourcesCompat.getDrawable(getResources() , R.drawable.dropdown_menu_tag, null));

        binding.itemRecycler.setAdapter(mNoteAdapter);
        enableSwipeToDeleteAndUndo();
        binding.autoCompleteTextView.setText(getResources().getText(R.string.all), false);

        binding.autoCompleteTextView.setOnItemClickListener((adapterView, view, position, l) -> {
            final String selectedValue = adapter.getItem(position);
            updateListTagNote(selectedValue);
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.nav_makeTag:
                    mDrawerLayout.closeDrawer(GravityCompat.START);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.PopupDialogTheme);

                    final EditText et = new EditText(MainActivity.this);
                    et.getBackground().setColorFilter(getResources().getColor(R.color.secondary_font), PorterDuff.Mode.SRC_ATOP);

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(et);
                    alertDialogBuilder.setMessage("Entrez le nom du tag");

                    // set dialog message
                    alertDialogBuilder.setCancelable(false).setPositiveButton("OK", (dialog, id) -> {
                        String text = et.getText().toString();
                        if(mAppManager.hasTag(text)){
                            Snackbar.make(binding.coordinatorLayout, "Le tag "+text+" existe déjà", Snackbar.LENGTH_LONG)
                                    .show();
                            return;
                        }
                        if(!text.trim().isEmpty()){
                            Tag tag = new Tag(text);
                            mAppManager.addTag(tag);
                            adapter.add(tag.getName());
                            try {
                                mAppManager.updateTagFile();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            Snackbar.make(binding.coordinatorLayout, "Vous avez créer un nouveau tag: "+tag.getName(), Snackbar.LENGTH_LONG)
                                    .setAction("Annuler", view -> {
                                        mAppManager.removeTag(tag);
                                        adapter.remove(tag.getName());
                                        try {
                                            mAppManager.updateTagFile();
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }).show();
                        }
                    }).setNeutralButton("Annuler", null);


                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();

                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.secondary_font));
                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getColor(R.color.secondary_font));

                    break;
                case R.id.nav_manageTag:
                    mDrawerLayout.closeDrawer(GravityCompat.START);

                    AlertDialog.Builder alertDialogBuilderManage = new AlertDialog.Builder(MainActivity.this, R.style.PopupDialogTheme);

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilderManage.setMessage("Choisisez le tag à supprimer");

                    final LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                    View promptView = layoutInflater.inflate(R.layout.popup_autocomplete_textview, null);
                    alertDialogBuilderManage.setView(promptView);

                    final AutoCompleteTextView textView = (AutoCompleteTextView)
                            promptView.findViewById(R.id.autoCompleteTextView_popup);

                    ArrayAdapter<String> adapterPopupManager = new ArrayAdapter(this.getApplicationContext(), R.layout.tag_dropdown_menu, mAppManager.getTagsManager().stream().map(tag -> tag.getName()).collect(Collectors.toList()));
                    adapterPopupManager.setDropDownViewResource(R.layout.tag_dropdown_menu);
                    textView.setDropDownBackgroundDrawable(
                            ResourcesCompat.getDrawable(getResources() , R.drawable.dropdown_menu_tag, null));
                    adapterPopupManager.sort(String::compareTo);
                    textView.setAdapter(adapterPopupManager);
                    textView.setText(adapterPopupManager.getItem(0), false);

                    // set dialog message
                    alertDialogBuilderManage.setCancelable(false).setPositiveButton("OK", (dialog, id) -> {
                        String text = textView.getText().toString();
                        if(mAppManager.hasTag(text)){
                            final Tag tag = mAppManager.getTag(text).get();

                            mAppManager.removeTag(tag);
                            adapter.remove(tag.getName());
                            adapter.sort(String::compareTo);
                            try {
                                mAppManager.updateTagFile();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

                            Snackbar snackbar = Snackbar
                                    .make(binding.coordinatorLayout, "Le tag "+tag.getName()+" va être supprimée definitivement !", Snackbar.LENGTH_LONG);
                            snackbar.setAction("Annuler", view -> {
                                mAppManager.addTag(tag);
                                adapter.add(tag.getName());
                                adapter.sort(String::compareTo);
                                try {
                                    mAppManager.updateTagFile();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            snackbar.setActionTextColor(Color.YELLOW);
                            snackbar.show();
                        }
                    }).setNeutralButton("Annuler", null);

                    // create alert dialog
                    AlertDialog alertDialogManage = alertDialogBuilderManage.create();
                    // show it
                    alertDialogManage.show();

                    alertDialogManage.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.secondary_font));
                    alertDialogManage.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getColor(R.color.secondary_font));


                    break;
            }
            return false;
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, binding.toolbar,
                R.string.nav_open, R.string.nav_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        /*
        MENU ACTION
         */
        binding.toolbar.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()){
                case R.id.action_changeMode:
                    mNoteAdapter.itemMode++;
                    if(mNoteAdapter.itemMode>1)mNoteAdapter.itemMode=0;
                    binding.itemRecycler.setAdapter(binding.itemRecycler.getAdapter());
                    mNoteAdapter.notifyDataSetChanged();
                    if(mNoteAdapter.itemMode == 0) {
                        item.setIcon(R.drawable.ic_blocs);
                        binding.itemRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                    }else if(mNoteAdapter.itemMode == 1){
                        item.setIcon(R.drawable.ic_list);
                        final int span  = (binding.itemRecycler.getWidth()-dip2px(getApplicationContext(), 7))/(dip2px(getApplicationContext(), 100)+dip2px(getApplicationContext(), 7));
                        System.out.println(span);
                        binding.itemRecycler.setLayoutManager(new GridLayoutManager(getApplicationContext(), span));
                    }

                    return true;

                case R.id.action_information:
                    Intent intent = new Intent(MainActivity.this, InfoActivity.class);

                    startActivity(intent);
                    return true;
                case R.id.action_import:
                    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFile.setType("*/*");
                    chooseFile = Intent.createChooser(chooseFile, "Choisissez un fichier");
                    startActivityForResult(chooseFile, 1);
                    return true;
            }

            return false;
        });
    }

    private void checkUpdate() {
//        PackageManager manager = getApplicationContext().getPackageManager();
//        PackageInfo info;
//        try {
//            info = manager.getPackageInfo(
//                    getApplicationContext().getPackageName(), 0);
//        } catch (PackageManager.NameNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        String version = info.versionName;
//
//        System.out.println("Version: "+version);
//
//        try {
//            System.out.println("connection");
//            final HttpURLConnection connection = (HttpURLConnection) new URL("https://api.github.com/repos/ShayF0x/K-Note/releases/latest").openConnection();
//            //connection.setRequestProperty("User-Agent", USER_AGENT);
//            connection.connect();
//
//            if (connection.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
//                System.out.println("error");
//                return;
//            }
//
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF8")))) {
//                System.out.println(reader);
//            } catch (JsonSyntaxException | NumberFormatException ex) {
//                System.out.println("Failed to parse the latest version info.");
//                ex.printStackTrace();
//            }
//        } catch (IOException ex) {
//            System.out.println("LOG: Failed to get release info from api.github.com.");
//            ex.printStackTrace();
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    File file = new File(getFilePathFromURI(this, data.getData()));

                    if(!getFileName(data.getData()).endsWith(".json")){
                        Snackbar snackbar = Snackbar
                                .make(binding.coordinatorLayout, "Désolé mais votre fichier est incorrect", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        return;
                    }
                    try {
                        FileInputStream fis = new FileInputStream(file);

                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader bufferedReader = new BufferedReader(isr);
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            sb.append(line);
                        }

                        String json = sb.toString();

                        if(!FileUtils.isJSONValid(json)){
                            Snackbar snackbar = Snackbar
                                    .make(binding.coordinatorLayout, "Désolé mais votre fichier est incorrect", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            return;
                        }
                        Gson gson = new GsonBuilder()
                                .registerTypeAdapter(Note.class, new BitmapDeserializer(mAppManager))
                                .create();
                        Note note = gson.fromJson(json, Note.class);
                        if(note.getUUID() == null){
                            Snackbar snackbar = Snackbar
                                    .make(binding.coordinatorLayout, "Désolé mais votre fichier est incorrect", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            return;
                        }
                        if(mAppManager.getNoteByUUID(note.getUUID()) != null){
                            Snackbar snackbar = Snackbar
                                    .make(binding.coordinatorLayout, "Vous possèdez déjà cette note !", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            return;
                        }
                        FileUtils.writeFileExternalStorage("Notes"+File.separator+note.getUUID()+".json", json, true);
                        try {
                            mAppManager.updateTagFile();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        mAppManager.addNote(note);
                        mNoteAdapter.notifyDataSetChanged();

                        Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
                        intent.putExtra("NoteUUID", note.getUUID());
                        intent.putExtra("isEdit", false);
                        startActivity(intent);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getFilePathFromURI(Context context, Uri contentUri) {
        String fileName = getFileName(contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            String TEMP_DIR_PATH = Environment.getExternalStorageDirectory().getPath();
            File copyFile = new File(TEMP_DIR_PATH + File.separator + fileName);
            Log.d("DREG", "FilePath copyFile: " + copyFile);
            FileUtils.copy(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }


    /**
     * Obtains the file name for a URI using content resolvers. Taken from the following link
     * https://developer.android.com/training/secure-file-sharing/retrieve-info.html#RetrieveFileInfo
     *
     * @param uri a uri to query
     * @return the file name with no path
     * @throws IllegalArgumentException if the query is null, empty, or the column doesn't exist
     */
    private String getFileName(Uri uri) throws IllegalArgumentException {
        // Obtain a cursor with information regarding this uri
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor.getCount() <= 0) {
            cursor.close();
            throw new IllegalArgumentException("Can't obtain file name, cursor is empty");
        }

        cursor.moveToFirst();

        String fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));

        cursor.close();

        return fileName;
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    public void updateListTagNote(String selectedValue){

        if(selectedValue.equalsIgnoreCase("Tout")){
            mNoteAdapter.setListNote(mAppManager.getNoteList());
            mNoteAdapter.notifyDataSetChanged();
        }else if(mAppManager.hasTag(selectedValue)){
            final Tag selectedTag = mAppManager.getTag(selectedValue).get();

            mNoteAdapter.setListNote(mAppManager.getNoteList().stream().filter(note -> note.hasTag(selectedTag)).collect(Collectors.toList()));
            mNoteAdapter.notifyDataSetChanged();
        }

        mNoteAdapter.getListNote().sort((note, note1) -> {
            if (note.isLiked() == note1.isLiked()) return 0;
            else if (note.isLiked() && !note1.isLiked()) return -1;
            return 1;
        });
        mNoteAdapter.notifyDataSetChanged();
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if(mNoteAdapter.itemMode != 0)return;
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                if(mNoteAdapter.itemMode != 0)return;
                final int position = viewHolder.getAdapterPosition();
                final Note note = mAppManager.getNoteList().get(position);
                AtomicBoolean restore = new AtomicBoolean(false);

                mAppManager.removeNote(note);
                mNoteAdapter.notifyItemRemoved(position);
                updateListTagNote(binding.autoCompleteTextView.getText().toString());

                Snackbar snackbar = Snackbar
                        .make(binding.coordinatorLayout, "La recette "+note.getName()+" va être supprimée definitivement !", Snackbar.LENGTH_LONG);
                snackbar.setAction("Annuler", view -> {
                    restore.set(true);
                    mAppManager.addNote(note);
                    mNoteAdapter.notifyItemInserted(position);
                    updateListTagNote(binding.autoCompleteTextView.getText().toString());
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.addCallback(new Snackbar.Callback(){
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, @DismissEvent int event) {
                        if(!restore.get()){
                            //Checking the availability state of the External Storage.
                            String state = Environment.getExternalStorageState();
                            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                                //If it isn't mounted - we can't write into it.
                                return;
                            }

                            //Create a new file that points to the root directory, with the given name:
                            File file = new File(getExternalFilesDir(null), "Notes"+File.separator+note.getUUID()+".json");
                            file.delete();
                        }
                    }
                });
                snackbar.show();
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(binding.itemRecycler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListNote();
    }

    public void updateListNote() {
        mNoteAdapter.notifyDataSetChanged();
    }
}