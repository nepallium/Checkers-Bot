package UI.auth;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

    /**
     * Hashes plain password and adds to db.csv file
     * @param email the user's email
     * @param password the user's password, as is
     */
    public static void saveUser(String email, String password) {
        String hashedPw = BCrypt.hashpw(password, BCrypt.gensalt());
        try (CSVWriter writer = new CSVWriter(new FileWriter(CSV_PATH, true))) {
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
        try (CSVReader reader = new CSVReader(new FileReader(CSV_PATH))) {
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
        try (CSVReader reader = new CSVReader(new FileReader(CSV_PATH))) {
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