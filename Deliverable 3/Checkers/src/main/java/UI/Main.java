package UI;

import UI.auth.DatabaseManager;
import game.Move;
import javafx.application.Application;
import javafx.stage.Stage;
import main.App;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        DatabaseManager.initialize();

        App.stage = stage;
        stage.setTitle("Checkers Bot");

        App.setScene("signup");

        Move.init();

        stage.show();
    }
}
