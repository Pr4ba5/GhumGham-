package Admin;

import First.RegisterController;
import Models.Attraction;
import Storage.AdminJSONHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import Models.User;
import Storage.JSONHandler;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TouristContentController implements Initializable {

    @FXML private TextField searchField;
    @FXML private TableView<TouristTableData> touristsTable;
    @FXML private TableColumn<TouristTableData, String> nameColumn;
    @FXML private TableColumn<TouristTableData, String> contactColumn;
    @FXML private TableColumn<TouristTableData, String> emailColumn;
    @FXML private Label totalTouristsLabel;
    @FXML private Label selectedTouristLabel;
    @FXML private Button addTourist;
    @FXML private Button deleteButton;

    private JSONHandler jsonHandler;
    private ObservableList<TouristTableData> touristData;
    private TouristTableData selectedTourist;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jsonHandler = new JSONHandler();
        touristData = FXCollections.observableArrayList();

        setupColumns();
        setupTable();
        loadTouristData();
        setupSearch();
        setupTableSelection();
    }

    private void setupTableSelection() {
        touristsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectTourist(newSelection);
            } else {
                clearSelection();
            }
        });
    }

    private void selectTourist(TouristTableData tourist) {
        selectedTourist = tourist;
        selectedTouristLabel.setText("Selected: " + tourist.getFullName());

        // Make delete button opaque and enabled
        deleteButton.setOpacity(1.0);
        deleteButton.setDisable(false);
    }

    private void clearSelection() {
        selectedTourist = null;
        selectedTouristLabel.setText("No tourist selected");

        // Make delete button transparent and disabled
        deleteButton.setOpacity(0.3);
        deleteButton.setDisable(true);
    }

    @FXML
    private void deleteSelectedTourist() {
        if (selectedTourist == null) {
            showAlert("Warning", "Please select a tourist to delete.");
            return;
        }

        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Tourist");
        confirmAlert.setContentText("Are you sure you want to delete the tourist: " +
                selectedTourist.getFullName() + "?\n\nThis action cannot be undone.");

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
            boolean success = JSONHandler.deleteUser(selectedTourist.getEmail());

            if (success) {
                showAlert("Success", "Tourist '" + selectedTourist.getFullName() + "' has been deleted successfully.");
                loadTouristData(); // Refresh the table
                clearSelection(); // Clear the selection
            } else {
                showAlert("Error", "Failed to delete the tourist. Please try again.");
            }
        }
    }

    public void addTourist(User tourist) {
        if (JSONHandler.addUser(tourist)) {
            loadTouristData();
            showAlert("Success", "Tourist added successfully!");
        } else {
            showAlert("Error", "Failed to add tourist. Email might already exist.");
        }
    }

    @FXML
    private void addTourist() {
        openAddTouristDialog();
    }

    private void openAddTouristDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Admin/AddTouristDialog.fxml"));
            Parent root = loader.load();

            AddTouristDialogController controller = loader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Add New Tourist");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open add tourist dialog.");
        }
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    private void setupTable() {
        touristsTable.setItems(touristData);

        touristsTable.setRowFactory(tv -> {
            TableRow<TouristTableData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    TouristTableData clickedTourist = row.getItem();
                    selectTourist(clickedTourist);

                    // Double click to show details
                    if (event.getClickCount() == 2) {
                        showTouristDetails(clickedTourist);
                    }
                }
            });
            return row;
        });
    }

    private void loadTouristData() {
        touristData.clear();
        List<User> allUsers = JSONHandler.loadUsers();
        List<User> tourists = allUsers.stream()
                .filter(u -> "user".equalsIgnoreCase(u.getUserType()))
                .toList();

        for (User tourist : tourists) {
            TouristTableData data = new TouristTableData();
            data.setFullName(tourist.getFullName());
            data.setEmail(tourist.getEmail());
            data.setPhone(tourist.getPhone());
            touristData.add(data);
        }

        updateStatistics();
        clearSelection(); // Clear selection when data is reloaded
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                touristsTable.setItems(touristData);
            } else {
                ObservableList<TouristTableData> filteredData = FXCollections.observableArrayList(
                        touristData.stream()
                                .filter(tourist ->
                                        tourist.getFullName().toLowerCase().contains(newValue.toLowerCase()) ||
                                                tourist.getEmail().toLowerCase().contains(newValue.toLowerCase())
                                )
                                .collect(Collectors.toList())
                );
                touristsTable.setItems(filteredData);
            }
            clearSelection(); // Clear selection when searching
        });
    }

    @FXML
    private void refreshData(ActionEvent event) {
        loadTouristData();
        showAlert("Success", "Data refreshed successfully!");
    }

    private void updateStatistics() {
        int total = touristData.size();
        totalTouristsLabel.setText("Total Tourists: " + total);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showTouristDetails(TouristTableData tourist) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tourist Details");
        alert.setHeaderText(tourist.getFullName());
        alert.setContentText(
                "Email: " + tourist.getEmail() + "\n" +
                        "Phone: " + tourist.getPhone()
        );
        alert.showAndWait();
    }

    public static class TouristTableData {
        private String fullName;
        private String phone;
        private String email;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
