package com.example.sharefile.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;


import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FileUtil {

    private static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    private static File logFile;
    private static String currentPhotoPath;
    public static final String FILES_AUTHORITY = "com.example.sharefile.provider";



    public static void log(String log, Context context) {
        if (logFile == null) {
            logFile = new File(context.getFilesDir(), "com.example.sharefile.logs.txt");
        }
        String timeStamp = new SimpleDateFormat(DATE_FORMAT).format(Calendar.getInstance().getTime());
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(logFile, true))) {
            printWriter.append(timeStamp).append("_").append(log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static void shareLogFile(Context context) {
        Activity activity = (Activity) context;
        Uri uriToFile = FileProvider.getUriForFile(context, FILES_AUTHORITY, logFile);
        Intent shareIntent = ShareCompat.IntentBuilder.from(activity)
                .setType(context.getContentResolver().getType(uriToFile))
                .setStream(uriToFile)
                .getIntent();
        shareIntent.setData(uriToFile);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(shareIntent);
    }

    public static String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

}
