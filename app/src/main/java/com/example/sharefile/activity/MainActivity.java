package com.example.sharefile.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.sharefile.R;
import com.example.sharefile.util.FileUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_LOG = 0;
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO = 3;
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    private FloatingActionButton optionsButton;
    private FloatingActionButton shareLogButton;
    private FloatingActionButton makePhotoButton;
    private FloatingActionButton chosePhotoButton;
    private final int GALLERY_REQUEST = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;
    private boolean clicked;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkStoragePermission(PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_LOG)) {
            FileUtil.log("onCreate() started", this);
        }
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.floatingActionButton_options):
                onOptionsButtonClicked();
                break;
            case (R.id.floatingActionButton_share):
                FileUtil.shareLogFile(this);
                break;
            case (R.id.floatingActionButton_chosePhoto):
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                photoPickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                break;
            case (R.id.floatingActionButton_makePhoto):
                if (checkStoragePermission(PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO)) {
                    dispatchTakePictureIntent();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ImageView imageView = findViewById(R.id.imageView);
            switch (requestCode) {
                case GALLERY_REQUEST:
                    if (data != null) {
                        Uri selectedImageUri = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImageUri != null) {
                            Cursor cursor = getContentResolver().query(selectedImageUri,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String imagePath = cursor.getString(columnIndex);
                                imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                                cursor.close();
                            }
                        }
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    if (FileUtil.getCurrentPhotoPath() != null) {
                        File file = new File(FileUtil.getCurrentPhotoPath());
                        Uri contentUri = Uri.fromFile(file);
                        Glide.with(this).load(contentUri).into(imageView);
                    }
                    break;
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = FileUtil.createImageFile(this);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        FileUtil.FILES_AUTHORITY,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_LOG: {
                if (grantResults.length > 0
                        && grantResults[0] == PERMISSION_GRANTED) {
                    //??????????????
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                }
            }
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO: {
                if (grantResults.length > 0
                        && grantResults[0] == PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean checkStoragePermission(int requestCode) {
        if (ContextCompat.checkSelfPermission(
                this, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            return true;
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.alertDialog_checkPerm_title)
                    .setMessage(R.string.alertDialog_checkPerm_desc)
                    .setPositiveButton(R.string.alertDialog_checkPerm_posButton, (dialogInterface, i) -> {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{WRITE_EXTERNAL_STORAGE}, requestCode
                        );
                        dialogInterface.dismiss();
                    }).setNegativeButton(R.string.alertDialog_checkPerm_negButton, (dialog, which) -> {
                dialog.dismiss();
            }).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{WRITE_EXTERNAL_STORAGE}, requestCode);
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkStoragePermission(PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_LOG)) {
            FileUtil.log("onStart() started", this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkStoragePermission(PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_LOG)) {
            FileUtil.log("onResume() started", this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (checkStoragePermission(PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_LOG)) {
            FileUtil.log("onPause() started", this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (checkStoragePermission(PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_LOG)) {
            FileUtil.log("onStop() started", this);
        }
    }

    @Override
    protected void onDestroy() {
        if (checkStoragePermission(PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_LOG)) {
            FileUtil.log("onDestroy() started", this);
        }
        super.onDestroy();
    }

}