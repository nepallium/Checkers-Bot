module org.example.checkers {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;
    requires static lombok;
    requires jbcrypt;
    requires com.opencsv; // 'static' because lombok is only needed at compile time

    opens main to javafx.fxml;
    exports main;

    opens UI to javafx.fxml;
    exports UI;
    exports UI.auth;
    opens UI.auth to javafx.fxml;
}