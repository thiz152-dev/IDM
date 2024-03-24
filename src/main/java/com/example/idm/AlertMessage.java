package com.example.idm;

import javafx.scene.control.Alert;

public class AlertMessage {
    public static void showErrorAlert(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    // Success alert method
    public static void showSuccessAlert(String successMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(successMessage);
        alert.showAndWait();
    }
    public static void showSuccessfulDownloadAlert() {
        showSuccessAlert("Download is successful");
    }
    public static void showNoUrlsError() {
        showErrorAlert("No URLs to download!");
    }



    
}
