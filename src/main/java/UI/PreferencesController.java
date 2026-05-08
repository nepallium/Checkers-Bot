package UI;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class PreferencesController {

    @FXML private RadioButton pieceDogRadio;
    @FXML private RadioButton pieceDefaultRadio;
    @FXML private ToggleGroup pieceToggleGroup;

    @FXML private RadioButton boardBlueRadio;
    @FXML private RadioButton boardBrownRadio;
    @FXML private ToggleGroup boardToggleGroup;

    @FXML private RadioButton targetCircleRadio;
    @FXML private RadioButton targetXRadio;
    @FXML private ToggleGroup targetToggleGroup;

    private AppPreferences prefs;
    private Runnable onApply;

    @FXML
    private void initialize() {
    }

    /**
     * Must be called right after FXMLLoader.load(), before showAndWait().
     * Injects the shared prefs object and pre-populates the radio buttons.
     */
    public void setPreferences(AppPreferences prefs) {
        this.prefs = prefs;

        pieceDogRadio.setSelected(    prefs.getPieceStyle() == AppPreferences.PieceStyle.DOG);
        pieceDefaultRadio.setSelected(prefs.getPieceStyle() == AppPreferences.PieceStyle.DEFAULT);

        boardBlueRadio.setSelected(  prefs.getBoardTheme() == AppPreferences.BoardTheme.BLUE_WHITE);
        boardBrownRadio.setSelected( prefs.getBoardTheme() == AppPreferences.BoardTheme.BROWN_WHITE);

        targetCircleRadio.setSelected(prefs.getTargetIcon() == AppPreferences.TargetIcon.CIRCLE);
        targetXRadio.setSelected(     prefs.getTargetIcon() == AppPreferences.TargetIcon.X);
    }

    public void setOnApply(Runnable onApply) {
        this.onApply = onApply;
    }

    @FXML
    private void onApplyClicked() {
        prefs.setPieceStyle(pieceDogRadio.isSelected()
                ? AppPreferences.PieceStyle.DOG
                : AppPreferences.PieceStyle.DEFAULT);

        prefs.setBoardTheme(boardBlueRadio.isSelected()
                ? AppPreferences.BoardTheme.BLUE_WHITE
                : AppPreferences.BoardTheme.BROWN_WHITE);

        prefs.setTargetIcon(targetCircleRadio.isSelected()
                ? AppPreferences.TargetIcon.CIRCLE
                : AppPreferences.TargetIcon.X);

        if (onApply != null) onApply.run();
        closeDialog();
    }

    @FXML
    private void onCancelClicked() {
        closeDialog();
    }

    private void closeDialog() {
        ((Stage) pieceDogRadio.getScene().getWindow()).close();
    }
}