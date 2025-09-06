package Admin;

import Models.Emergency;
import Storage.AdminJSONHandler;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdminEmergencyController implements Initializable {

    @FXML
    private TableView<EmergencyDisplayData> emergencyTable;

    @FXML
    private TableColumn<EmergencyDisplayData, String> idColumn;

    @FXML
    private TableColumn<EmergencyDisplayData, String> typeColumn;

    @FXML
    private TableColumn<EmergencyDisplayData, String> guideColumn;

    @FXML
    private TableColumn<EmergencyDisplayData, String> locationColumn;

    @FXML
    private TableColumn<EmergencyDisplayData, String> severityColumn;

    @FXML
    private TableColumn<EmergencyDisplayData, String> statusColumn;

    @FXML
    private TableColumn<EmergencyDisplayData, String> reportedColumn;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private ComboBox<String> severityFilter;

    @FXML
    private TextField searchField;

    @FXML
    private Button refreshButton;

    @FXML
    private Button viewDetailsButton;

    @FXML
    private Button resolveEmergencyButton;

    @FXML
    private Label totalEmergenciesLabel;

    @FXML
    private Label pendingEmergenciesLabel;

    @FXML
    private Label resolvedEmergenciesLabel;

    private ObservableList<EmergencyDisplayData> allEmergencies;
    private ObservableList<EmergencyDisplayData> filteredEmergencies;
    private AdminJSONHandler jsonHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jsonHandler = new AdminJSONHandler();

        setupTable();
        setupFilters();
        setupEventHandlers();
        loadAllEmergencies();

        System.out.println("AdminEmergencyController initialized");
    }

    private void setupTable() {
        // Set up table columns
        idColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().id())));

        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().emergencyType()));

        guideColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().guideName()));

        locationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().location()));

        severityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().severity()));

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().status()));

        reportedColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().formattedReportedDate()));

        // Custom cell factory for severity column to add styling
        severityColumn.setCellFactory(column -> {
            return new TableCell<>() {
                @Override
                protected void updateItem(String severity, boolean empty) {
                    super.updateItem(severity, empty);

                    if (empty || severity == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(severity);

                        // Style based on severity
                        switch (severity.toLowerCase()) {
                            case "low":
                                setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                                break;
                            case "medium":
                                setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                                break;
                            case "high":
                                setStyle("-fx-text-fill: #fd7e14; -fx-font-weight: bold;");
                                break;
                            case "critical":
                                setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                                break;
                            default:
                                setStyle("-fx-text-fill: #333333;");
                        }
                    }
                }
            };
        });

        // Custom cell factory for status column to add styling
        statusColumn.setCellFactory(column -> {
            return new TableCell<>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);

                    if (empty || status == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(status);

                        // Style based on status
                        switch (status.toLowerCase()) {
                            case "reported":
                                setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                                break;
                            case "in progress":
                                setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                                break;
                            case "resolved":
                                setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                                break;
                            default:
                                setStyle("-fx-text-fill: #333333;");
                        }
                    }
                }
            };
        });
    }

    private void setupFilters() {
        // Setup status filter
        statusFilter.getItems().addAll("All", "Reported", "In Progress", "Resolved");
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> applyFilters());

        // Setup severity filter
        severityFilter.getItems().addAll("All", "Low", "Medium", "High", "Critical");
        severityFilter.setValue("All");
        severityFilter.setOnAction(e -> applyFilters());

        // Setup search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void setupEventHandlers() {
        refreshButton.setOnAction(e -> loadAllEmergencies());
        refreshButton.setOnAction(e->showSuccess());
        viewDetailsButton.setOnAction(e -> viewSelectedEmergencyDetails());
        resolveEmergencyButton.setOnAction(e -> resolveSelectedEmergency());

        // Enable/disable buttons based on selection
        emergencyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            viewDetailsButton.setDisable(!hasSelection);

            // Only enable resolve if emergency is not already resolved
            boolean canResolve = hasSelection && !"resolved".equalsIgnoreCase(newSelection.status());
            resolveEmergencyButton.setDisable(!canResolve);
        });
    }

    private void loadAllEmergencies() {
        try {
            System.out.println("Loading all emergencies...");

            // Get ALL emergencies from JSON file
            List<Emergency> allEmergenciesList = jsonHandler.loadEmergencies();
            System.out.println("Found " + allEmergenciesList.size() + " total emergencies");

            // Convert to display data
            allEmergencies = FXCollections.observableArrayList();

            for (Emergency emergency : allEmergenciesList) {
                EmergencyDisplayData displayData = createDisplayData(emergency);
                if (displayData != null) {
                    allEmergencies.add(displayData);
                }
            }

            // Update display
            updateEmergenciesDisplay(allEmergencies);
            applyFilters();
            updateStatistics();

        } catch (Exception e) {
            System.err.println("Error loading all emergencies: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load emergencies. Please try again.", Alert.AlertType.ERROR);
        }
    }

    private EmergencyDisplayData createDisplayData(Emergency emergency) {
        try {
            // Format date
            String formattedDate = formatDate(emergency.getReportedAt());

            return new EmergencyDisplayData(
                    emergency,
                    emergency.getId(),
                    emergency.getEmergencyType(),
                    emergency.getGuideName(),
                    emergency.getLocation(),
                    emergency.getSeverity(),
                    emergency.getStatus(),
                    formattedDate
            );

        } catch (Exception e) {
            System.err.println("Error creating display data for emergency: " + emergency.getId());
            e.printStackTrace();
            return null;
        }
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "Not set";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        return dateTime.format(formatter);
    }

    private void applyFilters() {
        if (allEmergencies == null) return;

        String selectedStatus = statusFilter.getValue();
        String selectedSeverity = severityFilter.getValue();
        String searchText = searchField.getText().toLowerCase().trim();

        filteredEmergencies = allEmergencies.filtered(emergency -> {
            // Status filter
            boolean statusMatch = "All".equals(selectedStatus) ||
                    emergency.status().equalsIgnoreCase(selectedStatus);

            // Severity filter
            boolean severityMatch = "All".equals(selectedSeverity) ||
                    emergency.severity().equalsIgnoreCase(selectedSeverity);

            // Search filter
            boolean searchMatch = searchText.isEmpty() ||
                    emergency.emergencyType().toLowerCase().contains(searchText) ||
                    emergency.guideName().toLowerCase().contains(searchText) ||
                    emergency.location().toLowerCase().contains(searchText) ||
                    String.valueOf(emergency.id()).contains(searchText);

            return statusMatch && severityMatch && searchMatch;
        });

        emergencyTable.setItems(filteredEmergencies);
        updateTotalEmergenciesLabel();
    }

    private void updateEmergenciesDisplay(ObservableList<EmergencyDisplayData> emergencies) {
        allEmergencies = emergencies;
        emergencyTable.setItems(emergencies);
        updateTotalEmergenciesLabel();
    }

    private void updateTotalEmergenciesLabel() {
        int totalCount = (filteredEmergencies != null) ? filteredEmergencies.size() :
                (allEmergencies != null) ? allEmergencies.size() : 0;
        totalEmergenciesLabel.setText("Total Emergencies: " + totalCount);
    }

    private void updateStatistics() {
        if (allEmergencies == null) return;

        int total = allEmergencies.size();
        int pending = (int) allEmergencies.stream()
                .filter(e -> !"resolved".equalsIgnoreCase(e.status()))
                .count();
        int resolved = total - pending;

        totalEmergenciesLabel.setText("Total: " + total);
        pendingEmergenciesLabel.setText("Pending: " + pending);
        resolvedEmergenciesLabel.setText("Resolved: " + resolved);
    }

    private void viewSelectedEmergencyDetails() {
        EmergencyDisplayData selected = emergencyTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            viewEmergencyDetails(selected);
        }
    }

    private void viewEmergencyDetails(EmergencyDisplayData emergencyData) {
        Emergency emergency = emergencyData.originalEmergency();

        Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION);
        detailsAlert.setTitle("Emergency Details");
        detailsAlert.setHeaderText(emergency.getEmergencyType() + " - " + emergency.getSeverity());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss");
        String details = String.format(
                """
                        🚨 EMERGENCY INFORMATION:
                        ID: %d
                        Type: %s
                        Severity: %s
                        Status: %s
                        
                        📋 DESCRIPTION:
                        %s
                        
                        📍 LOCATION:
                        %s
                        
                        👨‍🦲 GUIDE INFORMATION:
                        Name: %s
                        Email: %s
                        
                        📞 CONTACT:
                        %s
                        
                        ⏰ TIMELINE:
                        Reported: %s
                        %s\
                        %s""",
                emergency.getId(),
                emergency.getEmergencyType(),
                emergency.getSeverity(),
                emergency.getStatus(),
                emergency.getDescription(),
                emergency.getLocation(),
                emergency.getGuideName(),
                emergency.getGuideEmail(),
                emergency.getContactNumber() != null ? emergency.getContactNumber() : "Not provided",
                emergency.getReportedAt().format(formatter),
                emergency.getResolvedAt() != null ?
                        "Resolved: " + emergency.getResolvedAt().format(formatter) + "\n" : "",
                emergency.getAdditionalNotes() != null ?
                        "\n📝 ADDITIONAL NOTES:\n" + emergency.getAdditionalNotes() : ""
        );

        detailsAlert.setContentText(details);
        detailsAlert.getDialogPane().setPrefWidth(500);
        detailsAlert.showAndWait();
    }

    private void resolveSelectedEmergency() {
        EmergencyDisplayData selected = emergencyTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            resolveEmergency(selected);
        }
    }

    private void resolveEmergency(EmergencyDisplayData emergencyData) {
        Emergency emergency = emergencyData.originalEmergency();

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Resolve Emergency");
        confirmAlert.setHeaderText("Resolve Emergency: " + emergency.getEmergencyType());
        confirmAlert.setContentText(
                "Are you sure you want to mark this emergency as resolved?\n\n" +
                        "Emergency ID: " + emergency.getId() + "\n" +
                        "Guide: " + emergency.getGuideName() + "\n" +
                        "Location: " + emergency.getLocation() + "\n\n" +
                        "This action will update the emergency status to 'Resolved'."
        );

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    emergency.setStatus("Resolved");
                    emergency.setResolvedAt(java.time.LocalDateTime.now());

                    if (jsonHandler.updateEmergency(emergency)) {
                        showAlert("Success", "Emergency marked as resolved successfully!", Alert.AlertType.INFORMATION);
                        loadAllEmergencies(); // Refresh the data
                    } else {
                        showAlert("Error", "Failed to update emergency status.", Alert.AlertType.ERROR);
                    }
                } catch (Exception e) {
                    System.err.println("Error resolving emergency: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Error", "An error occurred while resolving the emergency.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Data refreshed Successfully");
        alert.showAndWait();
    }

    // Inner class for table display data
        public record EmergencyDisplayData(Emergency originalEmergency, int id, String emergencyType, String guideName,
                                           String location, String severity, String status, String formattedReportedDate) {
    }
}
