package Admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import Models.*;

public class AddAttractionDialogController {

    @FXML private TextField nameField;
    @FXML private TextField locationField;
    @FXML private ComboBox<String> difficultyComboBox;
    @FXML private TextField typeField;
    @FXML private TextArea remarksArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private AttractionsContentController parentController;

    @FXML
    private void initialize() {
        difficultyComboBox.getItems().addAll("Easy", "Moderate", "Hard");

        saveButton.setOnAction(e -> saveAttraction());
        cancelButton.setOnAction(e -> closeDialog());
    }

    public void setParentController(AttractionsContentController parentController) {
        this.parentController = parentController;
    }

    private void saveAttraction() {
        if (nameField.getText().trim().isEmpty() ||
                locationField.getText().trim().isEmpty() ||
                difficultyComboBox.getValue() == null ||
                typeField.getText().trim().isEmpty()) {

            showAlert();
            return;
        }

        Attraction attraction = new Attraction(
                nameField.getText().trim(),
                locationField.getText().trim(),
                difficultyComboBox.getValue(),
                typeField.getText().trim(),
                remarksArea.getText().trim()
        );

        parentController.addAttraction(attraction);
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
