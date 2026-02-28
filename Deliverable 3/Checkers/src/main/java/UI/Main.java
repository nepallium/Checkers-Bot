package UI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Checkers Bot");

//        try {
//            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/View/startup.fxml")));
//
//            Scene scene = new Scene(root);
//
//            stage.setScene(scene);
//        } catch (IOException err) {
//            System.out.println("Error loading fxml file, " + err);
//        }

        stage.show();
    }
}
