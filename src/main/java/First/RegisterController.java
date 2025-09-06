package First;

import Main.MainApplication;
import Models.User;
import Storage.JSONHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class RegisterController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField nationalityField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleBack() {
        MainApplication.changeScene("/First/MainPage.fxml");
    }

    @FXML
    private void handleGuideRegister() {
        MainApplication.changeScene("/First/GuideRegister.fxml");
    }

    @FXML
    private void handleCreateAccount() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String nationality = nationalityField.getText().toLowerCase().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                || nationality.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (email.endsWith("@guide.com") || email.endsWith("@admin.com")) {
            showError("Please use the appropriate registration form for guides and admins");
            return;
        }

        // Check if user already exists
        if (JSONHandler.userExists(email)) {
            showError("User with this email already exists");
            return;
        }

        // Create new user
        User newUser = new User(firstName, lastName, email, phone, password, nationality);
        List<User> users = JSONHandler.loadUsers();
        users.add(newUser);

        if (JSONHandler.saveUsers(users)) {
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
        alert.setContentText("Account created successfully!");
        alert.showAndWait();
        MainApplication.changeScene("/First/MainPage.fxml");
    }
}
