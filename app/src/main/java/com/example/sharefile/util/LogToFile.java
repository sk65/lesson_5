package com.example.sharefile.util;

import android.content.Context;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogToFile {
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_LOG = 0;
    private final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    private File logFile;
    private Context context;
    private boolean permission;

    public LogToFile(Context context) {
        this.context = context;
        logFile = new File(context.getFilesDir(), "com.example.sharefile.logs.txt");
    }

    public File getLogFile() {
        return logFile;
    }

    public void log(String log) {
        if (permission) {
            writeToFile(log);
        } else {
            PermissionGet permissionGet = (PermissionGet) context;
            permissionGet.ascPermission(PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_LOG);
        }
    }

    private void writeToFile(String text) {
        String timeStamp = new SimpleDateFormat(DATE_FORMAT).format(Calendar.getInstance().getTime());
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(logFile, true))) {
            printWriter.append(timeStamp).append("_").append(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPermission(boolean permission) {
        this.permission = permission;
    }
}
