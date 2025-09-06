package Guide;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import Session.UserSession;
import Language.LanguageManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GuideDashboardController implements Initializable, LanguageManager.LanguageChangeListener {

    @FXML private ComboBox<String> languageCombo;
    @FXML private Label guideNameLabel;
    @FXML private Button dashboardBtn;
    @FXML private Button myTripsBtn;
    @FXML private Button touristsBtn;
    @FXML private Button profileBtn;
    @FXML private Button emergencyBtn;
    @FXML private Button logoutBtn;
    @FXML private StackPane contentArea;

    private LanguageManager languageManager;
    private boolean internalChange = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageManager = LanguageManager.getInstance();
        languageManager.addLanguageChangeListener(this);
        
        setupLanguageCombo();

        // Set guide name from session
        if (UserSession.getInstance().getCurrentUser() != null) {
            guideNameLabel.setText(UserSession.getInstance().getCurrentUserFullName());
        }

        updateTexts();
        // Load dashboard by default
        showDashboard(null);
    }

    private void setupLanguageCombo() {
        if (languageCombo == null || languageManager == null) return;

        internalChange = true;

        languageCombo.getItems().clear();
        languageCombo.getItems().addAll(
                languageManager.getString("language.english"),
                languageManager.getString("language.nepali")
        );

        String currentLang = languageManager.getCurrentLocale().getLanguage();
        if ("ne".equals(currentLang)) {
            languageCombo.setValue(languageManager.getString("language.nepali"));
        } else {
            languageCombo.setValue(languageManager.getString("language.english"));
        }

        internalChange = false;

        languageCombo.setOnAction(e -> {
            if (internalChange) return;

            String selectedLanguage = languageCombo.getValue();
            if (selectedLanguage != null) {
                if (selectedLanguage.contains("Nepali") || selectedLanguage.contains("नेपाली")) {
                    languageManager.setLanguage("ne");
                } else {
                    languageManager.setLanguage("en");
                }
            }
        });
    }

    @Override
    public void onLanguageChanged() {
        updateTexts();
        updateComboBoxLabels();
    }

    private void updateTexts() {
        if (languageManager == null) return;

        try {
            dashboardBtn.setText("🏠 " + languageManager.getString("nav.dashboard"));
            myTripsBtn.setText("🥾 " + languageManager.getString("guide.trips"));
            touristsBtn.setText("👥 " + languageManager.getString("guide.tourists"));
            emergencyBtn.setText("🚨 " + languageManager.getString("guide.emergency"));
            profileBtn.setText("👤 " + languageManager.getString("nav.profile"));
            logoutBtn.setText(languageManager.getString("nav.logout"));
        } catch (Exception e) {
            System.err.println("Error updating guide texts: " + e.getMessage());
        }
    }

    private void updateComboBoxLabels() {
        if (languageCombo == null || languageManager == null) return;

        try {
            String newEnglishLabel = languageManager.getString("language.english");
            String newNepaliLabel = languageManager.getString("language.nepali");

            internalChange = true;
            languageCombo.getItems().setAll(newEnglishLabel, newNepaliLabel);

            if (languageManager.getCurrentLocale().getLanguage().equals("ne")) {
                languageCombo.setValue(newNepaliLabel);
            } else {
                languageCombo.setValue(newEnglishLabel);
            }
            internalChange = false;
        } catch (Exception e) {
            System.err.println("Error updating ComboBox labels: " + e.getMessage());
        }
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        setActiveButton(dashboardBtn);
        loadContent("/Guide/guide_dashboard_content.fxml");
    }

    @FXML
    private void showMyTrips(ActionEvent event) {
        setActiveButton(myTripsBtn);
        loadContent("/Guide/guide_trips_content.fxml");
    }

    @FXML
    private void showTourists(ActionEvent event) {
        setActiveButton(touristsBtn);
        loadContent("/Guide/guide_tourist_content.fxml");
    }

    @FXML
    private void showProfile(ActionEvent event) {
        setActiveButton(profileBtn);
        loadContent("/Guide/guide_profile_content.fxml");
    }

    @FXML
    private void showEmergency(ActionEvent event) {
        setActiveButton(emergencyBtn);
        loadContent("/Guide/emergencyManagement.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(languageManager.getString("nav.logout"));
        confirmAlert.setHeaderText(languageManager.getString("logout.confirm.header"));
        confirmAlert.setContentText(languageManager.getString("logout.confirm.content"));

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Clean up language manager listener
                    languageManager.removeLanguageChangeListener(this);
                    
                    // Clear user session
                    UserSession.getInstance().logout();

                    // Load login screen
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/First/Login.fxml"));
                    Scene loginScene = new Scene(loader.load());

                    // Get current stage and set login scene
                    Stage currentStage = (Stage) logoutBtn.getScene().getWindow();
                    currentStage.setScene(loginScene);
                    currentStage.setTitle("GHUMGHAM - Login");
                    currentStage.centerOnScreen();

                    System.out.println("Guide logged out successfully");

                } catch (IOException e) {
                    System.err.println("Error loading login screen: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Error", "Could not load login screen. Please restart the application.");
                }
            }
        });
    }

    private void setActiveButton(Button activeButton) {
        // Reset all buttons
        dashboardBtn.setStyle("-fx-background-color: transparent; -fx-padding: 10; -fx-font-size: 14;");
        myTripsBtn.setStyle("-fx-background-color: transparent; -fx-padding: 10; -fx-font-size: 14;");
        touristsBtn.setStyle("-fx-background-color: transparent; -fx-padding: 10; -fx-font-size: 14;");
        profileBtn.setStyle("-fx-background-color: transparent; -fx-padding: 10; -fx-font-size: 14;");
        emergencyBtn.setStyle("-fx-background-color: transparent; -fx-padding: 10; -fx-font-size: 14;");

        // Set active button style
        activeButton.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-padding: 10; -fx-font-size: 14; -fx-background-radius: 5;");
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load content: " + fxmlPath);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
