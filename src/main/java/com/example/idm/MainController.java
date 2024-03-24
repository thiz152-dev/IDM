package com.example.idm;

import com.example.idm.fileService.FileDetails;
import com.example.idm.fileService.FileNameExtractor;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainController {

    private final ObservableList<FileDetails> fileList = FXCollections.observableArrayList();
    @FXML
    private ImageView pauseButton;

     @FXML
    private ImageView resumeButton;
     @FXML
    private ImageView deleteButton;
     @FXML
    private ImageView pauseAllButton;
      @FXML
    private ImageView resumeAllButton;
      @FXML
    private ImageView deleteAllButton;
    


    @FXML
    private TextField urlField;

    @FXML
    private Menu editMenu;

   

    @FXML
    private TableView<FileDetails> tableView;

    @FXML
    private TableColumn<FileDetails, String> nameColumn;

    @FXML
    private TableColumn<FileDetails, String> urlColumn;

    @FXML
    private TableColumn<FileDetails, String> sizeColumn;

    @FXML
    private TableColumn<FileDetails, String> stateColumn;

    @FXML
    private TableColumn<FileDetails, String> progressColumn;

   

    @FXML
    private void addUrlClick() throws IOException {
        URL url;
        try {
            url = new URL(urlField.getText());
        } catch (MalformedURLException e) {
            AlertMessage.showErrorAlert(DownloadError.URL_INVALID.getMessage());
            System.out.println(DownloadError.URL_INVALID.getMessage());
            return;
        }

        // Check if the URL already exists in the table view
        if (isUrlAlreadyExists(url)) {
            AlertMessage.showErrorAlert("URL is already in the list.");
            return;
        }

        FileDetails fileValidationTest = new FileDetails(FileNameExtractor.extractFileName(url).getName(),
                FileNameExtractor.extractFileName(url).getExtension());
        if (fileValidationTest.getName() == null || fileValidationTest.getExtension() == null) {
            AlertMessage.showErrorAlert(DownloadError.NOT_FOUND.getMessage());
            System.out.println(DownloadError.NOT_FOUND.getMessage());
            return;
        }

        FileDetails newFile = new FileDetails(
                FileNameExtractor.extractFileName(url).getName(),
                FileNameExtractor.extractFileName(url).getExtension(),
                url,
                FileDownloaderService.getFileSize(url), // Initial state
                "Waiting", // Initial state
                0 // Initial progress
        );
        tableView.getItems().add(newFile);
    }

    // Helper method to check if the URL already exists in the table view
    private boolean isUrlAlreadyExists(URL url) {
        for (FileDetails file : tableView.getItems()) {
            if (file.getUrl().equals(url)) {
                return true;
            }
        }
        return false;
    }




    @FXML
    private void downloadAllClick() {
        System.out.println("Pressed");

        if (tableView.getItems().isEmpty()) {
            AlertMessage.showNoUrlsError();
            return;
        }

        for (FileDetails file : tableView.getItems()) {
            file.setState("Processing");
        }

        List<FileDetails> filesToDownload = new ArrayList<>(tableView.getItems());
        List<CompletableFuture<Integer>> downloadFutures = new ArrayList<>();

        // Iterate through each file and create a CompletableFuture for its download
        for (FileDetails file : filesToDownload) {
            file.setState("Processing");
            CompletableFuture<Integer> downloadFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return downloadFileInBackground(file.getUrl(), file).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
            downloadFutures.add(downloadFuture);
        }

        // Wait for all downloads to complete
        CompletableFuture<Void> allDownloads = CompletableFuture.allOf(
                downloadFutures.toArray(new CompletableFuture[0])
        );
        
        allDownloads.thenRun(() -> {
            // Check if any download failed
            boolean anyFailed = downloadFutures.stream().anyMatch(future -> {
                try {
                    int errorCode = future.get();
                    return errorCode != DownloadError.SUCCESS.getCode();
                } catch (InterruptedException | ExecutionException e) {
                    // Log the error for better understanding of the failure
                    e.printStackTrace(); // Log using a logging framework for better control
                    return true; // Consider as failed if exception occurs
                }
            });

            // Show appropriate alert message based on download status
            Platform.runLater(() -> {
                if (anyFailed) {
                    for (FileDetails file : tableView.getItems()) {
                        if (file.getState().equals("Processing")) {
                            file.setState("Failed");
                        }
                    }
                    StringBuilder errorMessage = new StringBuilder("Some downloads failed with errors:\n");
                    downloadFutures.forEach(future -> {
                        try {
                            int errorCode = future.get();
                            DownloadError error = DownloadError.getByCode(errorCode);
                            errorMessage.append(error.getMessage()).append("\n");
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace(); // Log the exception
                            errorMessage.append("Error retrieving download status").append("\n");
                        }
                    });
                    AlertMessage.showErrorAlert(errorMessage.toString());
                } else {
                    AlertMessage.showSuccessfulDownloadAlert();
                }
            });
        });
    }













       @FXML
    private void pauseSelectedRows() {
        ObservableList<FileDetails> selectedRows = tableView.getSelectionModel().getSelectedItems();
        for (FileDetails file : selectedRows) {
            // Call a method in FileDownloaderService to pause this specific download
            pauseDownload(file);
        }
    }

    @FXML
    private void resumeSelectedRows() {
        ObservableList<FileDetails> selectedRows = tableView.getSelectionModel().getSelectedItems();
        for (FileDetails file : selectedRows) {
            resumeDownload(file);
        }
    }

    @FXML
    private void deleteSelectedRows() {
        ObservableList<FileDetails> selectedRows = tableView.getSelectionModel().getSelectedItems();
        List<FileDetails> filesToDelete = new ArrayList<>(selectedRows);
          // Interrupt downloads of selected files before deleting
             interruptDownloads(filesToDelete);

        FileDownloaderService.deleteFiles(filesToDelete);
        tableView.getItems().removeAll(selectedRows);
    }

    @FXML
    private void pauseAllRows() {
        for (FileDetails file : tableView.getItems()) {
            pauseDownload(file);
        }
    }

    @FXML
    private void resumeAllRows() {
        for (FileDetails file : tableView.getItems()) {
            resumeDownload(file);
        }
    }

    @FXML
    private void deleteAllRows() {
        ObservableList<FileDetails> allRows = tableView.getItems();
        List<FileDetails> filesToDelete = new ArrayList<>(allRows);
           // Interrupt downloads of all files before deleting
             interruptDownloads(filesToDelete);
        FileDownloaderService.deleteFiles(filesToDelete);
        allRows.clear();
    }


    private void interruptDownloads(List<FileDetails> files) {
        for (FileDetails file : files) {
            CompletableFuture<Void> downloadFuture = file.getDownloadFuture(); // Assuming FileDetails has a field for downloadFuture
            if (downloadFuture != null && !downloadFuture.isDone() ) {
                FileDownloaderService.interruptDownload(downloadFuture);
            }
        }
    }

    @FXML
    private void initialize() {
        // Set up columns
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName() + "." + param.getValue().getExtension()));
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        sizeColumn.setCellValueFactory(param -> new SimpleStringProperty(FileDownloaderService.formatFileSize(param.getValue().getSize())));
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));
        progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));

        tableView.setItems(fileList);
        tableView.setFocusTraversable(false);
      // Set the selection model for the TableView to enable selection of multiple rows
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        pauseButton.setOnMouseClicked(event -> pauseSelectedRows());
        resumeButton.setOnMouseClicked(event -> resumeSelectedRows());
        deleteButton.setOnMouseClicked(event -> deleteSelectedRows());
        pauseAllButton.setOnMouseClicked(event -> pauseAllRows());
        resumeAllButton.setOnMouseClicked(event -> resumeAllRows());
        deleteAllButton.setOnMouseClicked(event -> deleteAllRows());
    
    }

    @FXML
    private void deleteSelectedRow(ActionEvent event) {
        ObservableList<FileDetails> selectedFiles = tableView.getSelectionModel().getSelectedItems();
        for (FileDetails file : selectedFiles) {
            // Remove the selected file from the table
            tableView.getItems().remove(file);
        }
    }

    @FXML
    private void deleteSelectedRow() {
        ObservableList<FileDetails> selectedFiles = tableView.getSelectionModel().getSelectedItems();
        for (FileDetails file : selectedFiles) {
            // Remove the selected file from the table
            tableView.getItems().remove(file);
        }
    }

    public void updateProgress(FileDetails fileDetails) {
        // Update the TableView with the updated file details
        int index = tableView.getItems().indexOf(fileDetails);
        if (index >= 0) {
            tableView.getItems().set(index, fileDetails);
        }
    }

    Boolean waitToResume = false;
    public CompletableFuture<Integer> downloadFileInBackground(URL url, FileDetails fileDetails) {
        ExecutorService downloadExecutor = Executors.newFixedThreadPool(10); // Adjust the thread count as needed
        ScheduledExecutorService progressExecutor = Executors.newSingleThreadScheduledExecutor();

        CompletableFuture<Integer> result = new CompletableFuture<>();
        AtomicBoolean isPaused = new AtomicBoolean(false);

        try {
            long fileSize = FileDownloaderService.getFileSize(url);
            fileDetails.setSize(fileSize);

            downloadExecutor.execute(() -> {
                try {
                    int downloadResult = FileDownloaderService.downloadFile(url, isPaused);
                    result.complete(downloadResult);
                } catch (IOException e) {
                    result.completeExceptionally(e);
                }
            });

            progressExecutor.scheduleWithFixedDelay(() -> {
                long downloadedSize = FileDownloaderService.getDownloadedSize(fileDetails.getName(), fileDetails.getExtension());
                double progress = (double) downloadedSize / fileSize * 100.0;

                if (fileDetails.getState().compareTo("Paused") == 0) {
                    isPaused.set(true);
                } else if (fileDetails.getState().compareTo("Processing") == 0) {
                    synchronized (isPaused) {
                        isPaused.set(false);
                        isPaused.notifyAll(); // Notify other threads that the download can be resumed
                    }
                }

                if (progress >= 100.0) {
                    progress = 100.0;
                    fileDetails.setState("Completed");
                    downloadExecutor.shutdown(); // Stop downloading after completion
                    progressExecutor.shutdown(); // Stop updating progress after completion
                }

                fileDetails.setProgress((int) progress);
                Platform.runLater(() -> updateProgress(fileDetails)); // Update the progress in the TableView
            }, 0, 50, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            e.printStackTrace();
            fileDetails.setState("Failed");
            updateProgress(fileDetails);
            result.complete(-1); // Complete the future with a failure code
        }

        return result;
    }
  
  
  
  
  
  
  
    public void pauseDownload(FileDetails file) {
        file.setState("Paused");
    }

    public void resumeDownload(FileDetails file) {
        file.setState("Processing");
    }












   
    































    
}
