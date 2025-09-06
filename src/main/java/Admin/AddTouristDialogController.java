package Admin;

import Models.Attraction;
import Models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddTouristDialogController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField phoneField;
    @FXML private TextField nationalityField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private TouristContentController parentController;



    @FXML
    private void initialize() {
        saveButton.setOnAction(e -> saveTourist()); // Changed method name
        cancelButton.setOnAction(e -> closeDialog());
    }

    private void saveTourist() { // Renamed from saveAttraction
        if (firstNameField.getText().trim().isEmpty() ||
                lastNameField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                phoneField.getText().trim().isEmpty() ||
                passwordField.getText().trim().isEmpty() ||
                nationalityField.getText().trim().isEmpty()) {
                ;
            showAlert();
            return;
        }

        // Fix parameter order: (firstName, lastName, email, phone, password, nationality)
        User tourist = new User(
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),    // phone comes before password
                passwordField.getText().trim(),  // password comes last
                nationalityField.getText().trim()
        );
        parentController.addTourist(tourist);
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText("Please fill in all required fields.");
        alert.showAndWait();
    }
    public void setParentController(TouristContentController parentController) {
        this.parentController = parentController;
    }
}
