package Admin;

import Models.Guide;
import Models.Trek;
import Storage.AdminJSONHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TreksContentController implements Initializable {

    @FXML private TableView<Trek> treksTable;
    @FXML private TableColumn<Trek, String> trekNameColumn;
    @FXML private TableColumn<Trek, LocalDate> dateColumn;
    @FXML private TableColumn<Trek, String> durationColumn;
    @FXML private TableColumn<Trek, String> difficultyColumn;
    @FXML private TableColumn<Trek, String> maxAltitudeColumn;
    @FXML private TableColumn<Trek, Double> costColumn;
    @FXML private TableColumn<Trek, String> bestSeasonColumn;
    @FXML private TableColumn<Trek, String> guideColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> difficultyFilter;
    @FXML private Label totalTreksLabel;
    @FXML private Label easyCountLabel;
    @FXML private Label moderateCountLabel;
    @FXML private Label hardCountLabel;
    @FXML private Label selectedTrekLabel;
    @FXML private Button deleteButton;

    private AdminJSONHandler jsonHandler;
    private ObservableList<Trek> treksList;
    private ObservableList<Trek> filteredList;
    private Trek selectedTrek;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jsonHandler = new AdminJSONHandler();
        treksList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();

        setupTableColumns();
        setupFilters();
        setupTableSelection();
        loadTreks();
    }

    private void setupTableColumns() {
        trekNameColumn.setCellValueFactory(new PropertyValueFactory<>("trekName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        difficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        maxAltitudeColumn.setCellValueFactory(new PropertyValueFactory<>("maxAltitude"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        bestSeasonColumn.setCellValueFactory(new PropertyValueFactory<>("bestSeason"));

        // Custom cell value factory for guide column to show guide name instead of email
        guideColumn.setCellValueFactory(cellData -> {
            String guideEmail = cellData.getValue().getGuideEmail();
            if (guideEmail != null) {
                Guide guide = jsonHandler.getGuideByEmail(guideEmail);
                if (guide != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            guide.getFirstName() + " " + guide.getLastName());
                }
            }
            return new javafx.beans.property.SimpleStringProperty("No Guide");
        });

        treksTable.setItems(filteredList);

        // Add row click handler
        treksTable.setRowFactory(tv -> {
            TableRow<Trek> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Trek clickedTrek = row.getItem();
                    selectTrek(clickedTrek);
                }
            });
            return row;
        });
    }

    private void setupTableSelection() {
        treksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectTrek(newSelection);
            } else {
                clearSelection();
            }
        });
    }

    private void selectTrek(Trek trek) {
        selectedTrek = trek;
        selectedTrekLabel.setText("Selected: " + trek.getTrekName());

        // Make delete button opaque and enabled
        deleteButton.setOpacity(1.0);
        deleteButton.setDisable(false);
    }

    private void clearSelection() {
        selectedTrek = null;
        selectedTrekLabel.setText("No trek selected");

        // Make delete button transparent and disabled
        deleteButton.setOpacity(0.3);
        deleteButton.setDisable(true);
    }

    @FXML
    private void deleteSelectedTrek() {
        if (selectedTrek == null) {
            showAlert("Warning", "Please select a trek to delete.");
            return;
        }

        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Trek");
        confirmAlert.setContentText("Are you sure you want to delete the trek: " +
                selectedTrek.getTrekName() + "?\n\nThis action cannot be undone.");

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
            boolean success = jsonHandler.deleteTrek(selectedTrek.getId());

            if (success) {
                showAlert("Success", "Trek '" + selectedTrek.getTrekName() + "' has been deleted successfully.");
                loadTreks(); // Refresh the table
                clearSelection(); // Clear the selection
            } else {
                showAlert("Error", "Failed to delete the trek. Please try again.");
            }
        }
    }

    private void setupFilters() {
        // Setup difficulty filter
        difficultyFilter.setItems(FXCollections.observableArrayList(
                "Filter by Difficulty", "Easy", "Moderate", "Hard"));
        difficultyFilter.setValue("Filter by Difficulty");

        // Add listeners
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTreks();
            clearSelection(); // Clear selection when searching
        });

        difficultyFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterTreks();
            clearSelection(); // Clear selection when filtering
        });
    }

    private void filterTreks() {
        String searchText = searchField.getText();
        String selectedDifficulty = difficultyFilter.getValue();

        List<Trek> filtered = treksList.stream()
                .filter(trek -> {
                    boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                            trek.getTrekName().toLowerCase().contains(searchText.toLowerCase()) ||
                            getGuideName(trek.getGuideEmail()).toLowerCase().contains(searchText.toLowerCase());

                    boolean matchesDifficulty = selectedDifficulty == null ||
                            selectedDifficulty.equals("Filter by Difficulty") ||
                            trek.getDifficulty().equalsIgnoreCase(selectedDifficulty);

                    return matchesSearch && matchesDifficulty;
                })
                .collect(Collectors.toList());

        filteredList.setAll(filtered);
        updateLabels();
    }

    private String getGuideName(String guideEmail) {
        if (guideEmail != null) {
            Guide guide = jsonHandler.getGuideByEmail(guideEmail);
            if (guide != null) {
                return guide.getFirstName() + " " + guide.getLastName();
            }
        }
        return "No Guide";
    }

    public void loadTreks() {
        List<Trek> treks = jsonHandler.loadTreks();
        treksList.setAll(treks);
        filteredList.setAll(treks);
        updateLabels();
        clearSelection(); // Clear selection when data is reloaded
    }

    private void updateLabels() {
        int total = filteredList.size();
        int easy = (int) filteredList.stream().filter(t -> "Easy".equalsIgnoreCase(t.getDifficulty())).count();
        int moderate = (int) filteredList.stream().filter(t -> "Moderate".equalsIgnoreCase(t.getDifficulty())).count();
        int hard = (int) filteredList.stream().filter(t -> "Hard".equalsIgnoreCase(t.getDifficulty())).count();

        totalTreksLabel.setText("Total Treks: " + total);
        easyCountLabel.setText("Easy: " + easy);
        moderateCountLabel.setText("Moderate: " + moderate);
        hardCountLabel.setText("Hard: " + hard);
    }

    @FXML
    private void addNewTrek() {
        openAddTrekDialog();
    }

    @FXML
    private void refreshData() {
        loadTreks();
        showAlert("Success", "Data refreshed successfully!");
    }

    private void openAddTrekDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddTrekDialog.fxml"));
            Parent root = loader.load();

            AddTrekDialogController controller = loader.getController();
            controller.setParentController(this);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Trek");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open Add Trek dialog.");
        }
    }

    public void addTrek(Trek trek) {
        if (jsonHandler.addTrek(trek)) {
            loadTreks();
            showAlert("Success", "Trek added successfully!");
        } else {
            showAlert("Error", "Failed to add trek.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
