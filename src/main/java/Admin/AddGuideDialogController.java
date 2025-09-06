// Complete fix for AddGuideDialogController.java
package Admin;

import Models.Guide; // Import Guide instead of User
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddGuideDialogController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField phoneField;
    @FXML private TextField nationalityField;
    @FXML private TextField languageField;
    @FXML private TextArea experienceField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private GuidesContentController parentController;

    @FXML
    private void initialize() {
        saveButton.setOnAction(e -> saveGuide()); // Fix: Correct method name
        cancelButton.setOnAction(e -> closeDialog());
    }

    public void setParentController(GuidesContentController parentController) {
        this.parentController = parentController;
    }

    private void saveGuide() { // Fix: Renamed from saveTourist
        if (firstNameField.getText().trim().isEmpty() ||
                lastNameField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                phoneField.getText().trim().isEmpty() ||
                passwordField.getText().trim().isEmpty() ||
                nationalityField.getText().trim().isEmpty() ||
                languageField.getText().trim().isEmpty() ||
                experienceField.getText().trim().isEmpty()) {

            showAlert();
            return;
        }

        // Fix: Create Guide object with all required fields
        Guide guide = new Guide(
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                passwordField.getText().trim(),
                nationalityField.getText().trim(),
                languageField.getText().trim(),
                experienceField.getText().trim()
        );

        parentController.addGuide(guide); // Fix: Use correct variable name
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
}
