package Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainApplication.primaryStage = primaryStage;

        URL mainPageUrl = getClass().getResource("/First/MainPage.fxml");
        if (mainPageUrl == null) {
            throw new RuntimeException("MainPage.fxml not found");
        }

        Parent root = FXMLLoader.load(mainPageUrl);

        primaryStage.setTitle("GHUMGHAM - Trekking Software");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void changeScene(String fxmlFile) {
        try {
            URL fxmlUrl = MainApplication.class.getResource(fxmlFile);
            if (fxmlUrl == null) {
                throw new IllegalArgumentException("FXML file not found: " + fxmlFile);
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            primaryStage.getScene().setRoot(root);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
