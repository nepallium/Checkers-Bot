module org.example.checkers {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok; // 'static' because lombok is only needed at compile time

    opens main to javafx.fxml;
    exports main;

    opens UI to javafx.fxml;
    exports UI;
}