package UI;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import main.App;

public class StartupController {

    public ToggleButton easyButton;
    public ToggleButton mediumButton;
    public ToggleButton hardButton;

    private final String defaultStyle =
            " -fx-background-color:  101930;" +
                    " -fx-text-fill:  #FFFFFF;" +
                    " -fx-border-radius:  10;" +
                    " -fx-font-size:  16;";

    public void onEasyButtonAction(ActionEvent actionEvent) {
        App.setMctsSimulations(100);

        easyButton.setStyle(defaultStyle + "-fx-border-color: #FF0000;");
        mediumButton.setStyle(defaultStyle+ "-fx-border-color: #FFFFFF;");
        hardButton.setStyle(defaultStyle + "-fx-border-color: #FFFFFF;");

    }

    public void onMediumButtonAction(ActionEvent actionEvent) {
        App.setMctsSimulations(500);

        easyButton.setStyle(defaultStyle + "-fx-border-color: #FFFFFF;");
        mediumButton.setStyle(defaultStyle + "-fx-border-color: #FF0000;");
        hardButton.setStyle(defaultStyle + "-fx-border-color: #FFFFFF;");
    }

    public void onHardButtonAction(ActionEvent actionEvent) {
        App.setMctsSimulations(5000);

        easyButton.setStyle(defaultStyle + "-fx-border-color: #FFFFFF;");
        mediumButton.setStyle(defaultStyle + "-fx-border-color: #FFFFFF;");
        hardButton.setStyle(defaultStyle + "-fx-border-color: #FF0000;");
    }


    public void onPlayButtonAction(ActionEvent actionEvent) {
        App.setScene("mainGame");
    }

}


