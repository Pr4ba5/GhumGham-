module org.example.finall {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.desktop;

    opens First to javafx.fxml;
    opens Admin to javafx.fxml, javafx.base;
    opens Models to com.google.gson, javafx.base;
    opens Guide to javafx.fxml;
    opens Tourist to javafx.fxml;

    exports Tourist to javafx.fxml;
    exports Main;
    opens Main to javafx.fxml;
}
