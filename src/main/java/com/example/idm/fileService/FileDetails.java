package com.example.idm.fileService;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class FileDetails{
    private String name;
    private String extension;
    private URL url;
    private long size;
    private String state;
    private int progress;

        private CompletableFuture<Void> downloadFuture;


    public FileDetails(String name, String extension, URL url, long size, String state, int progress) {
        this.name = name;
        this.extension = extension;
        this.url = url;
        this.size = size;
        this.state = state;
        this.progress = progress;
    }

    public FileDetails(String name, String extension){
        this.name = name;
        this.extension = extension;
    }

    public FileDetails(String name, String extension, URL url){
        this.name = name;
        this.extension = extension;
        this.url = url;
    }

    public FileDetails(String name, String extension, URL url, String state, int currentProgress) {
        this.name = name;
        this.extension = extension;
        this.state = state;
        this.progress = currentProgress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getExtension() {
        return extension;
    }
 
    public void setExtension(String extension){this.extension = extension;}

    public CompletableFuture<Void> getDownloadFuture() {
        return downloadFuture;
    }

    public void setDownloadFuture(CompletableFuture<Void> downloadFuture) {
        this.downloadFuture = downloadFuture;
    }


}

