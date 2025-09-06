package Admin;

import Models.Attraction;
import Storage.AdminJSONHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AttractionsContentController implements Initializable {

    @FXML private TableView<Attraction> attractionsTable;
    @FXML public TableColumn<Attraction, String> placeColumn;
    @FXML private TableColumn<Attraction, String> locationColumn;
    @FXML private TableColumn<Attraction, String> difficultyColumn;
    @FXML private TableColumn<Attraction, String> typeColumn;
    @FXML private TableColumn<Attraction, String> remarksColumn;
    @FXML private TextField searchField;
    @FXML private Label totalAttractionsLabel;
    @FXML private Label selectedAttractionLabel;
    @FXML private Button deleteButton;

    private AdminJSONHandler jsonHandler;
    private ObservableList<Attraction> attractionsList;
    private ObservableList<Attraction> filteredList;
    private Attraction selectedAttraction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jsonHandler = new AdminJSONHandler();
        attractionsList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();

        setupTableColumns();
        setupSearchFilter();
        setupTableSelection();
        loadAttractions();
    }

    private void setupTableColumns() {
        placeColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        difficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        remarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        attractionsTable.setItems(filteredList);

        // Add row click handler
        attractionsTable.setRowFactory(tv -> {
            TableRow<Attraction> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Attraction clickedAttraction = row.getItem();
                    selectAttraction(clickedAttraction);
                }
            });
            return row;
        });
    }

    private void setupTableSelection() {
        attractionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectAttraction(newSelection);
            } else {
                clearSelection();
            }
        });
    }

    private void selectAttraction(Attraction attraction) {
        selectedAttraction = attraction;
        selectedAttractionLabel.setText("Selected: " + attraction.getName());

        // Make delete button opaque and enabled
        deleteButton.setOpacity(1.0);
        deleteButton.setDisable(false);
    }

    private void clearSelection() {
        selectedAttraction = null;
        selectedAttractionLabel.setText("No attraction selected");

        // Make delete button transparent and disabled
        deleteButton.setOpacity(0.3);
        deleteButton.setDisable(true);
    }

    @FXML
    private void deleteSelectedAttraction() {
        if (selectedAttraction == null) {
            showAlert("Warning", "Please select an attraction to delete.");
            return;
        }

        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Attraction");
        confirmAlert.setContentText("Are you sure you want to delete the attraction: " +
                selectedAttraction.getName() + "?\n\nThis action cannot be undone.");

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
            boolean success = jsonHandler.deleteAttraction(selectedAttraction.getId());

            if (success) {
                showAlert("Success", "Attraction '" + selectedAttraction.getName() + "' has been deleted successfully.");
                loadAttractions(); // Refresh the table
                clearSelection(); // Clear the selection
            } else {
                showAlert("Error", "Failed to delete the attraction. Please try again.");
            }
        }
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAttractions(newValue);
            clearSelection(); // Clear selection when searching
        });
    }

    private void filterAttractions(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredList.setAll(attractionsList);
        } else {
            List<Attraction> filtered = attractionsList.stream()
                    .filter(attraction ->
                            attraction.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                                    attraction.getLocation().toLowerCase().contains(searchText.toLowerCase()) ||
                                    attraction.getType().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());
            filteredList.setAll(filtered);
        }
        updateTotalLabel();
    }

    public void loadAttractions() {
        List<Attraction> attractions = jsonHandler.loadAttractions();
        attractionsList.setAll(attractions);
        filteredList.setAll(attractions);
        updateTotalLabel();
        clearSelection(); // Clear selection when data is reloaded
    }

    private void updateTotalLabel() {
        totalAttractionsLabel.setText("Total Attractions: " + filteredList.size());
    }

    @FXML
    private void addNewAttraction() {
        openAddAttractionDialog();
    }

    @FXML
    private void refreshData() {
        loadAttractions();
        showAlert("Success", "Data refreshed successfully!");
    }

    private void openAddAttractionDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddAttractionDialog.fxml"));
            Parent root = loader.load();

            AddAttractionDialogController controller = loader.getController();
            controller.setParentController(this);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Attraction");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open Add Attraction dialog.");
        }
    }

    public void addAttraction(Attraction attraction) {
        if (jsonHandler.addAttraction(attraction)) {
            loadAttractions();
            showAlert("Success", "Attraction added successfully!");
        } else {
            showAlert("Error", "Failed to add attraction.");
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
