package com.dakshin.notes;

import com.google.android.gms.drive.DriveFile;

import java.io.File;

public class DriveJobQueue {
    final static int JOB_TYPE_DOWNLOAD=0;
    final static int JOB_TYPE_UPLOAD=1;
    String name;
    DriveFile driveFile;
    String mimeType;
    File file;
    int jobType;
    DriveJobQueue(String mimeType, File file){
        this.file=file;
        this.mimeType=mimeType;
        jobType=JOB_TYPE_UPLOAD;
    }
    DriveJobQueue(String name, DriveFile driveFile) {
        this.name=name;
        this.driveFile=driveFile;
        jobType=JOB_TYPE_DOWNLOAD;
    }

    public String getName() {
        return name;
    }

    public DriveFile getDriveFile() {
        return driveFile;
    }

    public String getMimeType() {
        return mimeType;
    }

    public File getFile() {
        return file;
    }

    public int getJobType() {
        return jobType;
    }
}
