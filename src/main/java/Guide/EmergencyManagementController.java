package Guide;

import Models.Emergency;
import Models.User;
import Session.UserSession;
import Storage.AdminJSONHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class EmergencyManagementController implements Initializable {

    @FXML
    private VBox emergencyReportsList;

    @FXML
    private Label noReportsLabel;

    @FXML
    private Button reportEmergencyButton;

    @FXML
    private Button refreshButton;

    private AdminJSONHandler emergencyHandler;
    private User currentGuide;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        emergencyHandler = new AdminJSONHandler();
        UserSession userSession = UserSession.getInstance();
        currentGuide = userSession.getCurrentUser();

        setupEventHandlers();
        loadEmergencyReports();

        // Debug log
        if (currentGuide != null) {
            System.out.println("EmergencyManagementController initialized for guide: " + currentGuide.getEmail());
        } else {
            System.err.println("WARNING: No guide found in session!");
        }
    }

    private void setupEventHandlers() {
        reportEmergencyButton.setOnAction(e -> showReportEmergencyDialog());
        refreshButton.setOnAction(e -> loadEmergencyReports());

        // Add hover effects
        addHoverEffect(reportEmergencyButton);
        addHoverEffect(refreshButton);
    }

    private void loadEmergencyReports() {
        try {
            if (currentGuide == null) {
                System.err.println("No guide logged in");
                displayNoReports();
                return;
            }

            String guideEmail = currentGuide.getEmail();
            System.out.println("Loading emergency reports for guide: " + guideEmail);

            // Get all emergency reports for this guide
            List<Emergency> guideEmergencies = emergencyHandler.getEmergenciesByGuideEmail(guideEmail);
            System.out.println("Found " + guideEmergencies.size() + " emergency reports");

            displayEmergencyReports(guideEmergencies);

        } catch (Exception e) {
            System.err.println("Error loading emergency reports: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load emergency reports. Please try again.", Alert.AlertType.ERROR);
        }
    }

    private void displayEmergencyReports(List<Emergency> emergencies) {
        emergencyReportsList.getChildren().clear();

        if (emergencies.isEmpty()) {
            displayNoReports();
            return;
        }

        // Hide the no reports label
        noReportsLabel.setVisible(false);

        for (Emergency emergency : emergencies) {
            VBox emergencyCard = createEmergencyCard(emergency);
            emergencyReportsList.getChildren().add(emergencyCard);
        }
    }

    private void displayNoReports() {
        emergencyReportsList.getChildren().clear();
        noReportsLabel.setVisible(true);
        emergencyReportsList.getChildren().add(noReportsLabel);
    }

    private VBox createEmergencyCard(Emergency emergency) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-border-color: #dee2e6; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");

        // Header with emergency type and severity
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label typeLabel = new Label("🚨 " + emergency.getEmergencyType());
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333333;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label severityLabel = new Label(emergency.getSeverity());
        severityLabel.setStyle(getSeverityStyle(emergency.getSeverity()));

        Label statusLabel = new Label(emergency.getStatus());
        statusLabel.setStyle(getStatusStyle(emergency.getStatus()));

        headerBox.getChildren().addAll(typeLabel, spacer, severityLabel, statusLabel);

        // Emergency details
        VBox detailsBox = new VBox(8);

        Label descriptionLabel = new Label("Description: " + emergency.getDescription());
        descriptionLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px; -fx-wrap-text: true;");

        Label locationLabel = new Label("📍 Location: " + emergency.getLocation());
        locationLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 13px;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        Label timeLabel = new Label("⏰ Reported: " + emergency.getReportedAt().format(formatter));
        timeLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 13px;");

        if (emergency.getContactNumber() != null && !emergency.getContactNumber().isEmpty()) {
            Label contactLabel = new Label("📞 Contact: " + emergency.getContactNumber());
            contactLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 13px;");
            detailsBox.getChildren().add(contactLabel);
        }

        detailsBox.getChildren().addAll(descriptionLabel, locationLabel, timeLabel);

        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewDetailsButton = new Button("View Details");
        viewDetailsButton.setStyle("-fx-background-color: transparent; -fx-border-color: #007bff; " +
                "-fx-border-radius: 15; -fx-text-fill: #007bff; -fx-padding: 6 12; " +
                "-fx-font-size: 12px; -fx-cursor: hand;");
        viewDetailsButton.setOnAction(e -> viewEmergencyDetails(emergency));

        buttonBox.getChildren().add(viewDetailsButton);

        card.getChildren().addAll(headerBox, detailsBox, buttonBox);
        return card;
    }

    private String getSeverityStyle(String severity) {
        String baseStyle = "-fx-background-radius: 12; -fx-padding: 4 8; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-font-weight: bold;";

        return switch (severity.toLowerCase()) {
            case "low" -> baseStyle + " -fx-background-color: #28a745;";
            case "medium" -> baseStyle + " -fx-background-color: #ffc107; -fx-text-fill: #333333;";
            case "high" -> baseStyle + " -fx-background-color: #fd7e14;";
            case "critical" -> baseStyle + " -fx-background-color: #dc3545;";
            default -> baseStyle + " -fx-background-color: #6c757d;";
        };
    }

    private String getStatusStyle(String status) {
        String baseStyle = "-fx-background-radius: 12; -fx-padding: 4 8; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-font-weight: bold;";

        return switch (status.toLowerCase()) {
            case "reported" -> baseStyle + " -fx-background-color: #dc3545;";
            case "in progress" -> baseStyle + " -fx-background-color: #ffc107; -fx-text-fill: #333333;";
            case "resolved" -> baseStyle + " -fx-background-color: #28a745;";
            default -> baseStyle + " -fx-background-color: #6c757d;";
        };
    }

    private void showReportEmergencyDialog() {
        if (currentGuide == null) {
            showAlert("Error", "No guide session found. Please log in again.", Alert.AlertType.ERROR);
            return;
        }

        Dialog<Emergency> dialog = new Dialog<>();
        dialog.setTitle("Report Emergency");
        dialog.setHeaderText("Report a new emergency situation");

        // Create form fields
        VBox formBox = new VBox(15);
        formBox.setPrefWidth(400);

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Medical Emergency", "Weather Emergency", "Equipment Failure",
                "Lost Tourist", "Accident", "Natural Disaster", "Other");
        typeCombo.setPromptText("Select emergency type");
        typeCombo.setPrefWidth(Double.MAX_VALUE);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Describe the emergency situation in detail...");
        descriptionArea.setPrefRowCount(3);

        TextField locationField = new TextField();
        locationField.setPromptText("Emergency location");

        ComboBox<String> severityCombo = new ComboBox<>();
        severityCombo.getItems().addAll("Low", "Medium", "High", "Critical");
        severityCombo.setValue("Medium");

        TextField contactField = new TextField();
        contactField.setPromptText("Emergency contact number");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Additional notes (optional)");
        notesArea.setPrefRowCount(2);

        formBox.getChildren().addAll(
                new Label("Emergency Type:"), typeCombo,
                new Label("Description:"), descriptionArea,
                new Label("Location:"), locationField,
                new Label("Severity:"), severityCombo,
                new Label("Contact Number:"), contactField,
                new Label("Additional Notes:"), notesArea
        );

        dialog.getDialogPane().setContent(formBox);

        ButtonType reportButtonType = new ButtonType("Report Emergency", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reportButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == reportButtonType) {
                if (typeCombo.getValue() == null || descriptionArea.getText().trim().isEmpty() ||
                        locationField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Please fill in all required fields.", Alert.AlertType.ERROR);
                    return null;
                }

                Emergency emergency = new Emergency(
                        currentGuide.getFullName(),
                        currentGuide.getEmail(),
                        typeCombo.getValue(),
                        descriptionArea.getText().trim(),
                        locationField.getText().trim(),
                        severityCombo.getValue(),
                        contactField.getText().trim()
                );

                if (!notesArea.getText().trim().isEmpty()) {
                    emergency.setAdditionalNotes(notesArea.getText().trim());
                }

                return emergency;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(emergency -> {
            if (emergencyHandler.addEmergency(emergency)) {
                showAlert("Success", "Emergency report submitted successfully!", Alert.AlertType.INFORMATION);
                loadEmergencyReports();
            } else {
                showAlert("Error", "Failed to submit emergency report. Please try again.", Alert.AlertType.ERROR);
            }
        });
    }

    private void viewEmergencyDetails(Emergency emergency) {
        Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION);
        detailsAlert.setTitle("Emergency Details");
        detailsAlert.setHeaderText(emergency.getEmergencyType() + " - " + emergency.getSeverity());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss");
        String details = String.format(
                """
                        🚨 EMERGENCY INFORMATION:
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
                        %s
                        
                        ℹ️ NOTE: Only administrators can update emergency status.""",
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

    private void addHoverEffect(Button button) {
        String originalStyle = button.getStyle();

        button.setOnMouseEntered(e -> {
            if (button == reportEmergencyButton) {
                button.setStyle(originalStyle + " -fx-background-color: #d32f2f;");
            } else {
                button.setStyle(originalStyle + " -fx-background-color: #5a6268;");
            }
        });

        button.setOnMouseExited(e -> button.setStyle(originalStyle));
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
