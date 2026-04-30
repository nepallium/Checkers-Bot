package UI.auth;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    TextField emailField;
    @FXML
    PasswordField passwordField;

    /*
    String candidate = "password_from_login_form";
String storedHash = "hash_from_csv"; // Read this from the CSV row

// BCrypt extracts the salt from the hash itself and compares them
if (BCrypt.checkpw(candidate, storedHash)) {
    System.out.println("It matches!");
} else {
    System.out.println("Invalid password.");
}
     */
}
