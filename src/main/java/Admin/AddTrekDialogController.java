package Admin;

import Models.Attraction;
import Models.Guide;
import Models.Trek;
import Storage.AdminJSONHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class AddTrekDialogController {

    @FXML private TextField trekNameField;
    @FXML private TextField durationField;
    @FXML private DatePicker startDatePicker;
    @FXML private ComboBox<String> difficultyComboBox;
    @FXML private TextField maxAltitudeField;
    @FXML private TextField costField;
    @FXML private CheckBox applyDiscountCheckBox;
    @FXML private TextField discountPercentField;
    @FXML private TextField bestSeasonField;
    @FXML private ComboBox<String> attractionComboBox;
    @FXML private ComboBox<String> guideComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private TreksContentController parentController;
    private AdminJSONHandler jsonHandler;

    @FXML
    private void initialize() {
        jsonHandler = new AdminJSONHandler();

        // Setup difficulty options
        difficultyComboBox.getItems().addAll("Easy", "Moderate", "Hard", "Extreme");

        // Set default date to today
        startDatePicker.setValue(LocalDate.now());

        // Load attractions and guides for the combo boxes
        loadAttractions();
        loadGuides();

        // Set button actions
        saveButton.setOnAction(e -> saveTrek());
        cancelButton.setOnAction(e -> closeDialog());

        // Setup discount functionality
        setupDiscountControls();

        // Setup altitude validation
        setupAltitudeValidation();
    }

    private void setupAltitudeValidation() {
        // Only allow numeric input for altitude field
        maxAltitudeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                maxAltitudeField.setText(oldVal);
            } else {
                try {
                    if (!newVal.isEmpty()) {
                        int altitude = Integer.parseInt(newVal);
                        if (altitude < 0) {
                            maxAltitudeField.setText("0");
                        } else if (altitude > 10000) { // Reasonable upper limit
                            maxAltitudeField.setText("10000");
                        }
                    }
                } catch (NumberFormatException e) {
                    maxAltitudeField.setText(oldVal);
                }
            }
        });
    }

    private void setupDiscountControls() {
        // Initially hide discount percent field
        discountPercentField.setVisible(false);
        discountPercentField.setManaged(false);

        // Show/hide discount percent field when checkbox is toggled
        applyDiscountCheckBox.setOnAction(e -> {
            boolean isSelected = applyDiscountCheckBox.isSelected();
            discountPercentField.setVisible(isSelected);
            discountPercentField.setManaged(isSelected);

            if (!isSelected) {
                discountPercentField.clear();
            }
        });

        // Validate discount percentage input (0-100)
        discountPercentField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                discountPercentField.setText(oldVal);
            } else {
                try {
                    if (!newVal.isEmpty()) {
                        double percent = Double.parseDouble(newVal);
                        if (percent > 100) {
                            discountPercentField.setText("100");
                        } else if (percent < 0) {
                            discountPercentField.setText("0");
                        }
                    }
                } catch (NumberFormatException e) {
                    discountPercentField.setText(oldVal);
                }
            }
        });

        // Only allow numeric input for cost field
        costField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                costField.setText(oldVal);
            }
        });
    }

    private void loadAttractions() {
        List<Attraction> attractions = jsonHandler.loadAttractions();
        attractionComboBox.getItems().clear();
        for (Attraction attraction : attractions) {
            attractionComboBox.getItems().add(attraction.getName() + " (ID: " + attraction.getId() + ")");
        }
    }

    private void loadGuides() {
        List<Guide> guides = jsonHandler.loadGuides();
        guideComboBox.getItems().clear();
        for (Guide guide : guides) {
            guideComboBox.getItems().add(guide.getFirstName() + " " + guide.getLastName() + " (" + guide.getEmail() + ")");
        }
    }

    public void setParentController(TreksContentController parentController) {
        this.parentController = parentController;
    }

    private void saveTrek() {
        // Validate required fields
        if (trekNameField.getText().trim().isEmpty() ||
                durationField.getText().trim().isEmpty() ||
                startDatePicker.getValue() == null ||
                difficultyComboBox.getValue() == null ||
                maxAltitudeField.getText().trim().isEmpty() ||
                costField.getText().trim().isEmpty() ||
                bestSeasonField.getText().trim().isEmpty() ||
                attractionComboBox.getValue() == null ||
                guideComboBox.getValue() == null) {

            showAlert("Validation Error", "Please fill in all required fields.");
            return;
        }

        try {
            // Parse and validate altitude as integer
            int maxAltitude;
            try {
                maxAltitude = Integer.parseInt(maxAltitudeField.getText().trim());
                if (maxAltitude < 0) {
                    showAlert("Validation Error", "Altitude cannot be negative.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Validation Error", "Please enter a valid altitude in meters (numbers only).");
                return;
            }

            // Parse original cost
            double originalCost = Double.parseDouble(costField.getText().trim());

            // Calculate discount and final cost
            boolean hasDiscount = applyDiscountCheckBox.isSelected();
            double discountPercent = 0.0;
            double finalCost = originalCost;

            if (hasDiscount) {
                String discountText = discountPercentField.getText().trim();
                if (discountText.isEmpty()) {
                    showAlert("Validation Error", "Please enter discount percentage.");
                    return;
                }

                discountPercent = Double.parseDouble(discountText);
                if (discountPercent < 0 || discountPercent > 100) {
                    showAlert("Validation Error", "Discount percentage must be between 0 and 100.");
                    return;
                }

                // Calculate final cost after discount
                double discountAmount = originalCost * (discountPercent / 100.0);
                finalCost = originalCost - discountAmount;

                System.out.println("Original Cost: $" + String.format("%.2f", originalCost));
                System.out.println("Discount: " + discountPercent + "%");
                System.out.println("Discount Amount: $" + String.format("%.2f", discountAmount));
                System.out.println("Final Cost: $" + String.format("%.2f", finalCost));
            }

            // Extract attraction ID from combo box selection
            String attractionSelection = attractionComboBox.getValue();
            int attractionId = extractAttractionId(attractionSelection);

            // Extract guide email from combo box selection
            String guideSelection = guideComboBox.getValue();
            String guideEmail = extractGuideEmail(guideSelection);

            if (attractionId == -1) {
                showAlert("Error", "Invalid attraction selection.");
                return;
            }

            if (guideEmail == null) {
                showAlert("Error", "Invalid guide selection.");
                return;
            }

            // Create new trek with numeric altitude
            Trek trek = new Trek(
                    trekNameField.getText().trim(),
                    durationField.getText().trim(),
                    startDatePicker.getValue(),
                    difficultyComboBox.getValue(),
                    maxAltitude,  // Now passing integer instead of string
                    finalCost,
                    bestSeasonField.getText().trim(),
                    guideEmail,
                    attractionId
            );

            // Set discount information
            trek.setHasDiscount(hasDiscount);
            trek.setOriginalCost(originalCost);
            trek.setDiscountPercent(discountPercent);

            // Add to parent controller
            parentController.addTrek(trek);

            // Show success message with altitude warning if applicable
            String successMessage;
            if (hasDiscount) {
                successMessage = String.format("Trek '%s' has been added with %.1f%% discount.\nOriginal Cost: $%.2f\nFinal Cost: $%.2f",
                        trek.getTrekName(), discountPercent, originalCost, finalCost);
            } else {
                successMessage = String.format("Trek '%s' has been added.\nCost: $%.2f",
                        trek.getTrekName(), finalCost);
            }

            // Add altitude warning for admin awareness
            if (maxAltitude > 3000) {
                successMessage += String.format("\n\nNOTE: This trek reaches %d meters altitude. " +
                        "Tourists will see a high-altitude warning for this trek.", maxAltitude);
            }

            showSuccessAlert("Trek Added Successfully", successMessage);
            closeDialog();

        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter valid numeric values for cost and discount percentage.");
        } catch (Exception e) {
            showAlert("Error", "An error occurred while saving the trek: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int extractAttractionId(String attractionSelection) {
        try {
            int startIndex = attractionSelection.lastIndexOf("ID: ") + 4;
            int endIndex = attractionSelection.lastIndexOf(")");
            String idStr = attractionSelection.substring(startIndex, endIndex);
            return Integer.parseInt(idStr);
        } catch (Exception e) {
            return -1;
        }
    }

    private String extractGuideEmail(String guideSelection) {
        try {
            int startIndex = guideSelection.lastIndexOf("(") + 1;
            int endIndex = guideSelection.lastIndexOf(")");
            return guideSelection.substring(startIndex, endIndex);
        } catch (Exception e) {
            return null;
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
