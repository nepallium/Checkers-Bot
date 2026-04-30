package UI.auth;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DatabaseManager {
    // creates a path to a folder named "data" in the same directory as app (where pom.xml is)
    private static final String DATA_DIR = System.getProperty("user.dir") + File.separator + "data";
    private static final String CSV_PATH = DATA_DIR + File.separator + "users.csv";

    public static void initialize() {
        try {
            // create the 'data' folder if it doesn't exist
            Path path = Paths.get(DATA_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            // create the 'users.csv' if it doesn't exist
            File csvFile = new File(CSV_PATH);
            if (!csvFile.exists()) {
                csvFile.createNewFile();
                Files.writeString(Paths.get(CSV_PATH), "email,password_hash\n");
            }
        } catch (IOException e) {
            System.err.println("Could not initialize database: " + e.getMessage());
        }
    }
}