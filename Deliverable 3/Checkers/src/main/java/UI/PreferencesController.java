package UI;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import main.App;

public class PreferencesController {

    @FXML private ComboBox<String> backgroundThemeCombo;
    @FXML private ComboBox<String> pieceThemeCombo;
    @FXML private Spinner<Integer> undosSpinner;

    @FXML
    public void initialize() {
        backgroundThemeCombo.getItems().addAll("Dark Navy", "Forest Green", "Classic Wood", "Midnight Black");
        backgroundThemeCombo.setValue("Dark Navy");

        pieceThemeCombo.getItems().addAll("Classic", "Modern", "Minimal", "Pixel");
        pieceThemeCombo.setValue("Classic");

        undosSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
    }

    @FXML
    private void handleSave() {
        String bgTheme = backgroundThemeCombo.getValue();
        String pieceTheme = pieceThemeCombo.getValue();
        int undos = undosSpinner.getValue();

        // TODO: Apply and persist settings
    }

    @FXML
    private void handleCancel() {
        App.setScene("main.fxml"); // adjust to your main scene name
    }
}