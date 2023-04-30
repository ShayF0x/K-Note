package fr.shayfox.k_note.controller;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import fr.shayfox.k_note.BuildConfig;
import fr.shayfox.k_note.R;
import fr.shayfox.k_note.adapter.NoteAdapter;
import fr.shayfox.k_note.databinding.ActivityMainBinding;
import fr.shayfox.k_note.manager.AppManager;
import fr.shayfox.k_note.manager.ExeptionHandlerPrint;
import fr.shayfox.k_note.manager.Response;
import fr.shayfox.k_note.manager.SwipeToDeleteCallback;
import fr.shayfox.k_note.model.Note;
import fr.shayfox.k_note.model.Tag;
import fr.shayfox.k_note.serializer.BitmapDeserializer;
import fr.shayfox.k_note.utils.FileUtils;
import io.noties.markwon.Markwon;

public class MainActivity extends AppCompatActivity {

    public String VERSION_PATCH = "LastVersionPatch";
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

        if(!canReadWriteExternal()){
            requestPermission();
        }

        mAppManager = new AppManager(getApplicationContext());
        mNoteAdapter = new NoteAdapter(mAppManager.getNoteList(), this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        try {
            checkUpdate();
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        PackageManager manager = getApplicationContext().getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(
                    getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        String version = info.versionName;
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Constantes", 0);

        if(!pref.getString(VERSION_PATCH, "").equalsIgnoreCase(version)){
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(VERSION_PATCH, version);
            editor.commit();

            Response response = null;
            try {
                response = getResponseAll("https://api.github.com/repos/ShayF0x/K-Note/releases/tags/"+version);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            String message = "La mise à jour ne contient pas de note";
            if(response != null){
                if(response.get_code() == 404) {
                    message = "Votre version n'est pas officielle";
                }else if (response.get_code() == 452){
                    message = "Vous n'avez pas de connection internet";
                }else if (response.get_code() == 200){
                    Gson gson = new Gson();
                    message = gson.fromJson(response.get_content(), JsonObject.class).get("body").getAsString();
                }
            }

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.PopupDialogTheme);

            final Markwon markwon = Markwon.create(MainActivity.this);
            final Spanned markdown = markwon.toMarkdown(message);

            alertDialogBuilder.setMessage(markdown);

            // set dialog message
            alertDialogBuilder.setCancelable(false).setPositiveButton("OK", null);

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.secondary_font));
        }

        binding.addNoteButton.setOnClickListener(view ->{
            Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
            Note note = new Note("note n°"+(mAppManager.getNoteList().size()+1));
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
                case R.id.action_wiki:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ShayF0x/K-Note/wiki"));

                    startActivity(browserIntent);
                    return true;
                case R.id.action_import:
                    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFile.setType("*/*");
                    chooseFile = Intent.createChooser(chooseFile, "Choisissez un fichier (.knoterecipe)");
                    startActivityForResult(chooseFile, 1);
                    return true;
            }

            return false;
        });
    }

    private void checkUpdate() throws IOException, ExecutionException, InterruptedException {
        PackageManager manager = getApplicationContext().getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(
                    getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        String version = info.versionName;

        String response = getResponse("https://api.github.com/repos/ShayF0x/K-Note/releases/latest");
        if(response == null)return;

        Gson gson = new Gson();
        String version_latest = gson.fromJson(response, JsonObject.class).get("tag_name").getAsString();

        String[] test = new String[]{"1"+version.replaceAll("\\.", ""), "1"+version_latest.replaceAll("\\.", "")};
        if(isInteger(test[0]) && isInteger(test[1])){
            if(Integer.parseInt(test[1])>Integer.parseInt(test[0])){
                String link = gson.fromJson(response, JsonObject.class).get("assets").getAsJsonArray().get(0).getAsJsonObject().get("browser_download_url").getAsString();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.PopupDialogTheme);

                alertDialogBuilder.setMessage("Une mise à jour est disponible voulez-vous l'installer ? (version actuelle: "+version+", nouvelle version: "+version_latest+")");

                // set dialog message
                alertDialogBuilder.setCancelable(false).setPositiveButton("Oui", (dialog, id) -> {

                    DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri uri = Uri.parse(link);
                    DownloadManager.Request request = new DownloadManager.Request(uri);

                    File file = new File(getExternalFilesDir(null), "Download"+File.separator+"AppUpdate.apk");
                    if(file.exists())file.delete();
                    Uri fileUri  = (!file.getAbsolutePath().endsWith(".apk") && Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) ?
                            FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", file) :
                            Uri.fromFile(file);

                    request.setDestinationUri(fileUri);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                    downloadManager.enqueue(request);

                    IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                    BroadcastReceiver receiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            // App already have the permission to install so launch the APK installation.
                            Intent intentUpdate;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Uri apkUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                                intentUpdate = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                                intentUpdate.setData(apkUri);
                                intentUpdate.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } else {
                                Uri apkUri = Uri.fromFile(file);
                                intentUpdate = new Intent(Intent.ACTION_VIEW);
                                intentUpdate.setDataAndType(apkUri, "application/vnd.android.package-archive");
                                intentUpdate.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            }



                            context.startActivity(intentUpdate);

                            finish();
                        }
                    };
                    registerReceiver(receiver, filter);

                }).setNegativeButton("Non", null);


                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.secondary_font));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.secondary_font));
            }
        }

    }

    private Response getResponseAll(String src) throws ExecutionException, InterruptedException {
        AsyncTask<String, Void, Response> async = new MainActivity.getResponseTask().execute(src);
        return async.get();
    }
    private String getResponse(String src) throws ExecutionException, InterruptedException {
        AsyncTask<String, Void, Response> async = new getResponseTask().execute(src);
        if(async.get().get_code() == 452){
            Snackbar
                    .make(binding.coordinatorLayout, "Vous êtes hors-ligne", Snackbar.LENGTH_SHORT).show();
            return null;
        }
        return async.get().get_content();
    }

    public boolean isInteger(String str){
        try{
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    File file = new File(getFilePathFromURI(this, data.getData()));

                    if(!getFileName(data.getData()).endsWith(".knoterecipe")){
                        Snackbar snackbar = Snackbar
                                .make(binding.coordinatorLayout, "Désolé mais votre fichier est incorrect (ce doit être un fichier knoterecipe)", Snackbar.LENGTH_LONG);
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

    private final int REQUEST_WRITE_PERMISSION = 786;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
    }

    private boolean canReadWriteExternal() {
        return !(Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
    }

    static class getResponseTask extends AsyncTask<String, Void, Response> {

        protected Response doInBackground(String... urls) {
            if(!internetIsConnected())
                return new Response(null, 452);
            StringBuilder stringBuilder = new StringBuilder();
            try {
                for (String link:urls) {
                    URL url = null;

                    url = new URL(link);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() != 200) {
                        return new Response(conn.getResponseMessage(), conn.getResponseCode());
                    }
                    InputStreamReader in = new InputStreamReader(conn.getInputStream());
                    BufferedReader br = new BufferedReader(in);
                    String output;
                    while ((output = br.readLine()) != null) {
                        return new Response(output, conn.getResponseCode());
                    }
                    conn.disconnect();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return new Response(null, 0);
        }
        public boolean internetIsConnected() {
            try {
                String command = "ping -c 1 google.com";
                return (Runtime.getRuntime().exec(command).waitFor() == 0);
            } catch (Exception e) {
                return false;
            }
        }
    }
}