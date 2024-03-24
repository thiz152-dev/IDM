package com.example.idm.fileService;

import com.example.idm.FileDownloaderService;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileNameExtractor {

    public static FileDetails extractFileName(URL url) {
        String fileName = url.getFile();
        return extractFileName(fileName);
    }
    public static FileDetails extractFileName(String fileName) {
        int lastSlashIndex = fileName.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < fileName.length() - 1) {
            fileName = fileName.substring(lastSlashIndex + 1); // Extract the file name from the URL
        }

        String name = "";
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            name = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex + 1);
        }

        if (name.isEmpty()) {
            name = null;
        } else {
            String userDownloads = System.getProperty("user.home") + File.separator + "Downloads";
            Path filePath = Paths.get(userDownloads, name + "." + extension);
            String uniqueFileName = FileDownloaderService.getUniqueFileName(filePath);

            // Extract the name and extension from the unique file name
            int dotIndexUnique = uniqueFileName.lastIndexOf('.');
            if (dotIndexUnique >= 0 && dotIndexUnique < uniqueFileName.length() - 1) {
                name = uniqueFileName.substring(0, dotIndexUnique);
                extension = uniqueFileName.substring(dotIndexUnique + 1);
            }
        }
        return new FileDetails(name, extension);
    }

}