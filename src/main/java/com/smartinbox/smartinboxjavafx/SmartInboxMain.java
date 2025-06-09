package com.smartinbox.smartinboxjavafx;

import com.smartinbox.smartinboxjavafx.email.EmailService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

import static com.smartinbox.smartinboxjavafx.email.EmailService.credentialsFileExists;

public class SmartInboxMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        if (!credentialsFileExists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Missing credentials.json");
            alert.setHeaderText("Credentials file not found");
            alert.setContentText("Please place your credentials.json file in the same folder as this application.");
            alert.showAndWait();
            Platform.exit();
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(SmartInboxMain.class.getResource("smartInbox-view.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 1100, 810);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon8.png"))));
        stage.setTitle("Smart Inbox");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}