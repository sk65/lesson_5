package com.example.sharefile.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.sharefile.R;
import com.example.sharefile.util.LogToFile;
import com.example.sharefile.util.PermissionGet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PermissionGet {
    private static final String FILES_AUTHORITY = "com.example.sharefile.provider";
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO = 3;
    private LogToFile fileLogger;
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    private FloatingActionButton optionsButton;
    private FloatingActionButton shareLogButton;
    private FloatingActionButton makePhotoButton;
    private FloatingActionButton chosePhotoButton;
    private ImageView imageView;
    private final int GALLERY_REQUEST = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;
    private boolean clicked;
    private String currentPhotoPath;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileLogger = new LogToFile(this);
        fileLogger.log("onCreate() started");

        initAnimations();
        initButtons();
    }

    private void initButtons() {
        optionsButton = findViewById(R.id.floatingActionButton_options);
        optionsButton.setOnClickListener(this);
        shareLogButton = findViewById(R.id.floatingActionButton_makePhoto);
        shareLogButton.setOnClickListener(this);
        makePhotoButton = findViewById(R.id.floatingActionButton_share);
        makePhotoButton.setOnClickListener(this);
        chosePhotoButton = findViewById(R.id.floatingActionButton_chosePhoto);
        chosePhotoButton.setOnClickListener(this);
    }

    private void onOptionsButtonClicked() {
        setVisibility();
        setAnimation();
        setClickable();
        clicked = !clicked;
    }

    private void setAnimation() {
        if (!clicked) {
            shareLogButton.startAnimation(fromBottom);
            makePhotoButton.startAnimation(fromBottom);
            chosePhotoButton.startAnimation(fromBottom);
            optionsButton.startAnimation(rotateOpen);
        } else {
            shareLogButton.startAnimation(toBottom);
            makePhotoButton.startAnimation(toBottom);
            chosePhotoButton.startAnimation(toBottom);
            optionsButton.startAnimation(rotateClose);
        }
    }

    private void setVisibility() {
        if (!clicked) {
            shareLogButton.setVisibility(View.VISIBLE);
            makePhotoButton.setVisibility(View.VISIBLE);
            chosePhotoButton.setVisibility(View.VISIBLE);
        } else {
            shareLogButton.setVisibility(View.INVISIBLE);
            makePhotoButton.setVisibility(View.INVISIBLE);
            chosePhotoButton.setVisibility(View.INVISIBLE);
        }
    }

    private void setClickable() {
        if (!clicked) {
            shareLogButton.setClickable(true);
            makePhotoButton.setClickable(true);
            chosePhotoButton.setClickable(true);
        } else {
            shareLogButton.setClickable(false);
            makePhotoButton.setClickable(false);
            chosePhotoButton.setClickable(false);
        }
    }

    private void initAnimations() {
        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotete_open_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);

    }

    void shareLogFile() {
        Uri uriToFile = FileProvider.getUriForFile(this, FILES_AUTHORITY, fileLogger.getLogFile());
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType(getContentResolver().getType(uriToFile))
                .setStream(uriToFile)
                .getIntent();
        shareIntent.setData(uriToFile);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(shareIntent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.floatingActionButton_options):
                onOptionsButtonClicked();
                break;
            case (R.id.floatingActionButton_share):
                shareLogFile();
                break;
            case (R.id.floatingActionButton_chosePhoto):
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                photoPickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                break;
            case (R.id.floatingActionButton_makePhoto):
                requestPermissions(PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // не получаеться у меня с курсором, потому что когда я кладу в интент файл (190 строка)
        // data приходит null
        if (resultCode == RESULT_OK) {
            imageView = findViewById(R.id.imageView);
            switch (requestCode) {
                case GALLERY_REQUEST:
                    if (data != null) {
                        Uri selectedImageUri = data.getData();
                        Glide.with(this).load(selectedImageUri).into(imageView);
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    File file = new File(currentPhotoPath);
                    Uri contentUri = Uri.fromFile(file);
                    Glide.with(this).load(contentUri).into(imageView);
                    break;
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
       // на эмуляторе Pixel XL API 30 takePictureIntent.resolveActivity(getPackageManager()) == null програма дальше не идёт
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.sharefile.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void requestPermissions(int requestCode) {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {

            switch (requestCode) {
                case LogToFile.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_LOG: {
                    fileLogger.setPermission(true);
                    break;
                }
                case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO: {
                    dispatchTakePictureIntent();
                    break;
                }
            }

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Permission Required")
                    .setMessage("Storage permission is required to save data")
                    .setPositiveButton("ALLOW", (dialogInterface, i) -> {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{WRITE_EXTERNAL_STORAGE}, requestCode
                        );
                        dialogInterface.dismiss();
                    }).setNegativeButton("DENIED", (dialog, which) -> {
                dialog.dismiss();
            }).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{WRITE_EXTERNAL_STORAGE}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LogToFile.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_LOG: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fileLogger.setPermission(true);
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                }
            }
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        fileLogger.log("onStart() started");
    }

    @Override
    protected void onResume() {
        super.onResume();
        fileLogger.log("onResume() started");
    }

    @Override
    protected void onPause() {
        super.onPause();
        fileLogger.log("onPause() started");
    }

    @Override
    protected void onStop() {
        super.onStop();
        fileLogger.log("onStop() started");
    }

    @Override
    protected void onDestroy() {
        fileLogger.log("onDestroy() started");
        super.onDestroy();
    }

    @Override
    public void ascPermission(int requestCode) {
        requestPermissions(requestCode);
    }
}