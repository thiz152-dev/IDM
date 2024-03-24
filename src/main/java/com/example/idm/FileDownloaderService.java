package com.example.idm;

import com.example.idm.fileService.FileDetails;
import com.example.idm.fileService.FileNameExtractor;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileDownloaderService {

    public static int downloadFile(final URL url) throws IOException {
        if (url == null) return DownloadError.URL_EMPTY.getCode();
        String fileName = url.getFile();
        FileDetails fileDetails = FileNameExtractor.extractFileName(fileName);
        String name = fileDetails.getName();
        String extension = fileDetails.getExtension();
        String userDownloads = System.getProperty("user.home") + File.separator + "Downloads";
        Path filePath = Paths.get(userDownloads, name + "." + extension);
        String uniqueFileName = getUniqueFileName(filePath);
        return saveFileByUrl(Paths.get(userDownloads, uniqueFileName), url, 10, 20);
    }

    public static int downloadFile(final URL url, AtomicBoolean isPaused) throws IOException {
        if (url == null) return DownloadError.URL_EMPTY.getCode();
        String fileName = url.getFile();
        FileDetails fileDetails = FileNameExtractor.extractFileName(fileName);
        String name = fileDetails.getName();
        String extension = fileDetails.getExtension();
        String userDownloads = System.getProperty("user.home") + File.separator + "Downloads";
        Path filePath = Paths.get(userDownloads, name + "." + extension);
        String uniqueFileName = getUniqueFileName(filePath);
        return saveFileByUrl(Paths.get(userDownloads, uniqueFileName), url, 10, 20, isPaused);
    }

    public static int saveFileByUrl(final Path file, final URL url, int secsConnectTimeout, int secsReadTimeout) throws IOException {
        Files.createDirectories(file.getParent()); // make sure parent dir exists , this can throw exception
        URLConnection conn = url.openConnection(); // can throw exception if bad url
        if (secsConnectTimeout > 0) conn.setConnectTimeout(secsConnectTimeout * 1000);
        if (secsReadTimeout > 0) conn.setReadTimeout(secsReadTimeout * 1000);
        int ret = 0;
        boolean somethingRead = false;

        try (InputStream is = conn.getInputStream()) {
            try (BufferedInputStream in = new BufferedInputStream(is); OutputStream fout = Files.newOutputStream(file)) {
                final byte[] data = new byte[8192];
                int count;
                while ((count = in.read(data)) > 0) {
                    somethingRead = true;
                    fout.write(data, 0, count);
                }
            }
        } catch (java.io.IOException e) {
            int httpcode = 999;
            try {
                httpcode = ((HttpURLConnection) conn).getResponseCode();
            } catch (Exception ignored) {
            }
            if (somethingRead && e instanceof java.net.SocketTimeoutException) ret = 1;
            else if (e instanceof FileNotFoundException && httpcode >= 400 && httpcode < 500) ret = 2;
            else if (httpcode >= 400 && httpcode < 600) ret = 3;
            else if (e instanceof java.net.SocketTimeoutException) ret = 4;
            else if (e instanceof java.net.ConnectException) ret = 5;
            else if (e instanceof java.net.UnknownHostException) ret = 6;
            else ret = -1;
        }
        return ret;
    }

    public static int saveFileByUrl(final Path file, final URL url, int secsConnectTimeout, int secsReadTimeout, AtomicBoolean isPaused) throws IOException {
        Files.createDirectories(file.getParent()); // make sure parent dir exists, this can throw an exception
        URLConnection conn = url.openConnection(); // can throw an exception if bad URL
        if (secsConnectTimeout > 0) conn.setConnectTimeout(secsConnectTimeout * 1000);
        if (secsReadTimeout > 0) conn.setReadTimeout(secsReadTimeout * 1000);
        int ret = 0;
        boolean somethingRead = false;

        try (InputStream is = conn.getInputStream()) {
            try (BufferedInputStream in = new BufferedInputStream(is); OutputStream fout = Files.newOutputStream(file)) {
                final byte[] data = new byte[8192];
                int count;
                while ((count = in.read(data)) > 0) {
                    somethingRead = true;
                    synchronized (isPaused) {
                        while (isPaused.get()) {
                            try {
                                isPaused.wait(); // Wait until the download is resumed
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    fout.write(data, 0, count);
                }
            }
        } catch (java.io.IOException e) {
            int httpcode = 999;
            try {
                httpcode = ((HttpURLConnection) conn).getResponseCode();
            } catch (Exception ignored) {
            }
            if (somethingRead && e instanceof java.net.SocketTimeoutException) ret = 1;
            else if (e instanceof FileNotFoundException && httpcode >= 400 && httpcode < 500) ret = 2;
            else if (httpcode >= 400 && httpcode < 600) ret = 3;
            else if (e instanceof java.net.SocketTimeoutException) ret = 4;
            else if (e instanceof java.net.ConnectException) ret = 5;
            else if (e instanceof java.net.UnknownHostException) ret = 6;
            else ret = -1;
        }
        return ret;
    }

    public static String getUniqueFileName (Path filePath){
        String name = filePath.getFileName().toString();
        String baseName = name.substring(0, name.lastIndexOf('.'));
        String extension = name.substring(name.lastIndexOf('.') + 1);
        int count = 0;
        while (Files.exists(filePath)) {
            count++;
            filePath = Paths.get(filePath.getParent().toString(), baseName + "-" + count + "." + extension);
        }
        return filePath.getFileName().toString();
    }

    public static long getFileSize(URL fileUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
        connection.setRequestMethod("HEAD");
        connection.connect();
        long fileSize = connection.getContentLengthLong();
        connection.disconnect();
        return fileSize;
    }
 
    public static String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static long getDownloadedSize(String fileName, String extension) {
        String downloadPath = System.getProperty("user.home") + File.separator + "Downloads";

        Path filePath = Paths.get(downloadPath, fileName + "." + extension);
        File file = filePath.toFile();

        if (file.exists() && file.isFile()) {
            return file.length(); // Return the size of the file in bytes
        } else {
            return -1; // Indicate that the file doesn't exist or isn't a file
        }

    }

    public static void deleteFiles(List<FileDetails> filesToDelete) {
        for (FileDetails fileDetails : filesToDelete) {
            String fileName = fileDetails.getName();
            String extension = fileDetails.getExtension();
            String downloadPath = System.getProperty("user.home") + File.separator + "Downloads";
            Path filePath = Paths.get(downloadPath, fileName + "." + extension);
            File file = filePath.toFile();

            if (file.exists() && file.isFile()) {
                boolean deleted = file.delete();
                if (!deleted) {
                System.err.println("Failed to delete file: " + file.getAbsolutePath());
                }
            } else {
                System.err.println("File not found: " + file.getAbsolutePath());
            }
        }
    }




  
       
    
        public static CompletableFuture<Void> downloadFileWithInterrupt(final URL url) {
            CompletableFuture<Void> downloadFuture = CompletableFuture.runAsync(() -> {
                try {
                    int result = downloadFile(url); // Your existing download logic
                    if (result != 0) {
                        throw new IOException("Download failed");
                    }
                } catch (IOException e) {
                    // Handle download failure
                    e.printStackTrace();
                }
            });
    
            return downloadFuture;
        }
    
        public static CompletableFuture<Void> interruptDownload(CompletableFuture<Void> downloadFuture) {
            downloadFuture.cancel(true);
            return downloadFuture;
        }
  
    










        











}
