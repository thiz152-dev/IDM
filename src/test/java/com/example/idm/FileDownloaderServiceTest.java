package com.example.idm;

import com.example.idm.fileService.FileDetails;
import com.example.idm.fileService.FileNameExtractor;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

class FileDownloaderServiceTest {

    @Test
    void saveFileByUrl() {
        try {
            // Set URL
            // Example : https://www.africau.edu/images/default/sample.pdf
            URL url = new URL("https://www.africau.edu/images/default/sample.pdf");
            // Get the file name from the URL and Split the file name to extract name and extension
            String fileName = url.getFile();
            FileDetails fileDetails = FileNameExtractor.extractFileName(fileName);
            String name = fileDetails.getName();
            String extension = fileDetails.getExtension();
            //Set Params
            String userDownloads = System.getProperty("user.home") + File.separator + "Downloads";
            Path filePath = Paths.get(userDownloads, name + "." + extension);
            // Check and modify the filename if it exists
            String uniqueFileName = FileDownloaderService.getUniqueFileName(filePath);
            // Get results
            int result = FileDownloaderService.saveFileByUrl(Paths.get(userDownloads, uniqueFileName), url, 10, 20);
            switch (result) {
                case 0:
                    System.out.println("Download successful!");
                    break;
                case 1:
                    System.out.println("Connection interrupted, timeout (but something was read)");
                    break;
                case 2:
                    System.out.println("Not found (FileNotFoundException) (404)");
                    break;
                case 3:
                    System.out.println("Server error (500...)");
                    break;
                case 4:
                    System.out.println("Could not connect: connection timeout (no internet?) java.net.SocketTimeoutException");
                    break;
                case 5:
                    System.out.println("Could not connect: (server down?) java.net.ConnectException");
                    break;
                case 6:
                    System.out.println("Could not resolve host (bad host, or no internet - no dns)");
                    break;
                default:
                    System.out.println("Unknown error occurred.");
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    void downloadFile() throws IOException {
        URL url = new URL("https://www.africau.edu/images/default/sample.pdf");
        FileDownloaderService.downloadFile(url);
    }
}