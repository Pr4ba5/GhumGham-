package First;

import Main.MainApplication;
import Models.User;
import Models.Guide;
import Session.UserSession;
import Storage.JSONHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleBack() {
        MainApplication.changeScene("/First/MainPage.fxml");
    }

    @FXML
    private void handleLoginSubmit() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        List<User> users = JSONHandler.loadUsers();
        for (User user : users) {
            if ((user.getEmail().equalsIgnoreCase(username) || user.getFirstName().equalsIgnoreCase(username))
                    && user.getPassword().equals(password)) {

                UserSession.getInstance().setCurrentUser(user);  // << Add this line

                String userType = user.getUserType() != null ? user.getUserType().toLowerCase() : "user";

                if (userType.equals("admin")) {
                    showSuccess("Welcome back, Admin " + user.getFirstName() + "!");
                    MainApplication.changeScene("/Admin/admin_dashboard.fxml");
                    return;
                }
                showSuccess("Welcome back, " + user.getFirstName() + "!");
                MainApplication.changeScene("/Tourist/tourist_dashboard_main.fxml");
                return;
            }
        }

        List<Guide> guides = JSONHandler.loadGuides();
        for (Guide guide : guides) {
            if ((guide.getEmail().equalsIgnoreCase(username) || guide.getFirstName().equalsIgnoreCase(username))
                    && guide.getPassword().equals(password)) {

                UserSession.getInstance().setCurrentUser(guide);  // << Add this line

                showSuccess("Welcome back, Guide " + guide.getFirstName() + "!");
                MainApplication.changeScene("/Guide/guide_dashboard.fxml");
                return;
            }
        }

        showError("Invalid credentials");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Successful");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
