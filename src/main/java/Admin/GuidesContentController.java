package Admin;

import Models.Attraction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import Models.Guide;
import Storage.JSONHandler;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GuidesContentController implements Initializable {

    @FXML private TextField searchField;
    @FXML private TableView<GuideTableData> guidesTable;
    @FXML private Label totalGuidesLabel;
    @FXML private Label selectedGuideLabel;
    @FXML private Button deleteButton;

    private JSONHandler fileManager;
    private ObservableList<GuideTableData> guideData;
    private GuideTableData selectedGuide;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileManager = new JSONHandler();
        guideData = FXCollections.observableArrayList();

        setupTable();
        loadGuideData();
        setupSearch();
        setupTableSelection();
    }

    private void setupTable() {
        guidesTable.setItems(guideData);

        guidesTable.setRowFactory(tv -> {
            TableRow<GuideTableData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    GuideTableData clickedGuide = row.getItem();
                    selectGuide(clickedGuide);

                    // Double click to show details
                    if (event.getClickCount() == 2) {
                        showGuideDetails(clickedGuide);
                    }
                }
            });
            return row;
        });
    }

    private void setupTableSelection() {
        guidesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectGuide(newSelection);
            } else {
                clearSelection();
            }
        });
    }

    private void selectGuide(GuideTableData guide) {
        selectedGuide = guide;
        selectedGuideLabel.setText("Selected: " + guide.getFullName());

        // Make delete button opaque and enabled
        deleteButton.setOpacity(1.0);
        deleteButton.setDisable(false);
    }

    private void clearSelection() {
        selectedGuide = null;
        selectedGuideLabel.setText("No guide selected");

        // Make delete button transparent and disabled
        deleteButton.setOpacity(0.3);
        deleteButton.setDisable(true);
    }

    @FXML
    private void deleteSelectedGuide() {
        if (selectedGuide == null) {
            showAlert("Warning", "Please select a guide to delete.");
            return;
        }

        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Guide");
        confirmAlert.setContentText("Are you sure you want to delete the guide: " +
                selectedGuide.getFullName() + "?\n\nThis action cannot be undone.");

        // Add custom buttons
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(deleteButtonType, cancelButtonType);

        // Style the delete button to be red
        confirmAlert.getDialogPane().lookupButton(deleteButtonType).setStyle(
                "-fx-background-color: #DC3545; -fx-text-fill: white;"
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == deleteButtonType) {
            // Proceed with deletion
            boolean success = JSONHandler.deleteGuide(selectedGuide.getEmail());

            if (success) {
                showAlert("Success", "Guide '" + selectedGuide.getFullName() + "' has been deleted successfully.");
                loadGuideData(); // Refresh the table
                clearSelection(); // Clear the selection
            } else {
                showAlert("Error", "Failed to delete the guide. Please try again.");
            }
        }
    }

    private void loadGuideData() {
        guideData.clear();
        List<Guide> guides = fileManager.getGuides();

        for (Guide guide : guides) {
            GuideTableData data = new GuideTableData();
            data.setFullName(guide.getFullName());
            data.setLanguages(guide.getProficiencyLanguage());
            data.setPhone(guide.getPhone());
            data.setExperienceYears(extractExperienceYears(guide.getExperience()));
            data.setEmail(guide.getEmail());
            guideData.add(data);
        }

        updateStatistics();
        clearSelection(); // Clear selection when data is reloaded
    }

    private String extractExperienceYears(String experience) {
        if (experience != null && experience.contains("years")) {
            try {
                String[] parts = experience.split(" ");
                for (String part : parts) {
                    if (part.matches("\\d+")) {
                        return part + " yrs";
                    }
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
        return "N/A";
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                guidesTable.setItems(guideData);
            } else {
                ObservableList<GuideTableData> filteredData = guideData.stream()
                        .filter(guide ->
                                guide.getFullName().toLowerCase().contains(newValue.toLowerCase()) ||
                                        guide.getEmail().toLowerCase().contains(newValue.toLowerCase()) ||
                                        guide.getLanguages().toLowerCase().contains(newValue.toLowerCase()))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));
                guidesTable.setItems(filteredData);
            }
            clearSelection(); // Clear selection when searching
        });
    }

    @FXML
    private void addGuide() {
        openAddGuideDialog();
    }

    private void openAddGuideDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddGuideDialog.fxml"));
            Parent root = loader.load();

            AddGuideDialogController controller = loader.getController();
            controller.setParentController(this);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Guide");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open add guide dialog.");
        }
    }

    public void addGuide(Guide guide) {
        if (JSONHandler.addGuide(guide)) {
            loadGuideData();
            showAlert("Success", "Guide added successfully!");
        } else {
            showAlert("Error", "Failed to add guide. Email might already exist.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void refreshData(ActionEvent event) {
        loadGuideData();
        showAlert("Success", "Data refreshed successfully!");
    }

    private void updateStatistics() {
        int total = guideData.size();
        totalGuidesLabel.setText("Total Guides: " + total);
    }

    private void showGuideDetails(GuideTableData guide) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Guide Details");
        alert.setHeaderText(guide.getFullName());
        alert.setContentText(
                "Email: " + guide.getEmail() + "\n" +
                        "Phone: " + guide.getPhone() + "\n" +
                        "Languages: " + guide.getLanguages() + "\n" +
                        "Experience: " + guide.getExperienceYears()
        );
        alert.showAndWait();
    }

    // Inner class for table data
    public static class GuideTableData {
        private String fullName;
        private String languages;
        private String phone;
        private String experienceYears;
        private String email;

        // Getters and setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getLanguages() { return languages; }
        public void setLanguages(String languages) { this.languages = languages; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getExperienceYears() { return experienceYears; }
        public void setExperienceYears(String experienceYears) { this.experienceYears = experienceYears; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
