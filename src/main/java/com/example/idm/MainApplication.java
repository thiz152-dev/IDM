package com.example.idm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view-v1.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setResizable(false);
        stage.setTitle("IDM");

        try {
            Image icon = new Image(Objects.requireNonNull(MainApplication.class.getResourceAsStream("/com/example/idm/icons/png/app-icon.png")));
            stage.getIcons().add(icon);
        } catch (NullPointerException e) {
            System.err.println("Error: Unable to load the application icon. The icon file may be missing or the path is incorrect: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("The provided path is invalid or incorrectly formatted: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("An unexpected runtime error occurred: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unspecified exception occurred: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}