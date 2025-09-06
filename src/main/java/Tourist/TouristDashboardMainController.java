package Tourist;

import Models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.stage.Stage;
import Session.UserSession;
import Language.LanguageManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TouristDashboardMainController implements Initializable, LanguageManager.LanguageChangeListener {

    @FXML
    private ComboBox<String> languageCombo;

    @FXML
    private Label userAvatar;

    @FXML
    private Label userNameLabel;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button profileBtn;

    @FXML
    private Button exploreBtn;

    @FXML
    private Button bookingsBtn;

    @FXML
    private Button logoutBtn; // NEW: Logout button

    @FXML
    private StackPane contentArea;

    // Current active button for styling
    private boolean internalChange = false;
    private Button activeButton;
    private LanguageManager languageManager;
    private boolean isInitialized = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("Initializing TouristDashboardMainController...");

            // Initialize language manager first
            languageManager = LanguageManager.getInstance();
            if (languageManager != null) {
                languageManager.addLanguageChangeListener(this);
                System.out.println("Language manager initialized successfully");
            } else {
                System.err.println("Failed to initialize language manager");
            }

            // Check if FXML elements are injected
            checkFXMLElements();

            setupLanguageCombo();
            setupEventHandlers();

            // Set active button safely
            if (dashboardBtn != null) {
                setActiveButton(dashboardBtn);
            }

            // Set user name safely
            setupUserName();

            showDashboard();
            updateTexts(); // Initialize with current language

            isInitialized = true;
            System.out.println("TouristDashboardMainController initialized successfully");

        } catch (Exception e) {
            System.err.println("Error initializing TouristDashboardMainController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // NEW: Logout functionality
    @FXML
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Logout Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to logout?");
        confirmAlert.setContentText("You will be redirected to the login screen.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Clean up language manager listener
                    cleanup();

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

                    System.out.println("Tourist logged out successfully");

                } catch (IOException e) {
                    System.err.println("Error loading login screen: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Error", "Could not load login screen. Please restart the application.");
                }
            }
        });
    }

    private void checkFXMLElements() {
        System.out.println("Checking FXML elements...");
        System.out.println("languageCombo: " + (languageCombo != null ? "OK" : "NULL"));
        System.out.println("userNameLabel: " + (userNameLabel != null ? "OK" : "NULL"));
        System.out.println("dashboardBtn: " + (dashboardBtn != null ? "OK" : "NULL"));
        System.out.println("profileBtn: " + (profileBtn != null ? "OK" : "NULL"));
        System.out.println("exploreBtn: " + (exploreBtn != null ? "OK" : "NULL"));
        System.out.println("bookingsBtn: " + (bookingsBtn != null ? "OK" : "NULL"));
        System.out.println("logoutBtn: " + (logoutBtn != null ? "OK" : "NULL"));
        System.out.println("contentArea: " + (contentArea != null ? "OK" : "NULL"));
    }

    private void setupUserName() {
        try {
            if (userNameLabel != null) {
                User currentUser = UserSession.getInstance().getCurrentUser();
                if (currentUser != null && currentUser.getFirstName() != null) {
                    userNameLabel.setText(currentUser.getFirstName());
                } else {
                    userNameLabel.setText("Guest");
                }
            } else {
                System.err.println("userNameLabel is null - check FXML file");
            }
        } catch (Exception e) {
            System.err.println("Error setting up user name: " + e.getMessage());
        }
    }

    private void setupLanguageCombo() {
        try {
            if (languageCombo == null || languageManager == null) return;

            internalChange = true; // prevent triggering onAction while setting items

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

        } catch (Exception e) {
            System.err.println("Error setting up language combo: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        // Event handlers remain the same but with null checks
        System.out.println("Setting up event handlers...");
    }

    @FXML
    private void showDashboard() {
        loadContent("/Tourist/TouristDashboardContent.fxml");
        if (dashboardBtn != null) {
            setActiveButton(dashboardBtn);
        }
    }

    @FXML
    private void showProfile() {
        loadContent("/Tourist/touristProfile.fxml");
        if (profileBtn != null) {
            setActiveButton(profileBtn);
        }
    }

    @FXML
    private void showExplore() {
        loadContent("/Tourist/ExploreContent.fxml");
        if (exploreBtn != null) {
            setActiveButton(exploreBtn);
        }
    }

    @FXML
    private void showBookings() {
        loadContent("/Tourist/touristsBooking.fxml");
        if (bookingsBtn != null) {
            setActiveButton(bookingsBtn);
        }
    }

    private void loadContent(String fxmlPath) {
        try {
            if (contentArea == null) {
                System.err.println("contentArea is null - cannot load content");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);

        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
            showErrorContent("Failed to load content: " + fxmlPath);
        } catch (Exception e) {
            System.err.println("Unexpected error loading content: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showErrorContent(String message) {
        try {
            if (contentArea != null) {
                Label errorLabel = new Label(message);
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                contentArea.getChildren().clear();
                contentArea.getChildren().add(errorLabel);
            }
        } catch (Exception e) {
            System.err.println("Error showing error content: " + e.getMessage());
        }
    }

    private void setActiveButton(Button button) {
        try {
            if (button == null) {
                System.err.println("Cannot set active button - button is null");
                return;
            }

            resetButtonStyles();
            button.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-padding: 12; -fx-font-size: 14; -fx-background-radius: 8;");
            activeButton = button;

        } catch (Exception e) {
            System.err.println("Error setting active button: " + e.getMessage());
        }
    }

    private void resetButtonStyles() {
        try {
            String defaultStyle = "-fx-background-color: transparent; -fx-padding: 12; -fx-font-size: 14;";

            if (dashboardBtn != null) dashboardBtn.setStyle(defaultStyle);
            if (profileBtn != null) profileBtn.setStyle(defaultStyle);
            if (exploreBtn != null) exploreBtn.setStyle(defaultStyle);
            if (bookingsBtn != null) bookingsBtn.setStyle(defaultStyle);

        } catch (Exception e) {
            System.err.println("Error resetting button styles: " + e.getMessage());
        }
    }

    @Override
    public void onLanguageChanged() {
        if (!isInitialized) return;

        updateTexts(); // for buttons
        updateComboBoxLabels(); // update ComboBox label texts without triggering event
    }

    private void updateTexts() {
        try {
            if (languageManager == null) {
                System.err.println("languageManager is null - cannot update texts");
                return;
            }
            // Update button texts with null checks
            if (dashboardBtn != null) {
                dashboardBtn.setText(languageManager.getString("nav.dashboard"));
            }
            if (profileBtn != null) {
                profileBtn.setText(languageManager.getString("nav.profile"));
            }
            if (exploreBtn != null) {
                exploreBtn.setText(languageManager.getString("nav.explore"));
            }
            if (bookingsBtn != null) {
                bookingsBtn.setText(languageManager.getString("nav.bookings"));
            }
            if (logoutBtn != null) {
                logoutBtn.setText(languageManager.getString("nav.logout"));
            }

            System.out.println("UI texts updated to: " + languageManager.getCurrentLocale().getDisplayName());

        } catch (Exception e) {
            System.err.println("Error updating texts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cleanup() {
        try {
            if (languageManager != null) {
                languageManager.removeLanguageChangeListener(this);
            }
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    private void updateComboBoxLabels() {
        try {
            if (languageCombo == null || languageManager == null) return;

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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
