package UI.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import main.App;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private VBox errorsVbox;

    @FXML
    public void handleLogin() {
        clearErrors();

        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            addError("Email and password are required.");
            return;
        }

        if (DatabaseManager.verifyUser(email, password)) {
            App.setScene("startup");
        } else {
            addError("Invalid email or password.");
        }
    }

    @FXML
    public void switchToSignup() {
        App.setScene("signup");
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
