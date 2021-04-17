package com.app.sms;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper implements Serializable {


    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    private Drive mDriveService;

    public DriveServiceHelper(Drive mDriveService) {
        this.mDriveService = mDriveService;
    }

    public Task<String> createFilePDF(String filePath){

        return Tasks.call(mExecutor,()->{

            Drive.Files.List request = mDriveService.files().list().setQ(
                    "mimeType='application/vnd.google-apps.folder' and trashed=false and name='AllSMS' and 'root' in parents");
            FileList files = request.execute();
            String folderId=null;

            if(files.getFiles().size()==0) {
                File fileMetadata = new File();
                fileMetadata.setName("AllSMS");
                fileMetadata.setMimeType("application/vnd.google-apps.folder");

                File folder = mDriveService.files().create(fileMetadata)
                        .setFields("id")
                        .execute();
                System.out.println("Folder ID: " + folder.getId());
                folderId = folder.getId();
            }else{
                folderId = files.getFiles().get(0).getId();
            }

            File fileMetaData = new File();
            fileMetaData.setName(Calendar.getInstance().getTime()+"_SMS");
            fileMetaData.setParents(Collections.singletonList(folderId));
            java.io.File file = new java.io.File(filePath);

            FileContent mediaContent = new FileContent("text/plain",file);

            File myFile = null;
            try {

                myFile = mDriveService.files().create(fileMetaData,mediaContent).execute();
            }catch (Exception e){
                    e.printStackTrace();
            }

            if(myFile==null){
                throw new IOException("Null in file creation");
            }
            return myFile.getId();
        });

    }
}
