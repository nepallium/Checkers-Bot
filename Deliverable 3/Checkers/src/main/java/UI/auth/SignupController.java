package UI.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import main.App;

public class SignupController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private VBox errorsVbox;

    @FXML
    public void handleSignup() {
        clearErrors();

        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            addError("Email and password are required.");
            return;
        }

        if (DatabaseManager.isEmailAvailable(email)) {
            DatabaseManager.saveUser(email, password);
            App.setScene("startup");
        } else {
            addError("Email is already associated with an account.");
        }
    }

    @FXML
    public void switchToLogin() {
        App.setScene("login");
    }

    private void clearErrors() {
        if (errorsVbox != null) {
            errorsVbox.getChildren().clear();
            errorsVbox.setVisible(false);
            errorsVbox.setManaged(false);
        }
    }

    private void addError(String message) {
        if (errorsVbox == null) {
            return;
        }
        errorsVbox.setVisible(true);
        errorsVbox.setManaged(true);
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("error-text");
        errorsVbox.getChildren().add(errorLabel);
    }
}
