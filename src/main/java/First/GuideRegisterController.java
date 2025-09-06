package First;

import Main.MainApplication;
import Models.Guide;
import Storage.JSONHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class GuideRegisterController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField languageField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField nationalityField;

    @FXML
    private TextArea experienceArea;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleBack() {
        MainApplication.changeScene("/First/Register.fxml");
    }

    @FXML
    private void handleCreateAccount() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String language = languageField.getText().trim();
        String nationality = nationalityField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String experience = experienceArea.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                phone.isEmpty() || language.isEmpty() || password.isEmpty() ||
                nationality.isEmpty() || confirmPassword.isEmpty() || experience.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (!email.endsWith("@guide.com")) {
            showError("Guides must register with an @guide.com email address");
            return;
        }

        // Check if guide already exists
        if (JSONHandler.guideExists(email)) {
            showError("Guide with this email already exists");
            return;
        }

        // Create new guide
        Guide newGuide = new Guide(firstName, lastName, email, phone, password, nationality, language, experience);
        List<Guide> guides = JSONHandler.loadGuides();
        guides.add(newGuide);

        if (JSONHandler.saveGuides(guides)) {
            showSuccess();
        } else {
            showError("Failed to create account. Please try again.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Successful");
        alert.setHeaderText(null);
        alert.setContentText("Guide account created successfully!");
        alert.showAndWait();
        MainApplication.changeScene("/First/MainPage.fxml");
    }
}
