package Admin;

import Models.User;
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

public class AdminDashboardController implements Initializable, LanguageManager.LanguageChangeListener {

    @FXML private ComboBox<String> languageCombo;
    @FXML private Button settingsButton;
    @FXML private Label adminNameLabel;
    @FXML private Button dashboardBtn;
    @FXML private Button touristsBtn;
    @FXML private Button guidesBtn;
    @FXML private Button attractionsBtn;
    @FXML private Button bookingsBtn;
    @FXML private Button emergencyBtn;
    @FXML private Button treksBtn;
    @FXML private Button logoutBtn;
    @FXML private StackPane contentArea;

    private LanguageManager languageManager;
    private boolean internalChange = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageManager = LanguageManager.getInstance();
        languageManager.addLanguageChangeListener(this);
        
        setupLanguageCombo();
        
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            adminNameLabel.setText(currentUser.getFirstName());
        } else {
            adminNameLabel.setText("Guest");
        }
        
        updateTexts();
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
            dashboardBtn.setText(languageManager.getString("nav.dashboard"));
            touristsBtn.setText(languageManager.getString("admin.tourists"));
            guidesBtn.setText(languageManager.getString("admin.guides"));
            attractionsBtn.setText(languageManager.getString("admin.attractions"));
            bookingsBtn.setText(languageManager.getString("nav.bookings"));
            emergencyBtn.setText(languageManager.getString("admin.emergency"));
            treksBtn.setText(languageManager.getString("admin.treks"));
            logoutBtn.setText(languageManager.getString("nav.logout"));
        } catch (Exception e) {
            System.err.println("Error updating admin texts: " + e.getMessage());
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
        loadContent("/Admin/dashboard_content.fxml");
    }

    @FXML
    private void showTourists(ActionEvent event) {
        setActiveButton(touristsBtn);
        loadContent("/Admin/tourist_content.fxml");
    }

    @FXML
    private void showGuides(ActionEvent event) {
        setActiveButton(guidesBtn);
        loadContent("/Admin/guides_content.fxml");
    }

    @FXML
    private void showAttractions(ActionEvent event) {
        setActiveButton(attractionsBtn);
        loadContent("/Admin/attractions_content.fxml");
    }

    @FXML
    private void showBookings(ActionEvent event) {
        setActiveButton(bookingsBtn);
        loadContent("/Admin/bookings_content.fxml");
    }

    @FXML
    private void showEmergencyLogs(ActionEvent event) {
        setActiveButton(emergencyBtn);
        loadContent("/Admin/AdminEmergency.fxml");
    }

    @FXML
    private void showTreks(ActionEvent event) {
        setActiveButton(treksBtn);
        loadContent("/Admin/treks_content.fxml");
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

                    System.out.println("Admin logged out successfully");

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
        dashboardBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 0;");
        touristsBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 0;");
        guidesBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 0;");
        attractionsBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 0;");
        bookingsBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 0;");
        emergencyBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 0;");
        treksBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 0;");

        // Set active button style
        activeButton.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-background-radius: 0;");
    }

    private void loadContent(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Node content = loader.load();

            contentArea.getChildren().setAll(content);  // Replace any existing content
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
