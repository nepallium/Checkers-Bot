package UI.auth;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.mindrot.jbcrypt.BCrypt;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class DatabaseManager {
    // Keep data next to the app or project root.
    private static final Path APP_DIR = resolveAppDir();
    private static final Path DATA_DIR = APP_DIR.resolve("data");
    private static final Path CSV_PATH = DATA_DIR.resolve("users.csv");

    private static Path resolveAppDir() {
        try {
            Path codeSource = Paths.get(DatabaseManager.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());

            if (Files.isDirectory(codeSource)) {
                Path current = codeSource;
                while (current != null && !Files.exists(current.resolve("pom.xml"))) {
                    current = current.getParent();
                }
                return current != null ? current : codeSource;
            }

            Path jarParent = codeSource.getParent();
            return jarParent != null ? jarParent : Paths.get(System.getProperty("user.dir"));
        } catch (Exception e) {
            return Paths.get(System.getProperty("user.dir"));
        }
    }

    public static void initialize() {
        try {
            // create the 'data' folder if it doesn't exist
            Path path = DATA_DIR;
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            // create the 'users.csv' if it doesn't exist
            File csvFile = CSV_PATH.toFile();
            if (!csvFile.exists()) {
                csvFile.createNewFile();
                Files.writeString(CSV_PATH, "email,password_hash\n");
            }
        } catch (IOException e) {
            System.err.println("Could not initialize database: " + e.getMessage());
        }
    }

    /**
     * Hashes plain password and adds to db.csv file
     * @param email the user's email
     * @param password the user's password, as is
     */
    public static void saveUser(String email, String password) {
        String hashedPw = BCrypt.hashpw(password, BCrypt.gensalt());
        try (CSVWriter writer = new CSVWriter(new FileWriter(CSV_PATH.toFile(), true))) {
            String[] user = {email, hashedPw};
            writer.writeNext(user);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Authenticates user
     * @param email the email
     * @param password the password
     * @return whether user has given valid or invalid credentials
     */
    public static boolean verifyUser(String email, String password) {
        try (CSVReader reader = new CSVReader(new FileReader(CSV_PATH.toFile()))) {
            String[] nextLine;

            // skip header row
            reader.readNext();

            while ((nextLine = reader.readNext()) != null) {
                String storedEmail = nextLine[0];
                String storedHash = nextLine[1];

                if (storedEmail.equalsIgnoreCase(email)) {
                    return BCrypt.checkpw(password, storedHash);
                }
            }
        } catch (Exception e) {
            System.err.println("Database read error: " + e.getMessage());
        }

        // out of the while loop without finding email or password
        return false;
    }

    public static boolean isEmailAvailable(String email) {
        try (CSVReader reader = new CSVReader(new FileReader(CSV_PATH.toFile()))) {
            String[] nextLine;

            // skip header row
            reader.readNext();

            while ((nextLine = reader.readNext()) != null) {
                String storedEmail = nextLine[0];

                if (storedEmail.equalsIgnoreCase(email)) {
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println("Database read error: " + e.getMessage());
        }

        // out of the while loop
        return true;
    }
}