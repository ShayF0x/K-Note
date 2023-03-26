package fr.shayfox.k_note.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import fr.shayfox.k_note.BuildConfig;
import fr.shayfox.k_note.R;
import fr.shayfox.k_note.controller.fragments.InfoNoteFragment;
import fr.shayfox.k_note.controller.fragments.PreparationNoteFragment;
import fr.shayfox.k_note.controller.fragments.ResourcesNoteFragment;
import fr.shayfox.k_note.manager.AppManager;
import fr.shayfox.k_note.model.Note;
import fr.shayfox.k_note.utils.FileUtils;

public class ConsultNoteActivity extends AppCompatActivity {

    private AppManager mAppManager;
    private Note mNote;

    private MaterialToolbar mToolbar;

    private BottomNavigationView mNavigationView;

    private ImageView mPictureImageView;

    private TextView mNameTextView;

    private ResourcesNoteFragment mResourcesNoteFragment;
    private InfoNoteFragment mInfoNoteFragment;
    private PreparationNoteFragment mPreparationNoteFragment;

    private int mQuantity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_consult);

        MainActivity mainActivity = MainActivity.INSTANCE;
        mAppManager = mainActivity.mAppManager;

        UUID currentUUID = (UUID) getIntent().getSerializableExtra("NoteUUID");
        mNote = mAppManager.getNoteByUUID(currentUUID);
        mQuantity = mNote.getQuantity();

        mResourcesNoteFragment = new ResourcesNoteFragment(mNote, value -> {
            mQuantity = value;
        });
        mInfoNoteFragment = new InfoNoteFragment(mNote, value -> {
            mQuantity = value;
        });
        mPreparationNoteFragment = new PreparationNoteFragment(mNote);

        /*
        REGISTER
         */

        mPictureImageView = findViewById(R.id.consult_note_activity_picture);

        mNameTextView = findViewById(R.id.consult_note_activity_edittext_name);

        mToolbar = findViewById(R.id.consult_note_activity_toolbar);

        mNavigationView = findViewById(R.id.consult_note_activity_navigation_view);

        /*
        SETUP
         */

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setNavigationOnClickListener(view -> {
            finish();
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.consult_note_activity_fragment_container, mInfoNoteFragment).commit();

        mNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.consult_note_navigation_info:
                    getSupportFragmentManager().beginTransaction().replace(R.id.consult_note_activity_fragment_container, mInfoNoteFragment).commit();
                    mInfoNoteFragment.setQuantity(mQuantity);
                    return true;
                case R.id.consult_note_navigation_resources:
                    getSupportFragmentManager().beginTransaction().replace(R.id.consult_note_activity_fragment_container, mResourcesNoteFragment).commit();
                    mResourcesNoteFragment.setQuantity(mQuantity);
                    return true;
                case R.id.consult_note_navigation_preparation:
                    getSupportFragmentManager().beginTransaction().replace(R.id.consult_note_activity_fragment_container, mPreparationNoteFragment).commit();
                    return true;
            }
            return false;
        });

        if(mNote.getName() != null && !mNote.getName().isEmpty()) {
            mNameTextView.setText(mNote.getName());
        }

        if(mNote.getImage() != null){
            mPictureImageView.setImageBitmap(mNote.getImage());
            mPictureImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        mToolbar.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()){
                case R.id.consult_note_menu_edit:
                    Intent intent = new Intent(ConsultNoteActivity.this, CreateNoteActivity.class);
                    intent.putExtra("NoteUUID", mNote.getUUID());
                    intent.putExtra("isEdit", true);
                    startActivity(intent);
                    return true;
                case R.id.consult_note_menu_export:
                    try {
                        File file = new File(getExternalFilesDir(null), "Notes"+File.separator+mNote.getUUID()+".json");

                        File tempFile = File.createTempFile(mNote.getName(),".json");

                        FileUtils.copy(file, tempFile);

                        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + "." + getLocalClassName() + ".provider", tempFile);
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent.setType("*/json");
                        startActivity(Intent.createChooser(shareIntent, null));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    return true;
                case R.id.consult_note_menu_download:
                    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        File file = new File(getExternalFilesDir(null), "Notes" + File.separator + mNote.getUUID() + ".json");

                        File destFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Recettes" + File.separator + mNote.getName() + ".json");
                        System.out.println(destFile.getAbsolutePath());

                    try {
                        destFile.getParentFile().mkdirs();
                        destFile.createNewFile();
                        FileUtils.copy(file, destFile);

                        Intent notifIntent = new Intent();
                        notifIntent.setAction(Intent.ACTION_VIEW);
                        Uri imgUri = Uri.parse(destFile.getParentFile().getAbsolutePath()+File.separator);
                        notifIntent.setDataAndType(imgUri, "*/*");

                        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pIntent = PendingIntent.getActivity(this, 0, notifIntent, 0);

                         NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(this)
                                        .setSmallIcon(R.drawable.ic_download)
                                        .setContentTitle("Recette téléchargé !")
                                        .setContentIntent(pIntent)
                                        .setContentText(mNote.getName()+" à été téléchargée dans Download !");

                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        {
                            String channelId = "Your_channel_id";
                            NotificationChannel channel = new NotificationChannel(
                                    channelId,
                                    "Channel human readable title",
                                    NotificationManager.IMPORTANCE_HIGH);
                            mNotificationManager.createNotificationChannel(channel);
                            mBuilder.setChannelId(channelId);
                        }


                        mNotificationManager.notify(0, mBuilder.build());

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    }
                    return true;
            }

            return false;
        });



    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.consult_note_menu, menu);

        return true;
    }
}
