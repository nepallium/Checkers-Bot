package UI.auth;

import com.opencsv.CSVWriter;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.mindrot.jbcrypt.BCrypt;

import java.io.FileWriter;

public class SignupController {
    @FXML
    TextField emailField;
    @FXML
    PasswordField passwordField;

    @FXML
    public void handleSignup() {

    }
}
