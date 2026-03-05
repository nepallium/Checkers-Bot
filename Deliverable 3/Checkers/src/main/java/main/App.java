package main;

import game.Board;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App {
    public static Stage stage;
    private static final String FXML_SCENES_FILE_PATH_PREFIX = "/View/";
    private static final String IMAGES_FILE_PATH_PREFIX = "/Images/";
    private static final String WHITE_MAN_IMAGE_PATH = "Samurai_Cheems_Cropped_Light_Theme.png";
    private static final String WHITE_KING_IMAGE_PATH = "King_Cheems_Cropped_Light_Theme.png";
    private static final String BLACK_MAN_IMAGE_PATH = "Samurai_Cheems_Cropped_Dark_Theme.png";
    private static final String BLACK_KING_IMAGE_PATH = "King_Cheems_Cropped_Dark_Theme.png";
    public static final String BLACK_GRID_COLOR = "#101930";
    public static final String WHITE_GRID_COLOR = "#86a0e3";

    public static String getPieceImagePath(int piece) {
        if (Board.isPieceInvalid(piece)) {
            return "";
        }
        return App.class.getResource(IMAGES_FILE_PATH_PREFIX + new String[] {BLACK_KING_IMAGE_PATH, BLACK_MAN_IMAGE_PATH, "", WHITE_MAN_IMAGE_PATH, WHITE_KING_IMAGE_PATH,}[piece + 2]).toExternalForm();
    }

    /**
     * Sets the scene to that of the fxml and returns the success status
     * @param fxmlName name of the fxml file
     * @return if it successfully set the scene
     */
    public static boolean setScene(String fxmlName) {
        try {
            Parent parent = loadFXML(FXML_SCENES_FILE_PATH_PREFIX + fxmlName);
            if (parent == null) {
                return false;
            }
            stage.setScene(new Scene(parent));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads a fxml file
     * @param fxmlName fxml to load
     * @return the parent of the fxml
     * @throws IOException if there was an error in getting it
     */
    private static Parent loadFXML(String fxmlName) throws IOException {
        URL fxmlLocation = App.class.getResource(fxmlName + ".fxml");
        if (fxmlLocation == null) {
            System.out.printf("Invalid fxml: %s.fxml\n", fxmlName);
            return null;
        }
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(fxmlLocation);
        return fxmlLoader.load();
    }
}
