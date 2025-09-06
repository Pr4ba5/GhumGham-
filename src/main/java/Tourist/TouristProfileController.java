package Tourist;

import Models.User;
import Session.UserSession;
import Storage.JSONHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class TouristProfileController implements Initializable {

    @FXML
    private Label userAvatarLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    private User currentUser;
    private UserSession userSession;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userSession = UserSession.getInstance();
        loadCurrentUser();
        setupUI();
        displayUserInfo();
    }

    private void loadCurrentUser() {
        currentUser = userSession.getCurrentUser();

        if (currentUser == null) {
            showAlert("Error", "No user is currently logged in.", Alert.AlertType.ERROR);
        } else {
            System.out.println("Loaded current user: " + currentUser.getFullName());
        }
    }

    private void setupUI() {
        setFieldsEditable(false);

        // Avatar
        if (userAvatarLabel != null) {
            userAvatarLabel.setStyle(
                    "-fx-background-color: #3498db; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 50; " +
                            "-fx-font-size: 36px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-alignment: center; " +
                            "-fx-pref-width: 80; " +
                            "-fx-pref-height: 80;"
            );
        }

        // Name
        if (userNameLabel != null) {
            userNameLabel.setStyle(
                    "-fx-font-size: 24px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: #333333;"
            );
        }

        // Fields
        styleTextField(fullNameField);
        styleTextField(emailField);
        styleTextField(phoneField);

        // Email field read-only
        if (emailField != null) {
            emailField.setEditable(false);
            emailField.setStyle(emailField.getStyle() + " -fx-background-color: #f5f5f5;");
        }
    }

    private void styleTextField(TextField field) {
        if (field != null) {
            field.setStyle(
                    "-fx-padding: 12; " +
                            "-fx-font-size: 14px; " +
                            "-fx-border-color: #e0e0e0; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8;"
            );
        }
    }

    private void setFieldsEditable(boolean editable) {
        if (fullNameField != null) fullNameField.setEditable(editable);
        if (phoneField != null) phoneField.setEditable(editable);
        if (emailField != null) emailField.setEditable(false);
    }

    private void displayUserInfo() {
        if (currentUser == null) return;

        if (userAvatarLabel != null) {
            String avatarLetter = currentUser.getFirstName() != null && !currentUser.getFirstName().isEmpty()
                    ? currentUser.getFirstName().substring(0, 1).toUpperCase()
                    : "U";
            userAvatarLabel.setText(avatarLetter);
        }

        if (userNameLabel != null) {
            userNameLabel.setText(currentUser.getFullName());
        }

        if (fullNameField != null) {
            fullNameField.setText(currentUser.getFullName());
        }

        if (emailField != null) {
            emailField.setText(currentUser.getEmail());
        }

        if (phoneField != null) {
            phoneField.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // External refresh method
    public void refreshUserData() {
        loadCurrentUser();
        displayUserInfo();
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
